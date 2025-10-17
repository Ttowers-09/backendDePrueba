# Demo OIDC Front (Vite)

Frontend mínimo para probar tu backend OAuth2/OIDC con Google.

## Requisitos

- Node.js 18+

## Ejecutar en desarrollo

```powershell
cd "c:\Users\BUITR\Desktop\1. Proyecto ARSW\frontend"
npm install
npm run dev
```

Vite abrirá http://localhost:5173.

## Configurar el backend

- Asegúrate de que el backend conozca esta URL para el post-login.
- Si quieres redirigir aquí después del login, establece en el backend:
  - APP_FRONTEND_URL=http://localhost:5173
- Si prefieres que el backend responda JSON (sin redirigir), usa:
  - APP_FRONTEND_URL=none

## Flujo

- En http://localhost:5173 verás un botón “Login con Google”.
- Te enviará a `${BACKEND}/api/auth/login/google`.
- Al volver, el backend redirige a `http://localhost:5173/oauth2/callback#accessToken=...&refreshToken=...&tokenType=Bearer&expiresIn=...`.
- Esta página guarda los tokens en LocalStorage y puedes probar `/api/test/user` con el botón correspondiente.

## Cambiar la URL del backend

- Por defecto apunta a `http://localhost:8080`.
- Puedes arrancar Vite con una env para otra URL:

```powershell
$env:VITE_BACKEND_BASE = "http://localhost:8080"; npm run dev
```
