# Instalar herramientas para desplegar KatzenVet

## Por qué fallaron tus comandos

1. **`command not found: firebase` / `npm`** — no están instalados (o no están en el PATH).
2. **`zsh: number expected` / `unknown sort specifier`** — pegaste todo el bloque junto con líneas `# comentario`; zsh intentó ejecutar los comentarios como comandos.
3. **Solución:** instala las herramientas primero y ejecuta **un comando por línea** (o usa el script).

---

## Paso 1 — Instalar Node.js (incluye npm)

Opción A — Instalador oficial (más simple):

1. Abre https://nodejs.org/
2. Descarga la versión **LTS**
3. Instala el `.pkg`
4. Cierra y abre la Terminal
5. Verifica:

```bash
node -v
npm -v
```

Opción B — Con Homebrew (si ya tienes brew):

```bash
brew install node
node -v
npm -v
```

---

## Paso 2 — Instalar Firebase CLI

```bash
npm install -g firebase-tools
firebase --version
```

---

## Paso 3 — Iniciar sesión en Firebase

```bash
firebase login
```

Abre el navegador y entra con la cuenta de Google del proyecto `katzen-a0e3e`.

---

## Paso 4 — Desplegar (comandos individuales)

Copia y ejecuta **una línea a la vez**:

```bash
firebase database:get / --project katzen-a0e3e --pretty > ~/Desktop/backup-katzen.json
```

```bash
cd /Users/luisnino/Documents/GitHub/KatzenApp/firebase/functions && npm install
```

```bash
cd /Users/luisnino/Documents/GitHub/KatzenApp/firebase && firebase deploy --only functions --project katzen-a0e3e
```

```bash
cd /Users/luisnino/Documents/GitHub/KatzenWebAngular && firebase deploy --only database --project katzen-a0e3e
```

```bash
cd /Users/luisnino/Documents/GitHub/KatzenWebAngular && npm install && npm run build && firebase deploy --only hosting --project katzen-a0e3e
```

O todo junto (después de instalar Node y Firebase):

```bash
bash /Users/luisnino/Documents/GitHub/KatzenApp/scripts/deploy-katzen.sh
```

---

## Si `firebase login` pide permisos

Tu cuenta de Google debe ser **propietaria o editor** del proyecto Firebase `katzen-a0e3e`.
