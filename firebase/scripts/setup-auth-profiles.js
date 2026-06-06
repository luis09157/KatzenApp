#!/usr/bin/env node
/**
 * Configura perfiles iniciales en Katzen/AuthPerfiles.
 * Usa Firebase CLI (sin service account local).
 *
 * Ejecutar desde firebase/:
 *   node scripts/setup-auth-profiles.js
 *
 * Admin:  luisk2ify@gmail.com          → vje6BLSP2yZHAOq2ELkELv7OzVh1
 * Cliente: soldad_cowboy@hotmail.com    → imJAaNtDyRQOb4RrJkTnJaYu09T2
 */
const { execSync } = require("child_process");
const fs = require("fs");
const os = require("os");
const path = require("path");

const PROJECT = "katzen-a0e3e";
const FIREBASE_DIR = path.join(__dirname, "..");
const BOOTSTRAP_SECRET = "katzen-bootstrap-2026";

const AUTH_PERFILES = "Katzen/AuthPerfiles";
const CLIENTES = "Katzen/Cliente";

const PROFILES = [
  {
    uid: "vje6BLSP2yZHAOq2ELkELv7OzVh1",
    email: "luisk2ify@gmail.com",
    role: "dual",
    staffRole: "admin",
    clienteId: "9b969f00-dd8d-4b95-830e-849d92c5871c",
  },
  {
    uid: "imJAaNtDyRQOb4RrJkTnJaYu09T2",
    email: "soldad_cowboy@hotmail.com",
    role: "client",
  },
];

function runFirebase(args) {
  const cmd = `firebase ${args.join(" ")} --project ${PROJECT}`;
  execSync(cmd, { cwd: FIREBASE_DIR, stdio: "inherit", encoding: "utf8" });
}

function firebaseGet(pathRef) {
  const out = execSync(
    `firebase database:get ${pathRef} --project ${PROJECT}`,
    { cwd: FIREBASE_DIR, encoding: "utf8" }
  ).trim();
  if (!out || out === "null") return null;
  return JSON.parse(out);
}

function writeTempJson(data) {
  const file = path.join(os.tmpdir(), `katzen-bootstrap-${Date.now()}.json`);
  fs.writeFileSync(file, JSON.stringify(data, null, 2));
  return file;
}

function findClienteIdByEmail(email) {
  const all = firebaseGet(`/${CLIENTES}`);
  if (!all) return null;

  const target = email.trim().toLowerCase();
  for (const [id, val] of Object.entries(all)) {
    if (((val && val.correo) || "").trim().toLowerCase() === target) {
      return id;
    }
  }
  return null;
}

function ensureCliente(email, authUid, now) {
  let clienteId = findClienteIdByEmail(email);
  if (clienteId) {
    const patch = writeTempJson({
      authUid,
      portalActivo: true,
      fechaInvitacion: now,
    });
    runFirebase(["database:update", `/${CLIENTES}/${clienteId}`, patch, "--force"]);
    fs.unlinkSync(patch);
    console.log(`Cliente existente vinculado: ${clienteId}`);
    return clienteId;
  }

  const payload = {
    nombre: "Soldad",
    apellidoPaterno: "Cowboy",
    apellidoMaterno: "",
    correo: email,
    telefono: "8100000000",
    expediente: "PORTAL-001",
    activo: true,
    fecha: new Date().toLocaleDateString("es-MX"),
    authUid,
    portalActivo: true,
    fechaInvitacion: now,
  };

  const file = writeTempJson(payload);
  const pushOut = execSync(
    `printf 'y\\n' | firebase database:push /${CLIENTES} ${file} --project ${PROJECT}`,
    { cwd: FIREBASE_DIR, encoding: "utf8" }
  );
  fs.unlinkSync(file);

  const match = pushOut.match(/\/Cliente\/([^\s]+)/);
  clienteId = match ? match[1] : null;
  if (!clienteId) {
    throw new Error("No se pudo obtener el ID del cliente creado.");
  }

  const idPatch = writeTempJson({ id: clienteId });
  runFirebase(["database:update", `/${CLIENTES}/${clienteId}`, idPatch, "--force"]);
  fs.unlinkSync(idPatch);

  console.log(`Cliente creado: ${clienteId}`);
  return clienteId;
}

async function applyClaimsViaFunction() {
  const url = `https://us-central1-${PROJECT}.cloudfunctions.net/bootstrapApplyClaims`;
  const response = await fetch(url, {
    method: "POST",
    headers: {
      "x-bootstrap-secret": BOOTSTRAP_SECRET,
      "Content-Type": "application/json",
    },
    body: "{}",
  });

  const raw = await response.text();
  let body;
  try {
    body = JSON.parse(raw);
  } catch (_) {
    throw new Error(`bootstrapApplyClaims falló (${response.status}): ${raw}`);
  }
  if (!response.ok) {
    throw new Error(`bootstrapApplyClaims falló: ${JSON.stringify(body)}`);
  }
  return body;
}

async function main() {
  const now = new Date().toISOString();

  for (const item of PROFILES) {
    const perfil = {
      authUid: item.uid,
      email: item.email,
      role: item.role,
      activo: true,
      creado: now,
      actualizado: now,
    };

    if (item.role === "staff" || item.role === "dual") {
      perfil.staffRole = item.staffRole || "admin";
    }

    if (item.role === "client" || item.role === "dual") {
      perfil.clienteId = item.clienteId || ensureCliente(item.email, item.uid, now);
      perfil.roles = item.role === "dual" ? ["staff", "client"] : ["client"];
    }

    if (item.role === "staff") {
      perfil.roles = ["staff"];
    }

    const file = writeTempJson(perfil);
    runFirebase(["database:update", `/${AUTH_PERFILES}/${item.uid}`, file, "--force"]);
    fs.unlinkSync(file);

    console.log(`OK RTDB ${item.email} → ${item.role} (${item.uid})`);
  }

  console.log("\nAplicando custom claims...");
  const claimsResult = await applyClaimsViaFunction();
  console.log(JSON.stringify(claimsResult, null, 2));

  console.log("\nPerfiles listos. Cierra sesión y vuelve a entrar en app/web.");
}

main().catch((err) => {
  console.error(err.message || err);
  process.exit(1);
});
