## 🇧🇷 Portuguese Version

For the Portuguese version of this README, access the file [`README_PT.md`](./README_PT.md).

---

# 🔐 Auth-Server

## 📖 Overview

**Auth-Server** is an authentication server built with **Java 24** and **Spring Boot 3.5.3**. It uses **OAuth2** for user **registration** and **login**.

It uses **JWT Tokens** for access control to resources and issues **public keys** for token validation by applications.

**Auth-Server** centralizes and controls user login and session time, allowing other services to focus only on their specific business rules.

---

## 🛠️ Technologies

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-231F20?style=flat&logo=apache-kafka&logoColor=white)
![Resend](https://img.shields.io/badge/Resend-000000?style=flat&logo=resend&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-25A162?style=flat&logo=junit5&logoColor=white)

- **Java + Spring Boot** — Main backend with task scheduling, session control, and REST endpoints.
- **Apache Kafka** — Messaging for asynchronous processing and operation decoupling.
- **Resend** — Email platform used for sending two-factor authentication (2FA) validation codes.
- **Docker/Docker Compose** — Orchestration of development and production environments, facilitating service execution.
- **Postman** — Collections for endpoint testing and API flow documentation.
- **JUnit** — Unit testing framework for Java, used in service tests.

---

## ✨ Features

### 🔐 Authentication and Authorization
- Authentication with **Spring Security** and **JWT**
- 📧 Social login with **Gmail (OAuth2)**
- 🛡️ Password recovery with **two-factor authentication (2FA)**
- 🔑 **Public key** endpoint for token verification

### 🔒 Password Security
- Passwords must be between **8 and 12 characters** containing numbers, special characters, uppercase and lowercase letters
- Users registered via **Gmail cannot change their password** (managed by Google)

### ⏱️ Session Control
- **Automatic logout** after 30 minutes without generating new tokens
- **Single session**: does not allow the same user to be logged in from more than one location simultaneously
- Every **new login invalidates the previously generated token**
- **Automatic invalidation** of tokens after password change

### 🚨 Account Lockout Control
- Email notifications at **15, 30, and 90 days** warning about account lockout due to inactivity
- Automatic lockout of accounts without recent activity

### 🧹 Maintenance and Monitoring
- 🗑️ Scheduled cleanup of **expired verification codes**
- 📊 Implementation of **timestamped logs** for all operations, facilitating monitoring and auditing

---

## ⚙️ Environment Variables Configuration

The project uses environment variables for sensitive configurations. Use the `.env.example` file as a template.

### 📋 Variables Description

| Variable | Description |
|----------|-----------|
| `EMAIL_APPLICATION` | Application email for sending notifications |
| `EMAIL_APPLICATION_PASSWORD` | Email application password (not the regular account password) |
| `G_CLIENT_ID` | Google Cloud Client ID for using OAuth2 |
| `G_CLIENT_SECRET` | Google Cloud Client Secret for OAuth2 |
| `RESEND_API_KEY` | Resend API key for sending emails |
| `RESEND_EMAIL` | Sender email configured in Resend |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL database password (local environment) |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL database username (local environment) |

> **Note:** `G_CLIENT_ID` and `G_CLIENT_SECRET` are generated after creating OAuth 2.0 credentials in Google Cloud Console (see [OAuth2 Configuration](#-oauth2-configuration) section)

### 🐳 For use with Docker Compose

1. **Create the `.env` file** at the project root:
   ```bash
   cp .env.example .env
   ```

2. **Edit the `.env` file** and fill in your values:
   ```dotenv
   EMAIL_APPLICATION=your-email@example.com
   EMAIL_APPLICATION_PASSWORD=your-app-password
   G_CLIENT_ID=your-client-id.apps.googleusercontent.com
   G_CLIENT_SECRET=your-client-secret
   RESEND_API_KEY=re_your_resend_key
   RESEND_EMAIL=noreply@yourdomain.com
   SPRING_DATASOURCE_PASSWORD=your-db-password
   SPRING_DATASOURCE_USERNAME=your-db-username
   ```

3. **Run Docker Compose** (it will automatically load the `.env` file):
   ```bash
   docker compose up -d
   ```

### 💻 For local execution (outside container)

You can configure environment variables directly in your operating system or IDE.

#### 🍎 macOS / Linux

**Temporary (current terminal session only):**
```bash
export EMAIL_APPLICATION="your-email@example.com"
export EMAIL_APPLICATION_PASSWORD="your-app-password"
export G_CLIENT_ID="your-client-id.apps.googleusercontent.com"
export G_CLIENT_SECRET="your-client-secret"
export RESEND_API_KEY="re_your_resend_key"
export RESEND_EMAIL="noreply@yourdomain.com"
export SPRING_DATASOURCE_PASSWORD="your-db-password"
export SPRING_DATASOURCE_USERNAME="your-db-username"
```

**Permanent (add to `~/.zshrc` or `~/.bashrc`):**
```bash
# Open the file
nano ~/.zshrc  # or ~/.bashrc for bash

# Add the variables at the end of the file
export EMAIL_APPLICATION="your-email@example.com"
export EMAIL_APPLICATION_PASSWORD="your-app-password"
# ... add all variables

# Reload the file
source ~/.zshrc  # or source ~/.bashrc
```

#### 🪟 Windows

**PowerShell (temporary):**
```powershell
$env:EMAIL_APPLICATION="your-email@example.com"
$env:EMAIL_APPLICATION_PASSWORD="your-app-password"
$env:G_CLIENT_ID="your-client-id.apps.googleusercontent.com"
$env:G_CLIENT_SECRET="your-client-secret"
$env:RESEND_API_KEY="re_your_resend_key"
$env:RESEND_EMAIL="noreply@yourdomain.com"
$env:SPRING_DATASOURCE_PASSWORD="your-db-password"
$env:SPRING_DATASOURCE_USERNAME="your-db-username"
```

**Permanent (System Variables):**
1. Press `Win + R`, type `sysdm.cpl` and press Enter
2. Go to the **Advanced** tab → **Environment Variables**
3. Under **User variables**, click **New**
4. Add each variable with its name and value
5. Click **OK** and restart the terminal/IDE

#### 🔧 IntelliJ IDEA

1. **Open run configurations:**
   - Go to `Run` → `Edit Configurations...`

2. **Add environment variables:**
   - Find the **Environment variables** section
   - Click the folder icon 📁 to open the editor
   - Add each variable in `NAME=value` format
   - Or paste all at once separated by semicolon (`;` on Windows or `:` on macOS/Linux):
     ```
     EMAIL_APPLICATION=your-email@example.com;EMAIL_APPLICATION_PASSWORD=your-app-password;G_CLIENT_ID=your-client-id;G_CLIENT_SECRET=your-secret;RESEND_API_KEY=re_your_key;RESEND_EMAIL=noreply@yourdomain.com;SPRING_DATASOURCE_PASSWORD=password;SPRING_DATASOURCE_USERNAME=username
     ```

3. **Save and run** the project

#### 🌱 Spring Tool Suite (STS) / Eclipse

1. **Right-click** on the project → **Run As** → **Run Configurations...**

2. **Select the configuration** for the Spring Boot application

3. **Go to the Environment tab**

4. **Click Add** and add each variable:
   - Name: `EMAIL_APPLICATION`
   - Value: `your-email@example.com`
   - Repeat for all variables

5. **Click Apply** then **Run**

---

## 🔧 OAuth2 Configuration

To enable Gmail login via OAuth2, follow these steps:

1. **Create a Google Cloud account**  
   👉 [Google Cloud Console](https://console.cloud.google.com/)

2. **Create a new project**  
   - In the top menu, click **Select project** > **New project**

3. **Enable required APIs**  
   - Go to `APIs & Services` → `Library`  
   - Search for **Google People API** and click **Enable**

4. **Create OAuth 2.0 credentials**  
   - Go to `APIs & Services` → `Credentials`  
   - Click **Create credentials** → **OAuth 2.0 Client ID**  
   - Choose **Web application**  
   - Add your redirect URI:  
     ```
     http://localhost:8080/login/oauth2/code/google
     http://localhost:2300/auth-server/login/oauth2/code/google
     http://localhost:2310/auth-server/login/oauth2/code/google
     ```

5. **Save the credentials**  
   - Store `client_id` and `client_secret` as environment variables

6. **Reference in Spring Boot**  
   - In `application-local.yml` and `application-docker.yml`, add:
     ```yaml
     spring:
       security:
         oauth2:
           client:
             registration:
               google:
                 client-id: "${G_CLIENT_ID}"
                 client-secret: "${G_CLIENT_SECRET}"
     ```
   📁 Files: 
   - [`src/main/resources/application-local.yml`](src/main/resources/application-local.yml)
   - [`src/main/resources/application-docker.yml`](src/main/resources/application-docker.yml)

---

## 🚀 Deploy

### 📦 Running with Docker Compose

The project uses Docker Compose to orchestrate all necessary services: **PostgreSQL**, **Kafka**, **Zookeeper**, and the **Auth-Server** application.

#### Prerequisites
- Docker and Docker Compose installed
- `.env` file configured (see [Environment Variables Configuration](#️-environment-variables-configuration))

#### 🔧 Deploy Steps

1. **Clone the repository** (if you haven't already):
   ```bash
   git clone <repository-url>
   cd auth-server
   ```

2. **Configure the `.env` file**:
   ```bash
   cp .env.example .env
   ```
   Then edit `.env` with your credentials.

3. **Build and start all services**:
   ```bash
   docker compose up -d --build
   ```
   
   > 💡 This command will automatically start all necessary services:
   > - **PostgreSQL** (port 5432 for Docker / 5050 for local)
   > - **Zookeeper** (port 2181)
   > - **Kafka** (ports 9092 and 9093)
   > - **Auth-Server** (port 2300)

4. **Check if containers are running**:
   ```bash
   docker compose ps
   ```
   
   You should see the containers: `postgres-auth`, `postgres-auth-local`, `zookeeper`, `kafka`, and `ms-auth-server`

5. **Follow the logs** (optional):
   ```bash
   docker compose logs -f ms-auth-server
   ```

#### 🛑 Stop services

To stop all containers:
```bash
docker compose down
```

To stop and remove volumes (⚠️ deletes database data):
```bash
docker compose down -v
```

#### 🔄 Rebuild the application

If you made changes to the code and need to rebuild:
```bash
docker compose up -d --build ms-auth-server
```

#### 📍 Endpoints after Deploy

- **API Auth-Server**: `http://localhost:2300`
- **Swagger Docs**: `http://localhost:2300/auth-server/v3/api-docs`
- **PostgreSQL (Docker)**: `localhost:5432`
- **PostgreSQL (Local)**: `localhost:5050`
- **Kafka**: `localhost:9093`

📁 File: [`docker-compose.yml`](docker-compose.yml)

---

### 💻 Running Locally (without Docker)

If you prefer to run the application directly on your machine:

1. **Configure environment variables** (see [configuration section](#️-environment-variables-configuration))

2. **Start only the databases with Docker**:
   ```bash
   docker compose up -d postgres-auth-local
   ```

3. **Run the application with Maven**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```
   
   Or with Maven installed:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. **Or run via IDE** (IntelliJ/Eclipse) with the `local` profile active

---

## 📘 API Documentation

The application provides interactive documentation via **Swagger UI** and endpoints for **Health Check**.

### 🌐 Accessing Swagger UI

After starting the application, access the Swagger interface at the following addresses:

**Local Environment** (port 2310):
```
http://localhost:2310/auth-server/swagger-ui/index.html
```

**Docker Environment** (port 2300):
```
http://localhost:2300/auth-server/swagger-ui/index.html
```

### 🏥 Health Check

Check the application status through the Actuator endpoint:

**Local Environment**:
```
http://localhost:2310/auth-server/actuator/health
```

**Docker Environment**:
```
http://localhost:2300/auth-server/actuator/health
```

### 📥 Importing to Postman

To use the API in Postman, follow these steps:

1. **Access Swagger UI** using the links above

2. **Export the JSON documentation**:
   - In the Swagger UI interface, look for the `/v3/api-docs` link or
   - Access directly:
     - Local: `http://localhost:2310/auth-server/v3/api-docs`
     - Docker: `http://localhost:2300/auth-server/v3/api-docs`

3. **Save the JSON content** to a file (e.g., `auth-server-api.json`)

4. **Import into Postman**:
   - Open Postman
   - Click **Import** in the upper left corner
   - Select the saved JSON file
   - Postman will automatically create a collection with all endpoints

> 💡 **Alternative**: You can also use the static Swagger file available at [`src/main/resources/static/swagger.yml`](src/main/resources/static/swagger.yml)

### 📁 Documentation Files

- **Static Swagger YAML**: [`src/main/resources/static/swagger.yml`](src/main/resources/static/swagger.yml)
- **Additional documentation**: [`docs/swagger/`](docs/swagger/)

---

## 📨 Email Service

This project uses [**Resend**](https://resend.com) as an email service for:

- 🔁 Account verification emails  
- 🔐 Two-factor authentication (2FA) codes  
- 🔑 Password recovery links
- ⏰ Account lockout notifications due to inactivity

### 🌐 Step 1: Register a Domain

To send professional emails, you need your own domain.

#### Registration Options:

1. **Registro.br** (`.br` domains): [https://registro.br](https://registro.br)
2. **GoDaddy**: [https://godaddy.com](https://godaddy.com)
3. **Namecheap**: [https://namecheap.com](https://namecheap.com)
4. **Google Domains**: [https://domains.google](https://domains.google)
5. **Cloudflare Registrar**: [https://cloudflare.com/products/registrar](https://cloudflare.com/products/registrar)

> 💡 **Tip**: Cloudflare Registrar offers competitive prices without additional markup.

#### How to register:

1. Access one of the sites above
2. Search for the desired domain (e.g., `mydomain.com`)
3. Add to cart and complete the purchase
4. After purchase, you will receive access to the DNS management panel

---

### ☁️ Step 2: Configure Cloudflare

**Cloudflare** offers free DNS management, better performance, and security for your domain.

#### 2.1 - Create Cloudflare account

1. Access [https://dash.cloudflare.com/sign-up](https://dash.cloudflare.com/sign-up)
2. Create your free account

#### 2.2 - Add your domain to Cloudflare

1. In the Cloudflare panel, click **"Add a Site"**
2. Enter your domain (e.g., `mydomain.com`)
3. Choose the **Free** plan
4. Click **"Continue"**

#### 2.3 - Update Nameservers

Cloudflare will provide two custom nameservers, something like:

```
alice.ns.cloudflare.com
bob.ns.cloudflare.com
```

**Now you need to update the nameservers at your registrar:**

1. **Access the registrar panel** where you bought the domain (Registro.br, GoDaddy, etc.)
2. **Find the Nameservers section** (or DNS/Name Servers)
3. **Replace the default nameservers** with those provided by Cloudflare
4. **Save changes**

> ⏱️ **Attention**: DNS propagation can take from a few minutes to 48 hours.

#### 2.4 - Verify activation

1. Return to the Cloudflare panel
2. Wait until the status changes to **"Active"**
3. You will receive an email confirming activation

---

### 📧 Step 3: Configure Resend

**Resend** is the platform that will send the application's emails.

#### 3.1 - Create Resend account

1. Access [https://resend.com/signup](https://resend.com/signup)
2. Create your account (free plan allows **3,000 emails/month**)

#### 3.2 - Get the API Key

1. In the Resend dashboard, go to **"API Keys"**
2. Click **"Create API Key"**
3. Give the key a name (e.g., `auth-server-production`)
4. Copy the generated key (it will only appear once)
5. Save in the `.env` file:
   ```env
   RESEND_API_KEY=re_your_key_here
   ```

#### 3.3 - Add and verify your domain

1. In the Resend dashboard, go to **"Domains"**
2. Click **"Add Domain"**
3. Enter your domain (e.g., `mydomain.com`)
4. Resend will provide DNS records for configuration

#### 3.4 - Configure DNS records in Cloudflare

Resend will provide **3 types of DNS records**:

**1. SPF Record (TXT)**
```
Type: TXT
Name: @
Content: v=spf1 include:_spf.resend.com ~all
TTL: Auto
```

**2. DKIM Record (TXT)**
```
Type: TXT
Name: resend._domainkey
Content: [value provided by Resend]
TTL: Auto
```

**3. DMARC Record (TXT)** - Optional but recommended
```
Type: TXT
Name: _dmarc
Content: v=DMARC1; p=none; rua=mailto:dmarc@yourdomain.com
TTL: Auto
```

**How to add in Cloudflare:**

1. Go to the Cloudflare panel
2. Select your domain
3. Click **"DNS"** in the sidebar
4. Click **"Add record"**
5. For each record provided by Resend:
   - Select the **Type** (TXT)
   - Enter the **Name** (e.g., `@`, `resend._domainkey`, `_dmarc`)
   - Paste the **Content** exactly as provided
   - Keep **Proxy status** as **"DNS only"** (gray cloud icon)
   - Click **"Save"**

#### 3.5 - Verify domain in Resend

1. After adding all DNS records in Cloudflare
2. Return to the Resend dashboard
3. Click **"Verify"** next to your domain
4. If the records are correct, the status will change to **"Verified"** ✅

> ⏱️ **Note**: It may take a few minutes for DNS records to propagate.

#### 3.6 - Configure sender email

After verifying the domain, define the email that will be used for sending:

```env
RESEND_EMAIL=noreply@yourdomain.com
```

> 💡 **Tip**: Use emails like `noreply@`, `notifications@`, or `auth@` for automated communications.

#### 3.7 - Test sending

You can test sending directly in the Resend panel:

1. Go to **"Emails"** → **"Send test email"**
2. Enter a destination email
3. Click **"Send"**
4. Check if the email arrived (also check the spam folder)

---

### ✅ Step 4: Configure in the Project

Make sure the `.env` file is configured correctly:

```env
# Resend Configuration
RESEND_API_KEY=re_your_resend_key_here
RESEND_EMAIL=noreply@yourdomain.com
```

The configurations are already ready in `application-local.yml` and `application-docker.yml`:

```yaml
resend:
  api:
    key: "${RESEND_API_KEY}"
    url: "https://api.resend.com/emails"
  sender: "${RESEND_EMAIL}"
```

Start the application and emails will be automatically sent by Resend! 🚀

---

### 🔍 Verification and Troubleshooting

#### How to test if it's working:

1. **Register a new user** in the application
2. **Check if the verification email** was sent
3. **Check the logs** of the application for possible errors
4. **Access the Resend dashboard** to see the sending history

#### Common problems:

❌ **Error: "Domain not verified"**
- Check if all DNS records were added correctly in Cloudflare
- Wait for DNS propagation (can take up to 48 hours)
- Use tools like [MXToolbox](https://mxtoolbox.com/SuperTool.aspx) to verify records

❌ **Emails going to spam**
- Configure the DMARC record
- Add a valid DKIM record
- Avoid suspicious content in emails
- Warm up the domain by sending few emails initially

❌ **Invalid API Key**
- Check if the key is correct in the `.env` file
- Generate a new key in the Resend panel if necessary

---

### 📁 Related Files

- 📦 Email service layer: `src/main/java/.../service/EmailService.java`  
- ⚙️ Configurations: 
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-docker.yml`

---

## 📄 License

This project is licensed under the **MIT License**.

---

## 🚀 Future Implementations

This project follows a structured roadmap of features planned for upcoming versions.

### 📊 About Versioning

The project uses **Semantic Versioning** in the format `MAJOR.MINOR.PATCH`:

- **Version 1.0.0**: First stable version with all features described in the [Features](#-features) section
- **Versions X.Y.0** (e.g., 1.1.0, 1.2.0): Small fixes and improvements, **without new features**
- **Versions X.0.0** (e.g., 2.0.0, 3.0.0): Include important **new features**
- **Versions X.Y.Z** (e.g., 2.1.1, 3.0.2): Small updates, bug fixes, and security patches

---

### 🎯 Features Roadmap

#### 📦 Version 1.1.0
**Social Authentication Expansion**

- 🔐 Login via **Microsoft** (Azure AD / Microsoft Account)
- 🍎 Login via **Apple** (Sign in with Apple)
- 🐙 Login via **GitHub** (OAuth2)
- 🔑 Creation and management of **application tokens** (API Keys)
- 📋 Dashboard for viewing and revoking active tokens

---

#### 📦 Version 2.0.0
**Administrative Notifications and Auditing**

- 📧 Email notification to **administrators** when there are:
  - User password changes
  - Suspicious access attempts
  - Account lockouts due to inactivity
- 📝 Complete audit system with action history
- 👥 Administrator profile management

---

#### 📦 Version 3.0.0
**Reports and Metrics**

- 📊 Automatic sending of **monthly reports** via email containing:
  - 📈 Number of new registrations
  - 🔐 Total logins performed
  - ✅ Number of active users
  - ❌ Number of inactive users
  - 📉 Conversion and retention rates
- 📉 Real-time metrics dashboard
- 📅 Custom report generation by period

---

### 💡 Suggestions and Contributions

Have any ideas to improve **Auth-Server**? 

- Send an email to guilherme.f.h@hotmail.com with the subject **Auth-Server**

**Thank you for your interest** ✨

Best regards,  
**Guilherme Hahn** - [GitHub](https://github.com/HahnGuil)

---

## 🇧🇷 Portuguese Version

For the Portuguese version of this README, access the file [`README_PT.md`](./README_PT.md).

