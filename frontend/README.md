# Frontend

## Variables de entorno

Crea un archivo `.env` en la raíz del proyecto (`frontend/.env`, no se commitea) con las siguientes variables:

```env
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_...
VITE_API_BASE_URL=http://localhost:8080
```

- `NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY`: clave pública de Clerk (login/logout). Sácala de tu propio proyecto en [clerk.com](https://clerk.com) — cada integrante puede usar su propia instancia de desarrollo de Clerk.
- `VITE_API_BASE_URL`: URL del backend. Para desarrollo local con el backend corriendo en el perfil `local` (ver `backend/README.md`), usa `http://localhost:8080`. Si no se define, el frontend cae por defecto al backend de producción (`https://tonyy1-bit.hf.space`).

## Requisitos

- Node.js 18+ y npm.
- El backend corriendo (local o apuntando a producción vía `VITE_API_BASE_URL`) para que las pantallas carguen datos.

## Comandos útiles

```bash
# Instalar dependencias
npm install

# Correr en modo desarrollo (hot reload)
npm run dev

# Compilar para producción
npm run build

# Vistazo previo de la build de producción
npm run preview

# Linter (ESLint) util para evitar errrores. 
npm run lint

