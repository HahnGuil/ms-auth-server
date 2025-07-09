# 🔐 Auth-server

Welcome to **auth-server**!  
This project is currently under construction 🏗️ — new features will be implemented and improved over time.

The **auth-server** will be responsible for handling authentication across other services that are yet to be developed.

---

## 🛠️ Tech Stack

- **Back-end:** Java 24 ☕ + Spring Boot 3.5 🌱

---

## ✨ Features

- 🔐 Authentication using **Spring Security** with **JWT**
- 📧 Integration with **OAuth2** for authentication **Gmail**
---

---

## 🔧 OAuth 2 Configuration

To enable Gmail login using OAuth 2, follow these steps:

1. **Create a Google Cloud account** (if you don't already have one):
  - Visit [Google Cloud Console](https://console.cloud.google.com/).

2. **Create a new project** (if you don't already have one):
  - Navigate to the "Select a Project" dropdown and click "New Project."

3. **Enable APIs and Services**:
  - Go to **APIs & Services** > **Library**.
  - Search for "Google People API" and enable it.

4. **Create OAuth 2.0 Credentials**:
  - Go to **APIs & Services** > **Credentials**.
  - Click **Create Credentials** > **OAuth 2.0 Client ID**.
  - Select **Web Application** as the application type.
  - Add your application's redirect URI (e.g., `http://localhost:8080/login/oauth2/code/google`).

5. **Save the credentials**:
  - Save the `client_id` and `client_secret` in environment variables.

6. **Configure the application**:
  - Reference the environment variables in your Spring Boot application properties file.

  - Example configuration in `application.properties`:

    📁  File location ['src/main/resources/application.properties'](src/main/resources/application.properties)
---

## 🚀 Deployment

To start the PostgreSQL database using Docker, run the following command:
```bash
     docker compose -f docs/containers/docker-compose.postgres.yml up -d
```
\
📁  File location: [`docs/containers/docker-compose.postgres.yml`](docs/containers/docker-compose.postgres.yml)
---


## 📘 API Documentation

Once the application is running, a Swagger/OpenAPI JSON file will be automatically generated.

You can explore and test the API using:

- 🔗 **Swagger UI**: Import the file at [https://editor.swagger.io/](https://editor.swagger.io/)
- 🧪 **Postman**: Import the `.json` file directly as a collection
- 🌐 **Local Swagger endpoint** (when app is running):
  ```bash
  http://localhost:8080/auth-server/v3/api-docs
  ```
 The Swagger file will be saved automatically after starting the application.
 📁 Location: [`docs/swagger`](docs/swagger)

---

## 🧩 Work in Progress

⚠️ This project is in an early stage.  
Expect frequent changes and new features coming soon! ✨

---

## 📄 License

This project is licensed under the MIT License.  

