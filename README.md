# Auth-Server

Authentication service built with Java 24, Spring Boot, and JWT. This guide explains how to run it locally; detailed endpoint documentation is available in [docs/documentation/EN](docs/documentation/EN/).

## Run locally

### Requirements

- JDK 24
- Docker with Docker Compose
- Google OAuth 2.0 credentials for `G_CLIENT_ID` and `G_CLIENT_SECRET`. The current Spring configuration requires these variables at startup; use real credentials to enable Google login.

### Steps

1. Clone the project and enter its directory:

   ```bash
   git clone https://github.com/HahnGuil/ms-auth-server.git
   cd ms-auth-server
   ```

   If the repository is already cloned, open its root directory.

2. Create the local configuration file:

   ```bash
   cp .env.example .env
   ```

   Edit `.env` and set `G_CLIENT_ID` and `G_CLIENT_SECRET` to your Google OAuth credentials. Add this redirect URI to the Google OAuth client:

   ```text
   http://localhost:2310/auth-server/login/oauth2/code/google
   ```

   The example already provides development credentials for the local PostgreSQL database. Do not use them in production. See [Google OAuth setup](#google-oauth-setup) for details.

3. Start local PostgreSQL with Docker Compose:

   ```bash
   docker compose -f docker-compose.local.yml up -d --wait
   ```

   The `authdb-service-local` database is available at `localhost:5050`. The application runs database migrations on startup.

4. On macOS/Linux, load `.env` variables and start the application:

   ```bash
   set -a
   . ./.env
   set +a
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

   Keep this terminal open. The `local` profile uses port `2310`.

5. Check that the application started:

   ```bash
   curl http://localhost:2310/auth-server/actuator/health
   ```

   Swagger UI: <http://localhost:2310/auth-server/swagger-ui/index.html>

   OpenAPI: <http://localhost:2310/auth-server/v3/api-docs>

To stop, press `Ctrl+C` in the application terminal. Stop the database when needed:

```bash
docker compose -f docker-compose.local.yml down
```

This command preserves the local database volume.

## Google OAuth setup

To enable Google login, create **Web application** OAuth 2.0 credentials in the [Google Cloud Console](https://console.cloud.google.com/) and register the redirect URI shown above. Copy the Client ID and Client Secret into `G_CLIENT_ID` and `G_CLIENT_SECRET` in `.env`.

## Docker environments

To run the application in a container as well, set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` in `.env`, then run:

```bash
docker compose -f docker-compose.docker.yml up -d --build
```

This environment publishes the API at `http://localhost:2300/auth-server`. The `local` compose file starts only the database; for development, run the application with Maven using the steps above.

## Documentation

- [Endpoints and flows](docs/documentation/EN/endpoints-and-flows.md)
- [Business rules](docs/documentation/EN/business-rules.md)
- [Integration guide](docs/documentation/EN/integration-guide.md)
- [Local Swagger UI](http://localhost:2310/auth-server/swagger-ui/index.html)
- [Local health check](http://localhost:2310/auth-server/actuator/health)
