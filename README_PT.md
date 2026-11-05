# Auth-Server

Serviço de autenticação com Java 24, Spring Boot e JWT. Este guia mostra como executar a aplicação localmente; os endpoints detalhados estão em [docs/documentation/PT](docs/documentation/PT/).

## Executar localmente

### Pré-requisitos

- JDK 24
- Docker com Docker Compose
- Credenciais OAuth 2.0 do Google para preencher `G_CLIENT_ID` e `G_CLIENT_SECRET`. A configuração atual do Spring exige essas variáveis na inicialização; use credenciais reais para habilitar o login Google.

### Passos

1. Clone o projeto e entre na pasta:

   ```bash
   git clone https://github.com/HahnGuil/ms-auth-server.git
   cd ms-auth-server
   ```

   Se o repositório já estiver clonado, apenas abra a pasta raiz dele.

2. Crie o arquivo local de configuração:

   ```bash
   cp .env.example .env
   ```

   Edite `.env` e preencha `G_CLIENT_ID` e `G_CLIENT_SECRET` com credenciais Google OAuth. Configure o URI de redirecionamento:

   ```text
   http://localhost:2310/auth-server/login/oauth2/code/google
   ```

   O usuário e a senha do PostgreSQL local já têm valores de desenvolvimento no exemplo. Não use esses valores em produção. Veja [como configurar o Google OAuth](#configuração-do-google-oauth).

3. Inicie o PostgreSQL local pelo Docker Compose:

   ```bash
   docker compose -f docker-compose.local.yml up -d --wait
   ```

   O banco `authdb-service-local` fica disponível em `localhost:5050`. As migrações são executadas pela aplicação na inicialização.

4. No macOS/Linux, carregue as variáveis do `.env` e inicie a aplicação:

   ```bash
   set -a
   . ./.env
   set +a
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

   Mantenha esse terminal aberto. O profile `local` usa a porta `2310`.

5. Confirme que iniciou:

   ```bash
   curl http://localhost:2310/auth-server/actuator/health
   ```

   Swagger UI: <http://localhost:2310/auth-server/swagger-ui/index.html>

   OpenAPI: <http://localhost:2310/auth-server/v3/api-docs>

Para encerrar, use `Ctrl+C` no terminal da aplicação e, se desejar parar o banco:

```bash
docker compose -f docker-compose.local.yml down
```

Esse comando mantém os dados do volume local.

## Configuração do Google OAuth

Para habilitar login Google, crie credenciais OAuth 2.0 do tipo **Web application** no [Google Cloud Console](https://console.cloud.google.com/) e cadastre o URI de redirecionamento mostrado acima. Copie o Client ID e o Client Secret para as variáveis `G_CLIENT_ID` e `G_CLIENT_SECRET` no `.env`.

## Ambientes Docker

Para executar também a aplicação em container, configure no `.env` `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`, então rode:

```bash
docker compose -f docker-compose.docker.yml up -d --build
```

Esse ambiente publica a API em `http://localhost:2300/auth-server`. O compose `local` inicia somente o banco; para desenvolvimento, execute a aplicação com Maven conforme os passos acima.

## Documentação

- [Endpoints e fluxos](docs/documentation/PT/fluxos-endpoints.md)
- [Regras de negócio](docs/documentation/PT/regras-negocio.md)
- [Guia de integração](docs/documentation/PT/guia-integracao.md)
- [Swagger UI local](http://localhost:2310/auth-server/swagger-ui/index.html)
- [Health check local](http://localhost:2310/auth-server/actuator/health)
