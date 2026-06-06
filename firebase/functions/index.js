const { onCall, onRequest, HttpsError } = require("firebase-functions/v2/https");
const { onValueCreated } = require("firebase-functions/v2/database");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getDatabase } = require("firebase-admin/database");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

const db = getDatabase();
const auth = getAuth();
const messaging = getMessaging();

const CLIENTES_PATH = "Katzen/Cliente";
const USUARIOS_PATH = "Katzen/Usuarios";
const AUTH_PERFILES_PATH = "Katzen/AuthPerfiles";
const NOTIFICACIONES_PATH = "Katzen/Notificaciones";
const MASCOTA_PATH = "Katzen/Mascota";

function isClientRole(role) {
  return role === "client";
}

function isStaffRole(role) {
  return role === "staff";
}

async function getAuthPerfil(uid) {
  const snap = await db.ref(`${AUTH_PERFILES_PATH}/${uid}`).get();
  if (!snap.exists()) return null;
  return snap.val();
}

async function isStaffUser(uid, claims = {}) {
  if (claims.role === "staff") return true;
  if (claims.dualAccess === true) return true;
  const perfil = await getAuthPerfil(uid);
  if (!perfil || perfil.activo === false) return false;
  const roles = Array.isArray(perfil.roles) ? perfil.roles : [];
  return (
    isStaffRole(perfil.role) ||
    perfil.role === "dual" ||
    roles.includes("staff")
  );
}

async function assertStaff(callerUid, claims) {
  if (isClientRole(claims.role)) {
    throw new HttpsError("permission-denied", "Solo personal autorizado.");
  }
  const allowed = await isStaffUser(callerUid, claims);
  if (!allowed) {
    throw new HttpsError("permission-denied", "No tienes permisos de staff.");
  }
}

async function applyClaimsFromPerfil(uid, perfil) {
  if (!perfil || perfil.activo === false) {
    await auth.setCustomUserClaims(uid, {});
    return null;
  }

  const roles = Array.isArray(perfil.roles)
    ? perfil.roles.map((r) => String(r).toLowerCase())
    : [];
  const roleField = String(perfil.role || "").toLowerCase();
  const hasStaff =
    roleField === "staff" ||
    roleField === "dual" ||
    roles.includes("staff");
  const hasClient =
    roleField === "client" ||
    roleField === "dual" ||
    roles.includes("client");
  const clienteId = perfil.clienteId || null;

  if (hasStaff && hasClient && clienteId) {
    const claims = {
      role: "staff",
      staffRole: perfil.staffRole || "admin",
      clienteId,
      dualAccess: true,
    };
    await auth.setCustomUserClaims(uid, claims);
    return claims;
  }

  if (hasClient && clienteId) {
    await auth.setCustomUserClaims(uid, {
      role: "client",
      clienteId,
    });
    return { role: "client", clienteId };
  }

  if (hasStaff) {
    await auth.setCustomUserClaims(uid, {
      role: "staff",
      staffRole: perfil.staffRole || "staff",
    });
    return { role: "staff", staffRole: perfil.staffRole || "staff" };
  }

  await auth.setCustomUserClaims(uid, {});
  return null;
}

async function writeAuthPerfil(uid, data) {
  const payload = {
    ...data,
    authUid: uid,
    actualizado: new Date().toISOString(),
  };
  if (!payload.creado) payload.creado = payload.actualizado;
  await db.ref(`${AUTH_PERFILES_PATH}/${uid}`).update(payload);
  return payload;
}

async function findClienteIdByEmail(email) {
  const normalized = (email || "").trim().toLowerCase();
  if (!normalized) return null;

  const snap = await db.ref(CLIENTES_PATH).get();
  if (!snap.exists()) return null;

  let foundId = null;
  snap.forEach((child) => {
    if (foundId) return false;
    const cliente = child.val() || {};
    if ((cliente.correo || "").trim().toLowerCase() === normalized) {
      foundId = child.key;
    }
    return false;
  });
  if (foundId) return foundId;
  return null;
}

/**
 * Sincroniza custom claims desde Katzen/AuthPerfiles/{uid} del usuario logueado.
 */
exports.syncMyClaims = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Debes iniciar sesión.");
  }

  const uid = request.auth.uid;
  const perfil = await getAuthPerfil(uid);
  if (!perfil || perfil.activo === false) {
    return { success: false, role: "none", message: "Sin perfil asignado." };
  }

  const claims = await applyClaimsFromPerfil(uid, perfil);
  return { success: true, ...claims };
});

/**
 * Staff asigna perfil a un UID de Firebase Auth (tabla AuthPerfiles).
 */
exports.assignAuthProfile = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Debes iniciar sesión.");
  }
  await assertStaff(request.auth.uid, request.auth.token);

  const authUid = request.data?.authUid;
  const role = request.data?.role;
  const email = (request.data?.email || "").trim().toLowerCase();
  const clienteId = request.data?.clienteId || null;
  const staffRefId = request.data?.staffRefId || null;
  const staffRole = request.data?.staffRole || "admin";

  if (!authUid || typeof authUid !== "string") {
    throw new HttpsError("invalid-argument", "authUid es requerido.");
  }
  if (role !== "staff" && role !== "client" && role !== "dual") {
    throw new HttpsError("invalid-argument", "role debe ser staff, client o dual.");
  }

  let authUser;
  try {
    authUser = await auth.getUser(authUid);
  } catch (_) {
    throw new HttpsError("not-found", "UID no encontrado en Firebase Auth.");
  }

  const resolvedEmail = email || (authUser.email || "").toLowerCase();
  const existing = (await getAuthPerfil(authUid)) || {};
  const perfilData = {
    email: resolvedEmail,
    role,
    activo: true,
  };

  const wantsClient = role === "client" || role === "dual";
  const wantsStaff = role === "staff" || role === "dual";

  if (wantsClient) {
    let resolvedClienteId = clienteId || existing.clienteId || null;
    if (!resolvedClienteId) {
      resolvedClienteId = await findClienteIdByEmail(resolvedEmail);
    }
    if (!resolvedClienteId) {
      throw new HttpsError(
        "failed-precondition",
        "No hay Katzen/Cliente con ese correo. Crea el cliente primero."
      );
    }

    perfilData.clienteId = resolvedClienteId;
    perfilData.roles = wantsStaff ? ["staff", "client"] : ["client"];
    await db.ref(`${CLIENTES_PATH}/${resolvedClienteId}`).update({
      authUid,
      portalActivo: true,
      fechaInvitacion: new Date().toISOString(),
    });
  }

  if (wantsStaff) {
    perfilData.staffRole = staffRole || existing.staffRole || "admin";
    if (staffRefId) perfilData.staffRefId = staffRefId;
    if (!perfilData.roles) perfilData.roles = wantsClient ? ["staff", "client"] : ["staff"];
  }

  await writeAuthPerfil(authUid, perfilData);
  const claims = await applyClaimsFromPerfil(authUid, perfilData);

  return {
    success: true,
    authUid,
    perfil: perfilData,
    claims,
  };
});

/**
 * Compatibilidad web admin: asegura claims staff desde AuthPerfiles.
 */
exports.ensureStaffClaims = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Debes iniciar sesión.");
  }

  const uid = request.auth.uid;
  const perfil = await getAuthPerfil(uid);

  if (!perfil || !isStaffRole(perfil.role) || perfil.activo === false) {
    throw new HttpsError("permission-denied", "Usuario sin perfil staff.");
  }

  const claims = await applyClaimsFromPerfil(uid, perfil);
  return { success: true, ...claims };
});

/**
 * Invita un cliente existente al portal.
 * Escribe AuthPerfiles + authUid en Cliente (solo update).
 */
exports.inviteClientePortal = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Debes iniciar sesión.");
  }
  await assertStaff(request.auth.uid, request.auth.token);

  const clienteId = request.data?.clienteId;
  if (!clienteId || typeof clienteId !== "string") {
    throw new HttpsError("invalid-argument", "clienteId es requerido.");
  }

  const clienteRef = db.ref(`${CLIENTES_PATH}/${clienteId}`);
  const clienteSnap = await clienteRef.get();
  if (!clienteSnap.exists()) {
    throw new HttpsError("not-found", "Cliente no encontrado.");
  }

  const cliente = clienteSnap.val();
  const email = (cliente.correo || "").trim().toLowerCase();
  if (!email) {
    throw new HttpsError("failed-precondition", "El cliente debe tener correo electrónico.");
  }

  let authUid = cliente.authUid || null;
  let tempPassword = null;

  if (authUid) {
    try {
      await auth.getUser(authUid);
    } catch (_) {
      authUid = null;
    }
  }

  if (!authUid) {
    try {
      const existing = await auth.getUserByEmail(email);
      authUid = existing.uid;
    } catch (_) {
      tempPassword = generateTempPassword();
      const created = await auth.createUser({
        email,
        password: tempPassword,
        displayName: buildDisplayName(cliente),
        emailVerified: false,
      });
      authUid = created.uid;
    }
  }

  const perfilData = {
    email,
    role: "client",
    clienteId,
    activo: true,
  };
  await writeAuthPerfil(authUid, perfilData);
  await applyClaimsFromPerfil(authUid, perfilData);

  const now = new Date().toISOString();
  await clienteRef.update({
    authUid,
    portalActivo: true,
    fechaInvitacion: now,
  });

  return {
    success: true,
    clienteId,
    authUid,
    portalActivo: true,
    tempPassword: tempPassword || null,
    message: tempPassword
      ? "Portal activado. Comparte la contraseña temporal con el cliente."
      : "Portal activado. El cliente puede usar su contraseña existente o restablecerla.",
  };
});

/**
 * Aplica custom claims leyendo todos los registros de Katzen/AuthPerfiles.
 * Uso local: POST con header x-bootstrap-secret (ver setup-auth-profiles.js).
 */
exports.bootstrapApplyClaims = onRequest(async (req, res) => {
  if (req.method !== "POST") {
    res.status(405).json({ error: "Usa POST." });
    return;
  }

  const secret = req.headers["x-bootstrap-secret"];
  if (secret !== "katzen-bootstrap-2026") {
    res.status(403).json({ error: "Forbidden" });
    return;
  }

  const snap = await db.ref(AUTH_PERFILES_PATH).get();
  if (!snap.exists()) {
    res.status(200).json({ success: true, applied: 0, results: [] });
    return;
  }

  const results = [];
  const entries = [];
  snap.forEach((child) => {
    entries.push({ uid: child.key, perfil: child.val() || {} });
    return false;
  });

  for (const { uid, perfil } of entries) {
    try {
      const claims = await applyClaimsFromPerfil(uid, perfil);
      results.push({ uid, email: perfil.email, role: perfil.role, claims });
    } catch (error) {
      results.push({ uid, email: perfil.email, error: error.message });
    }
  }

  res.status(200).json({ success: true, applied: results.length, results });
});

exports.onVacunaCreated = onValueCreated("/Katzen/Vacunas/{vacunaId}", async (event) => {
  const vacuna = event.data.val();
  if (!vacuna || !vacuna.idPaciente) return;

  const mascotaSnap = await db.ref(`${MASCOTA_PATH}/${vacuna.idPaciente}`).get();
  if (!mascotaSnap.exists()) return;

  const mascota = mascotaSnap.val();
  const clienteId = mascota.idCliente;
  if (!clienteId) return;

  const clienteSnap = await db.ref(`${CLIENTES_PATH}/${clienteId}`).get();
  if (!clienteSnap.exists() || !clienteSnap.val().portalActivo) return;

  const notifRef = db.ref(`${NOTIFICACIONES_PATH}/${clienteId}`).push();
  await notifRef.set({
    id: notifRef.key,
    tipo: "vacuna",
    mascotaId: vacuna.idPaciente,
    titulo: "Nueva vacuna registrada",
    mensaje: `${mascota.nombre || "Tu mascota"} recibió: ${vacuna.vacuna || "vacuna"}`,
    leida: false,
    fecha: new Date().toISOString(),
    referenciaId: event.params.vacunaId,
  });
  await sendPortalPush(
    clienteId,
    "Nueva vacuna registrada",
    `${mascota.nombre || "Tu mascota"} recibió: ${vacuna.vacuna || "vacuna"}`,
    {
      tipo: "vacuna",
      mascotaId: String(vacuna.idPaciente || ""),
      referenciaId: String(event.params.vacunaId || ""),
    }
  );
});

exports.onCitaCreated = onValueCreated("/Katzen/Citas/{citaId}", async (event) => {
  const cita = event.data.val();
  if (!cita || cita.activo === false) return;

  const pacienteId = cita.paciente_id || cita.idPaciente;
  const clienteId = cita.cliente_id || cita.idCliente;
  if (!pacienteId && !clienteId) return;

  let resolvedClienteId = clienteId;
  let mascotaNombre = cita.paciente || "Tu mascota";

  if (pacienteId) {
    const mascotaSnap = await db.ref(`${MASCOTA_PATH}/${pacienteId}`).get();
    if (mascotaSnap.exists()) {
      const mascota = mascotaSnap.val();
      mascotaNombre = mascota.nombre || mascotaNombre;
      resolvedClienteId = resolvedClienteId || mascota.idCliente;
    }
  }

  if (!resolvedClienteId) return;

  const clienteSnap = await db.ref(`${CLIENTES_PATH}/${resolvedClienteId}`).get();
  if (!clienteSnap.exists() || !clienteSnap.val().portalActivo) return;

  const fecha = cita.fecha_hora || cita.fecha || "";
  const notifRef = db.ref(`${NOTIFICACIONES_PATH}/${resolvedClienteId}`).push();
  await notifRef.set({
    id: notifRef.key,
    tipo: "cita",
    mascotaId: pacienteId || "",
    titulo: "Nueva cita programada",
    mensaje: `${mascotaNombre}: ${cita.motivo || "Cita"}${fecha ? ` · ${fecha}` : ""}`,
    leida: false,
    fecha: new Date().toISOString(),
    referenciaId: event.params.citaId,
  });
  await sendPortalPush(
    resolvedClienteId,
    "Nueva cita programada",
    `${mascotaNombre}: ${cita.motivo || "Cita"}${fecha ? ` · ${fecha}` : ""}`,
    {
      tipo: "cita",
      mascotaId: String(pacienteId || ""),
      referenciaId: String(event.params.citaId || ""),
    }
  );
});

exports.onHistorialCreated = onValueCreated("/Katzen/Historiales_Clinicos/{historialId}", async (event) => {
  const historial = event.data.val();
  if (!historial) return;
  if (historial.ocultoPortal === true || historial.oculto_portal === true) return;

  const pacienteId = historial.paciente_id || historial.idPaciente;
  if (!pacienteId) return;

  const mascotaSnap = await db.ref(`${MASCOTA_PATH}/${pacienteId}`).get();
  if (!mascotaSnap.exists()) return;

  const mascota = mascotaSnap.val();
  const clienteId = mascota.idCliente;
  if (!clienteId) return;

  const clienteSnap = await db.ref(`${CLIENTES_PATH}/${clienteId}`).get();
  if (!clienteSnap.exists() || !clienteSnap.val().portalActivo) return;

  const diagnostico =
    historial.diagnostico ||
    historial.diagnostico_presuntivo ||
    "Actualización clínica";

  const notifRef = db.ref(`${NOTIFICACIONES_PATH}/${clienteId}`).push();
  await notifRef.set({
    id: notifRef.key,
    tipo: "historial",
    mascotaId: pacienteId,
    titulo: "Nuevo registro clínico",
    mensaje: `${mascota.nombre || "Tu mascota"}: ${diagnostico}`,
    leida: false,
    fecha: new Date().toISOString(),
    referenciaId: event.params.historialId,
  });
  await sendPortalPush(
    clienteId,
    "Nuevo registro clínico",
    `${mascota.nombre || "Tu mascota"}: ${diagnostico}`,
    {
      tipo: "historial",
      mascotaId: String(pacienteId || ""),
      referenciaId: String(event.params.historialId || ""),
    }
  );
});

function buildDisplayName(cliente) {
  return [cliente.nombre, cliente.apellidoPaterno, cliente.apellidoMaterno]
    .filter(Boolean)
    .join(" ")
    .trim();
}

function generateTempPassword() {
  const chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$";
  let password = "";
  for (let i = 0; i < 12; i++) {
    password += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return password;
}

function portalTopic(clienteId) {
  return `portal_cliente_${String(clienteId || "").replace(/\./g, "_")}`;
}

async function sendPortalPush(clienteId, title, body, extraData = {}) {
  if (!clienteId) return;
  try {
    await messaging.send({
      topic: portalTopic(clienteId),
      notification: { title, body },
      data: {
        target: "portal",
        clienteId: String(clienteId),
        ...Object.fromEntries(
          Object.entries(extraData).map(([key, value]) => [key, String(value ?? "")])
        ),
      },
    });
  } catch (error) {
    console.warn("FCM portal push failed:", error.message);
  }
}
