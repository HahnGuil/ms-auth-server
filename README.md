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
- 
---

## 🚀 Deployment

To start the PostgreSQL database using Docker, run the following command:

```bash
docker compose -f docs/containers/docker-compose.postgres.yml up -d
```

📁 File location: [`docs/containers/docker-compose.postgres.yml`](docs/containers/docker-compose.postgres.yml)

---

## 📘 API Documentation

Once the application is running, a Swagger/OpenAPI JSON file will be automatically generated.

📁 Location: [`docs/swagger`](docs/swagger)

You can explore and test the API using:

- 🔗 **Swagger UI**: Import the file at [https://editor.swagger.io/](https://editor.swagger.io/)
- 🧪 **Postman**: Import the `.json` file directly as a collection
- 🌐 **Local Swagger endpoint** (when app is running):
  ```bash
  http://localhost:8080/v3/api-docs
  ```

> The Swagger file will be saved automatically after starting the application.

---

## 🧩 Work in Progress

⚠️ This project is in an early stage.  
Expect frequent changes and new features coming soon! ✨

---

## 📄 License

This project is licensed under the MIT License.  

