# 🔐 Auth-server

Welcome to **auth-server**!  
This project is currently under construction 🏗️ — new features will be implemented and improved over time.

The **auth-server** is an authentication server responsible for **user registration** and **login**, designed to be consumed by other services.  
These consuming services will apply an additional layer of authorization (e.g., user roles), while relying on this server for credential validation.

By centralizing authentication, services don’t need to handle credential generation — only validation.

---

## 🛠️ Tech Stack

- **Back-end:** Java 24 ☕ + Spring Boot 3.5 🌱

---

## ✨ Features

- 🔐 Authentication using **Spring Security** and **JWT**
- 📧 Social login with **Gmail (OAuth2)**
- 🛡️ Password recovery with **two-factor authentication**
- 🔑 **Public key** endpoint for token verification
- 🗑️ Scheduled cleanup of **expired verification codes**

---

## 🔧 OAuth2 Configuration

To enable login with Gmail using OAuth2, follow these steps:

1. **Create a Google Cloud account**  
   👉 [Google Cloud Console](https://console.cloud.google.com/)

2. **Create a new project**
    - In the top nav, click **Select a Project** > **New Project**

3. **Enable required APIs**
    - Go to `APIs & Services` → `Library`
    - Search for **Google People API** and click **Enable**

4. **Create OAuth 2.0 Credentials**
    - Navigate to `APIs & Services` → `Credentials`
    - Click **Create Credentials** → **OAuth 2.0 Client ID**
    - Choose **Web Application**
    - Add your redirect URI:
      ```
      http://localhost:8080/login/oauth2/code/google
      ```

5. **Save credentials**
    - Store `client_id` and `client_secret` as environment variables

6. **Reference in Spring Boot**
    - In `application.properties`, add:
      ```properties
      spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
      spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
      ```
   📁 File: [`src/main/resources/application.properties`](src/main/resources/application.properties)

---

## 🚀 Deployment

To start the PostgreSQL database using Docker:

```bash
  docker compose -f docs/containers/docker-compose.postgres.yml up -d
```

📁 File: [`docs/containers/docker-compose.postgres.yml`](docs/containers/docker-compose.postgres.yml)

---

## 📘 API Documentation

When the application is running, a Swagger/OpenAPI spec will be generated automatically.

You can explore the API using:

- 🔗 **Swagger UI**: Import the file at [https://editor.swagger.io/](https://editor.swagger.io/)
- 🧪 **Postman**: Import the `.json` file as a collection
- 🌐 **Local Swagger endpoint**:
  ```
  http://localhost:8080/auth-server/v3/api-docs
  ```

📁 Swagger file location: [`docs/swagger`](docs/swagger)

---

## 🧩 Work in Progress

⚠️ This project is in early development.  
Frequent changes and new features are expected! ✨

---

## 📄 License

This project is licensed under the **MIT License**.


---

## 📨 Email Service

This project uses [**Resend**](https://resend.com) as the email delivery service for:

- 🔁 Account verification emails
- 🔐 Two-factor authentication (2FA) codes
- 🔑 Password recovery links

### 🔧 Configuration

To enable Resend, follow these steps:

1. **Create an account** at [resend.com](https://resend.com)
2. **Get your API key** from the Resend dashboard
3. **Set the environment variable**:
   ```env
   RESEND_API_KEY=your_resend_api_key_here
   ```
4. **Configure in Spring Boot** (`application.properties` or `application.yml`):
   ```properties
   email.api.provider=resend
   email.resend.api-key=${RESEND_API_KEY}
   email.resend.from=you@yourdomain.com
   ```

> 📬 You must verify the `from` domain or email in Resend before sending.

---

### 📁 Related Files

- 📦 Email Service Layer: `src/main/java/.../service/EmailService.java`
- ⚙️ Properties: `src/main/resources/application.properties`
