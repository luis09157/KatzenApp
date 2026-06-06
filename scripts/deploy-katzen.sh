#!/usr/bin/env bash
# Despliegue KatzenVet — ejecutar: bash scripts/deploy-katzen.sh
set -euo pipefail

PROJECT="katzen-a0e3e"
PROJECT_NUMBER="262209452533"
KATZEN_APP="/Users/luisnino/Documents/GitHub/KatzenApp"
KATZEN_WEB="/Users/luisnino/Documents/GitHub/KatzenWebAngular"
FIREBASE_DIR="$KATZEN_APP/firebase"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "ERROR: no se encontró '$1'."
    exit 1
  fi
}

require_cmd node
require_cmd npm
require_cmd firebase

echo "=== Proyecto: $PROJECT ==="

echo ""
echo "[1/6] Backup RTDB..."
BACKUP_FILE="$KATZEN_APP/backup-rtdb-$(date +%Y%m%d-%H%M%S).json"
firebase database:get / --project "$PROJECT" --pretty > "$BACKUP_FILE"
echo "Backup guardado en: $BACKUP_FILE"

echo ""
echo "[2/6] Dependencias Cloud Functions..."
cd "$FIREBASE_DIR/functions"
npm install

echo ""
echo "[3/6] Functions críticas (portal + staff)..."
cd "$FIREBASE_DIR"
firebase deploy --only "functions:ensureStaffClaims,functions:inviteClientePortal" --project "$PROJECT"

echo ""
echo "[4/6] Function opcional onVacunaCreated (notificaciones automáticas)..."
set +e
firebase deploy --only "functions:onVacunaCreated" --project "$PROJECT"
TRIGGER_STATUS=$?
set -e
if [[ $TRIGGER_STATUS -ne 0 ]]; then
  echo ""
  echo "AVISO: onVacunaCreated no se desplegó (permisos Eventarc/Cloud Build)."
  echo "El portal y 'Activar portal' SÍ pueden funcionar sin esta función."
  echo "Reintenta en 10 minutos o revisa permisos IAM (ver scripts/FIX-FUNCTIONS-IAM.md)."
fi

echo ""
echo "[5/6] Reglas RTDB..."
cd "$KATZEN_WEB"
firebase deploy --only database --project "$PROJECT"

echo ""
echo "[6/6] Build y hosting (web + portal)..."
npm install
npm run build
firebase deploy --only hosting --project "$PROJECT"

echo ""
echo "=== Despliegue completado ==="
echo "Portal: https://katzen-a0e3e.web.app/portal/login"
echo "Admin:  https://katzen-a0e3e.web.app/admin/login"
echo ""
echo "Si onVacunaCreated falló, el portal funciona igual."
echo "Consola IAM: https://console.cloud.google.com/iam-admin/iam?project=$PROJECT"
