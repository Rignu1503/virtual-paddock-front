# 🔐 Guía de Integración Frontend — Autenticación Segura (JWT + HttpOnly Cookies)

Esta guía explica cómo el Frontend (Angular/React/Vue) debe integrarse con el sistema de autenticación de **Virtual Paddock**.

El sistema utiliza un enfoque de **Alta Seguridad** contra ataques XSS y CSRF mediante un **sistema de token dual**:
1. **Access Token (JWT)**: Vida corta (15 min). Viaja en el body (`AuthResponse`) y debe ser inyectado en la cabecera `Authorization: Bearer <token>` en cada petición privada.
2. **Refresh Token**: Vida larga (7 días). Viaja y se almacena **automáticamente** en una Cookie del navegador con los flags `HttpOnly` y `Secure`. El frontend no puede ni necesita leer este token.

---

## ⚙️ Configuración Crucial del Cliente HTTP (Axios / Fetch / HttpClient)

Para que el sistema funcione (especialmente el refresh token y el logout), **tu cliente HTTP debe incluir credenciales en las peticiones**.

### Ejemplo en `Axios` (React / Vue)
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true // ⚠️ CRÍTICO: Permite enviar y recibir cookies HttpOnly
});
```

### Ejemplo en `HttpClient` (Angular)
```typescript
this.http.post(url, body, { 
  withCredentials: true // ⚠️ CRÍTICO
});
```

---

## 🛠️ Catálogo de Endpoints de Autenticación (`/api/auth`)

Todas estas rutas son públicas y no requieren enviar el header `Authorization`.

### 1. Registrar Usuario (`POST /api/auth/register`)

Crea la cuenta, retorna el JWT y establece la cookie HttpOnly con el refresh token.

- **Body Request (`RegisterRequest`)**:
```json
{
  "email": "admin@virtualpaddock.com",
  "password": "Password123",
  "role": "SUPERADMIN" // o "LEAGUE_ADMIN"
}
```

- **Respuesta Exitosa (201 Created)**:
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "email": "admin@virtualpaddock.com",
    "role": "SUPERADMIN"
  }
}
```
*(Además, recibirás el header `Set-Cookie: refresh_token=...; HttpOnly; Secure`)*

---

### 2. Iniciar Sesión (`POST /api/auth/login`)

Valida credenciales, retorna el JWT y establece una nueva cookie HttpOnly con el refresh token.

- **Body Request (`LoginRequest`)**:
```json
{
  "email": "admin@virtualpaddock.com",
  "password": "Password123"
}
```

- **Respuesta Exitosa (200 OK)**:
*(Igual que la respuesta de Register)*

---

### 3. Refrescar Token (`POST /api/auth/refresh`)

Se usa cuando el JWT expira (obtienes un `401 Unauthorized`). El navegador enviará la cookie automáticamente.

- **Body Request**: Vacío (`{}`) o sin body.
- **Respuesta Exitosa (200 OK)**:
```json
{
  "success": true,
  "message": "Token refrescado exitosamente",
  "data": {
    "accessToken": "NUEVO_JWT_eyJhbGciOiJI...",
    "userId": 1,
    "email": "admin@virtualpaddock.com",
    "role": "SUPERADMIN"
  }
}
```

---

### 4. Cerrar Sesión (`POST /api/auth/logout`)

Invalida el refresh token en la base de datos y le dice al navegador que elimine la cookie.

- **Body Request**: Vacío.
- **Respuesta Exitosa (200 OK)**:
```json
{
  "success": true,
  "message": "Logout exitoso",
  "data": null
}
```

---

## 🛡️ Cómo Consumir Rutas Protegidas

A excepción de `/api/auth/**`, **TODA** la API está protegida (ej: `/api/leagues`, `/api/drivers`). 
Para consumirla, debes inyectar el `accessToken` en los headers de tu cliente HTTP.

### Ejemplo de Interceptor (Axios):
```javascript
api.interceptors.request.use(config => {
  const token = store.getState().auth.accessToken; // O de donde lo guardes en memoria
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});
```

## 🔄 Flujo Recomendado de Interceptor para "Silent Refresh"

1. Haces una petición a `/api/leagues`.
2. El servidor retorna `401 Unauthorized` (Token expirado).
3. Tu interceptor captura el error 401.
4. Llama silenciosamente a `POST /api/auth/refresh` (esto envía la cookie).
5. Si el refresh es exitoso, guardas el nuevo `accessToken` y **reintentas** la petición original a `/api/leagues`.
6. Si el refresh falla (ej: cookie expiró después de 7 días), el usuario debe ser redirigido a la pantalla de Login.
