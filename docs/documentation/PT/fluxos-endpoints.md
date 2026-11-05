# Endpoints e fluxos

Base local: `http://localhost:2310/auth-server`. Em produção, use o host do serviço seguido de `/auth-server`. Endpoints protegidos recebem `Authorization: Bearer <token>`. Respostas de erro normalmente têm `message` e `timestamp`.

## Endpoints

| Método e caminho | Entrada | Resposta e uso |
|---|---|---|
| `POST /users` | JSON: `username`, `email`, `password`, `firstName`, `lastName`, `typeUser` (`DIRECT_USER` ou `OAUTH_USER`) e `applicationCode` (ID numérico da aplicação). | `201`, `userId`, `userName`, `email`, `token` e `refreshToken`. Cria ou associa uma conta com senha a uma aplicação. |
| `POST /login` | JSON: `applicationPublicId` (UUID), `email` e `password`. | `201`, `userName`, `email`, `token` e `refreshToken`. Login de conta com senha. |
| `GET /oauth2/authorization/google` | Redireciona ao Google; não recebe corpo. O Google retorna a `/login/oauth2/code/google`. | Após autenticar, envia os tokens ao popup via `postMessage` (`google-login`) ou redireciona ao callback do frontend com `data` codificado na URL. |
| `POST /application` | Header `publicId` (UUID); sem corpo. | `201`, `applicationToken` e `refreshApplicationToken`. Emite tokens para a aplicação cadastrada. |
| `POST /token` | Header `Authorization: Bearer <refreshToken>`; sem corpo. | `201`, novo `LoginResponse`. Renova tokens de usuário. |
| `POST /token/application` | Header `Authorization: Bearer <refreshApplicationToken>` e `publicId` (UUID). | `201`, novos tokens de aplicação. |
| `DELETE /login` | Bearer de acesso e JSON `{ "email": "pessoa@exemplo.com" }`. A sessão encerrada é identificada pelo token. | `204`, sem corpo. Faz logout e invalida a sessão/token ativo. |
| `POST /application/register` | Bearer de usuário administrador; JSON com `nameApplication`, `applicationEmail`, `applicationDomain`, `resendApiKey`, `definePasswordPolicy` e, opcionalmente, `useDefaultPasswordPolicy`/`passwordExpirationMonths`. | `201`, dados da aplicação e `applicationPublicId`. Cadastra uma aplicação. |
| `POST /users/register` | Bearer de usuário e header `applicationPublicId` (UUID). | `201`, mensagem de sucesso. Associa usuário OAuth à aplicação; para usuário direto a associação não é necessária. |
| `PATCH /users` | Bearer de usuário e header `applicationPublicID` (UUID). | `204`, sem corpo. Atualiza o papel do usuário naquela aplicação quando ele já está associado. |
| `GET /users/me` | Bearer de usuário. | `200`, `userName`, `fullName` e `email` do usuário autenticado. |
| `PATCH /users/username` | Bearer de usuário; JSON `{ "userName": "novo_nome" }`. | `204`, sem corpo. Altera o nome de usuário. |
| `PATCH /password/change-password` | Bearer de acesso; JSON: `applicationPublicId`, `email`, `oldPassword`, `newPassword`. | `204`, sem corpo. Altera a senha da aplicação atual. |
| `POST /password/reset-request` | JSON: `email`, `applicationPublicId`. | `200`, mensagem; envia código de recuperação por e-mail. |
| `POST /password/validate-code` | JSON: `applicationPublicId`, `email`, `recoveryCode`. | `201`, `recoverToken`. Valida o código recebido por e-mail. |
| `PATCH /password/reset-password` | Bearer `recoverToken`; JSON `{ "newPassword": "..." }`. | `204`, sem corpo. Define a nova senha. |
| `GET /public-key/jwks` | Sem autenticação. | `200`, conjunto JWKS (`keys`) para validar assinaturas JWT. É a opção recomendada para consumidores. |
| `GET /public-key/legacy` | Requer autenticação segundo a configuração de segurança atual. | Mapa de `kid` para chave pública codificada em Base64 no formato legado. |

As senhas de cadastro, login e alteração devem ter 8–12 caracteres, ao menos um dígito, uma letra maiúscula e um caractere especial. `applicationCode` em `/users` é um ID numérico; os demais fluxos usam o UUID público `applicationPublicId`.

## Exemplos de fluxo

**Cadastro e login com senha**

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

Use o `token` de acesso nas chamadas autenticadas e guarde o `refreshToken` para `POST /token`. O login exige o UUID público da aplicação:

```json
{
  "applicationPublicId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "email": "ana@example.com",
  "password": "SecurePass1!"
}
```

**Recuperação de senha:** `POST /password/reset-request` envia o código; envie o código e a aplicação para `POST /password/validate-code`; use o `recoverToken` retornado como Bearer em `PATCH /password/reset-password`.

**OAuth Google:** abra `GET /oauth2/authorization/google`; após o consentimento, o frontend recebe o resultado no callback configurado ou por mensagem ao popup.

Documentação interativa: `/swagger-ui/index.html`; OpenAPI: `/v3/api-docs`. Esses caminhos e `/actuator/health` e `/actuator/info` estão disponíveis para documentação/monitoramento.
