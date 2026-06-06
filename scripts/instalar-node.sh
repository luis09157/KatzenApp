#!/usr/bin/env bash
# Descarga e instala Node.js LTS en Mac Apple Silicon (sin Homebrew)
set -euo pipefail

NODE_VERSION="v24.16.0"
ARCH="$(uname -m)"
TMP_DIR="${TMPDIR:-/tmp}/katzen-node-install"
PKG_URL=""
TAR_NAME=""

if [[ "$ARCH" == "arm64" ]]; then
  TAR_NAME="node-${NODE_VERSION}-darwin-arm64.tar.gz"
  PKG_URL="https://nodejs.org/dist/${NODE_VERSION}/${TAR_NAME}"
else
  TAR_NAME="node-${NODE_VERSION}-darwin-x64.tar.gz"
  PKG_URL="https://nodejs.org/dist/${NODE_VERSION}/${TAR_NAME}"
fi

INSTALL_DIR="$HOME/.local/node"
PROFILE_FILE="$HOME/.zshrc"

echo "Instalando Node.js ${NODE_VERSION} para ${ARCH}..."
mkdir -p "$TMP_DIR" "$INSTALL_DIR"
cd "$TMP_DIR"

if [[ ! -f "$TAR_NAME" ]]; then
  echo "Descargando $PKG_URL"
  curl -fsSL "$PKG_URL" -o "$TAR_NAME"
fi

tar -xzf "$TAR_NAME" -C "$INSTALL_DIR" --strip-components=1

MARKER="# Katzen Node.js PATH"
if ! grep -q "$MARKER" "$PROFILE_FILE" 2>/dev/null; then
  {
    echo ""
    echo "$MARKER"
    echo "export PATH=\"$INSTALL_DIR/bin:\$PATH\""
  } >> "$PROFILE_FILE"
fi

export PATH="$INSTALL_DIR/bin:$PATH"

echo ""
echo "Node instalado en: $INSTALL_DIR"
node -v
npm -v
echo ""
echo "IMPORTANTE: cierra esta Terminal, abre una nueva y ejecuta:"
echo "  node -v"
echo "  npm install -g firebase-tools"
echo "  firebase login"
