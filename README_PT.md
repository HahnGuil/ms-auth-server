# 🔐 Auth-server

Bem-vindo ao **auth-server**!  
Este projeto está em desenvolvimento 🏗️ — novas funcionalidades serão implementadas e aprimoradas com o tempo.

O **auth-server** é um servidor de autenticação responsável pelo **registro** e **login** de usuários, projetado para ser consumido por outros serviços.  
Esses serviços consumidores aplicarão uma camada adicional de autorização (por exemplo, controle de papéis), enquanto dependem deste servidor para validação das credenciais.

Centralizando a autenticação, os serviços não precisam lidar com geração de credenciais — apenas validação.

---

## 🛠️ Tecnologias

- **Back-end:** Java 24 ☕ + Spring Boot 3.5 🌱

---

## ✨ Funcionalidades

- 🔐 Autenticação com **Spring Security** e **JWT**
- 📧 Login social com **Gmail (OAuth2)**
- 🛡️ Recuperação de senha com **autenticação em duas etapas (2FA)**
- 🔑 Endpoint de **chave pública** para verificação de tokens
- 🗑️ Limpeza agendada de **códigos de verificação expirados**

---

## 🔧 Configuração do OAuth2

Para habilitar login com Gmail via OAuth2, siga os passos:

1. **Crie uma conta no Google Cloud**  
   👉 [Google Cloud Console](https://console.cloud.google.com/)

2. **Crie um novo projeto**  
   - No menu superior, clique em **Selecionar projeto** > **Novo projeto**

3. **Ative as APIs necessárias**  
   - Vá em `APIs e Serviços` → `Biblioteca`  
   - Procure por **Google People API** e clique em **Ativar**

4. **Crie credenciais OAuth 2.0**  
   - Vá em `APIs e Serviços` → `Credenciais`  
   - Clique em **Criar credenciais** → **ID do cliente OAuth 2.0**  
   - Escolha **Aplicativo Web**  
   - Adicione sua URI de redirecionamento:  
     ```
     http://localhost:8080/login/oauth2/code/google
     ```

5. **Salve as credenciais**  
   - Armazene `client_id` e `client_secret` como variáveis de ambiente

6. **Referencie no Spring Boot**  
   - No `application.properties`, adicione:
     ```properties
     spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
     spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
     ```
   📁 Arquivo: [`src/main/resources/application.properties`](src/main/resources/application-local.properties)

---

## 🚀 Deploy

Para iniciar o banco de dados PostgreSQL usando Docker:

```bash
docker compose -f docs/containers/docker-compose.postgres.yml up -d
```

📁 Arquivo: [`docs/containers/docker-compose.postgres.yml`](docs/containers/docker-compose.postgres.yml)

---

## 📘 Documentação da API

Quando a aplicação estiver rodando, um arquivo Swagger/OpenAPI será gerado automaticamente.

Você pode explorar a API usando:

- 🔗 **Swagger UI**: importe o arquivo em [https://editor.swagger.io/](https://editor.swagger.io/)
- 🧪 **Postman**: importe o arquivo `.json` como coleção
- 🌐 **Endpoint local do Swagger**:  
  ```
  http://localhost:8080/auth-server/v3/api-docs
  ```

📁 Local do arquivo Swagger: [`docs/swagger`](docs/swagger)

---

## 📨 Serviço de Email

Este projeto utiliza o [**Resend**](https://resend.com) como serviço para envio de emails em:

- 🔁 Emails de verificação de conta  
- 🔐 Códigos de autenticação em duas etapas (2FA)  
- 🔑 Links de recuperação de senha

### 🔧 Configuração

Para habilitar o Resend, siga os passos:

1. **Crie uma conta** em [resend.com](https://resend.com)  
2. **Obtenha sua API key** no dashboard do Resend  
3. **Defina a variável de ambiente**:
   ```env
   RESEND_API_KEY=sua_api_key_do_resend
   ```
4. **Configure no Spring Boot** (`application.properties` ou `application.yml`):
   ```properties
   email.api.provider=resend
   email.resend.api-key=${RESEND_API_KEY}
   email.resend.from=voce@seudominio.com
   ```

> 📬 É necessário verificar o domínio ou email remetente no Resend antes do envio.

---

### 📁 Arquivos Relacionados

- 📦 Camada de serviço de email: `src/main/java/.../service/EmailService.java`  
- ⚙️ Propriedades: `src/main/resources/application.properties`

---

## 🧩 Em Desenvolvimento

⚠️ Este projeto está em estágio inicial.  
Espere mudanças frequentes e novidades em breve! ✨

---

## 📄 Licença

Este projeto está licenciado sob a **Licença MIT**.

---

## 🇬🇧 Versão em Inglês

Para a versão em inglês deste README, acesse o arquivo [`README.md`](./README.md).
