# Corregir permisos Cloud Functions (katzen-a0e3e)

Proyecto: `katzen-a0e3e`  
Número de proyecto: `262209452533`

## Qué pasó

El primer deploy de **Cloud Functions 2nd gen** falló por permisos de:

1. **Cloud Build** (compilar las functions)
2. **Eventarc** (trigger `onVacunaCreated` cuando se crea una vacuna)

Las functions **inviteClientePortal** y **ensureStaffClaims** son las críticas para el portal.
`onVacunaCreated` es opcional (notificaciones automáticas).

---

## Opción rápida — Reintentar en 10 minutos

Google a veces tarda en propagar permisos la primera vez:

```bash
cd /Users/luisnino/Documents/GitHub/KatzenApp/firebase
firebase deploy --only "functions:ensureStaffClaims,functions:inviteClientePortal" --project katzen-a0e3e
```

Si eso funciona, continúa el deploy completo:

```bash
bash /Users/luisnino/Documents/GitHub/KatzenApp/scripts/deploy-katzen.sh
```

---

## Opción IAM — Consola Google Cloud

1. Abre: https://console.cloud.google.com/iam-admin/iam?project=katzen-a0e3e

2. Busca esta cuenta de servicio y edítala (lápiz):

   `262209452533-compute@developer.gserviceaccount.com`

   Agrega estos roles:
   - **Cloud Build Service Account**
   - **Cloud Functions Admin**
   - **Service Account User**
   - **Artifact Registry Reader**

3. Busca (o agrega principal) la cuenta Eventarc:

   `service-262209452533@gcp-sa-eventarc.iam.gserviceaccount.com`

   Rol: **Eventarc Service Agent**

4. Guarda y espera 2–5 minutos.

5. Reintenta:

```bash
cd /Users/luisnino/Documents/GitHub/KatzenApp/firebase
firebase deploy --only functions --project katzen-a0e3e
```

---

## Verificar plan Firebase

Cloud Functions requiere plan **Blaze** (pago por uso).  
Firebase Console → Configuración del proyecto → Uso y facturación.

---

## Deploy sin el trigger (si solo quieres el portal ya)

```bash
cd /Users/luisnino/Documents/GitHub/KatzenApp/firebase
firebase deploy --only "functions:ensureStaffClaims,functions:inviteClientePortal" --project katzen-a0e3e

cd /Users/luisnino/Documents/GitHub/KatzenWebAngular
firebase deploy --only database --project katzen-a0e3e
npm install && npm run build && firebase deploy --only hosting --project katzen-a0e3e
```
