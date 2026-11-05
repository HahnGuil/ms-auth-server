# Endpoints and flows

Local base URL: `http://localhost:2310/auth-server`. In other environments, use the service host followed by `/auth-server`. Protected endpoints use `Authorization: Bearer <token>`. Error responses generally contain `message` and `timestamp`.

## Endpoints

| Method and path | Input | Response and purpose |
|---|---|---|
| `POST /users` | JSON: `username`, `email`, `password`, `firstName`, `lastName`, `typeUser` (`DIRECT_USER` or `OAUTH_USER`), and `applicationCode` (numeric application ID). | `201`, `userId`, `userName`, `email`, `token`, and `refreshToken`. Creates or associates a password-based account with an application. |
| `POST /login` | JSON: `applicationPublicId` (UUID), `email`, and `password`. | `201`, `userName`, `email`, `token`, and `refreshToken`. Logs in a password-based account. |
| `GET /oauth2/authorization/google` | Redirects to Google; no body. Google returns to `/login/oauth2/code/google`. | On success, sends tokens to a popup using `postMessage` (`google-login`) or redirects to the configured frontend callback with URL-encoded `data`. |
| `POST /application` | `publicId` (UUID) header; no body. | `201`, `applicationToken` and `refreshApplicationToken`. Issues tokens for the registered application. |
| `POST /token` | `Authorization: Bearer <refreshToken>`; no body. | `201`, a new `LoginResponse`. Renews user tokens. |
| `POST /token/application` | `Authorization: Bearer <refreshApplicationToken>` and `publicId` (UUID) headers. | `201`, new application tokens. |
| `DELETE /login` | Access Bearer token and JSON `{ "email": "person@example.com" }`. The session is identified by the token. | `204`, no body. Logs out and invalidates the active session/token. |
| `POST /application/register` | Authenticated admin Bearer token; JSON with `nameApplication`, `applicationEmail`, `applicationDomain`, `resendApiKey`, `definePasswordPolicy` and optional `useDefaultPasswordPolicy`/`passwordExpirationMonths`. | `201`, application data and `applicationPublicId`. Registers an application. |
| `POST /users/register` | User Bearer token and `applicationPublicId` (UUID) header. | `201`, success message. Associates an OAuth user with an application; direct users do not need this association flow. |
| `PATCH /users` | User Bearer token and `applicationPublicID` (UUID) header. | `204`, no body. Updates the user's role in that application when already associated. |
| `GET /users/me` | User Bearer token. | `200`, authenticated user's `userName`, `fullName`, and `email`. |
| `PATCH /users/username` | User Bearer token; JSON `{ "userName": "new_name" }`. | `204`, no body. Changes the username. |
| `PATCH /password/change-password` | Access Bearer token; JSON: `applicationPublicId`, `email`, `oldPassword`, `newPassword`. | `204`, no body. Changes the password for the current application. |
| `POST /password/reset-request` | JSON: `email`, `applicationPublicId`. | `200`, message; sends a recovery code by email. |
| `POST /password/validate-code` | JSON: `applicationPublicId`, `email`, `recoveryCode`. | `201`, `recoverToken`. Validates the emailed code. |
| `PATCH /password/reset-password` | `recoverToken` Bearer token; JSON `{ "newPassword": "..." }`. | `204`, no body. Sets the new password. |
| `GET /public-key/jwks` | No authentication. | `200`, JWKS (`keys`) for validating JWT signatures. Recommended for consumers. |
| `GET /public-key/legacy` | Requires authentication under the current security configuration. | Map of `kid` to public key encoded in Base64 using the legacy format. |

Passwords submitted for registration, login, and changes must be 8–12 characters, with at least one digit, uppercase letter, and special character. `applicationCode` in `/users` is a numeric ID; other flows use the public UUID `applicationPublicId`.

## Example flows

**Password-based registration and login**

```http
POST /auth-server/users
Content-Type: application/json

{
  "username": "ana",
  "email": "ana@example.com",
  "password": "SecurePass1!",
  "firstName": "Ana",
  "lastName": "Silva",
  "typeUser": "DIRECT_USER",
  "applicationCode": 1
}
```

Use the returned `token` for authenticated calls and keep `refreshToken` for `POST /token`. Login requires the application's public UUID:

```json
{
  "applicationPublicId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "ana@example.com",
  "password": "SecurePass1!"
}
```

**Password recovery:** `POST /password/reset-request` sends a code; submit the code and application to `POST /password/validate-code`; use the returned `recoverToken` as Bearer in `PATCH /password/reset-password`.

**Google OAuth:** open `GET /oauth2/authorization/google`; after consent, the frontend receives the result through its configured callback or a popup message.

Interactive docs: `/swagger-ui/index.html`; OpenAPI: `/v3/api-docs`. These paths and `/actuator/health` and `/actuator/info` are available for documentation/monitoring.
