# 🔐 Auth-Server

## 📖 Apresentação

O **Auth-Server** é um servidor de autenticação desenvolvido com **Java 24** e **Spring Boot 3.5.3**. Ele utiliza **OAuth2** para realizar o **registro** e o **login** de usuários.

Utiliza **Tokens JWT** para o controle de acessos a recursos e emite **chaves públicas** para validação de tokens pelas aplicações.

O **Auth-Server** centraliza e controla o login de usuários e o tempo de sessão, permitindo que os outros serviços cuidem somente de suas específicas regras de negócio.

---

## 🔗 Referências da API

### URLs de Execução

- **Auth-Server local**: [`http://localhost:2310/auth-server`](http://localhost:2310/auth-server)
- **Auth-Server Docker**: [`http://localhost:2300/auth-server`](http://localhost:2300/auth-server)
- **Auth-Server produção**: [`https://auth.toxicbet.com.br/auth-server`](https://auth.toxicbet.com.br/auth-server)
- **Frontend local**: [`http://localhost:4200`](http://localhost:4200)
- **Toxic Bet API local**: [`http://localhost:10000`](http://localhost:10000)
- **Toxic Bet API Docker**: [`http://localhost:20000`](http://localhost:20000)
- **Toxic Bet API produção**: [`https://api.toxicbet.com.br`](https://api.toxicbet.com.br)

### URLs de Documentação e Segurança

- **Swagger UI local**: [`http://localhost:2310/auth-server/swagger-ui/index.html`](http://localhost:2310/auth-server/swagger-ui/index.html)
- **Swagger UI Docker**: [`http://localhost:2300/auth-server/swagger-ui/index.html`](http://localhost:2300/auth-server/swagger-ui/index.html)
- **OpenAPI JSON local**: [`http://localhost:2310/auth-server/v3/api-docs`](http://localhost:2310/auth-server/v3/api-docs)
- **OpenAPI JSON Docker**: [`http://localhost:2300/auth-server/v3/api-docs`](http://localhost:2300/auth-server/v3/api-docs)
- **Login Google OAuth2**: [`https://auth.toxicbet.com.br/auth-server/oauth2/authorization/google`](https://auth.toxicbet.com.br/auth-server/oauth2/authorization/google)
- **JWK set para validação JWT**: [`https://auth.toxicbet.com.br/auth-server/public-key/jwks`](https://auth.toxicbet.com.br/auth-server/public-key/jwks)

---

## 🛠️ Tecnologias

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![Kafka](https://img.shields.io/badge/Kafka-231F20?style=flat&logo=apache-kafka&logoColor=white)
![Resend](https://img.shields.io/badge/Resend-000000?style=flat&logo=resend&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-25A162?style=flat&logo=junit5&logoColor=white)

- **Java + Spring Boot** — Backend principal, com suporte a agendamento de tarefas, controle de sessões e endpoints REST.
- **Apache Kafka** — Mensageria para processamento assíncrono e desacoplamento das operações de voto.
- **Resend** — Plataforma de envio de e-mails utilizada para o envio de códigos de validação em duas etapas (2FA).
- **Docker/Docker Compose** — Orquestração dos ambientes de desenvolvimento e produção, facilitando a execução dos serviços.
- **Postman** — Collections para teste dos endpoints e documentação do fluxo das APIs.
- **JUnit** — Framework de testes unitários para Java, utilizado nos testes dos serviços.

---

## ✨ Funcionalidades

### 🔐 Autenticação e Autorização
- Autenticação com **Spring Security** e **JWT**
- 📧 Login social com **Gmail (OAuth2)**
- 🛡️ Recuperação de senha com **autenticação em duas etapas (2FA)**
- 🔑 Endpoint de **chave pública** para verificação de tokens

### 🔒 Segurança de Senhas
- Senhas devem ter entre **8 e 12 caracteres** contendo números, caracteres especiais, letras maiúsculas e minúsculas
- Usuários registrados via **Gmail não podem alterar a senha** (gerenciada pelo Google)

### ⏱️ Controle de Sessão
- **Logout automático** após 30 minutos sem geração de novos tokens
- **Sessão única**: não permite que o mesmo usuário esteja logado em mais de um local simultaneamente
- Todo **novo login invalida o token** gerado anteriormente
- **Invalidação automática** de tokens após mudança de senha

### 🚨 Controle de Bloqueio de Conta
- Notificações via e-mail em **15, 30 e 90 dias** alertando sobre bloqueio de conta por inatividade
- Bloqueio automático de contas sem atividade recente

### 🧹 Manutenção e Monitoramento
- 🗑️ Limpeza agendada de **códigos de verificação expirados**
- 📊 Implementação de **logs com data** para todas as operações, facilitando monitoramento e auditoria

---

## ⚙️ Configuração de Variáveis de Ambiente

O projeto utiliza variáveis de ambiente para configurações sensíveis. Use o arquivo `.env.example` como modelo.

### 📋 Descrição das Variáveis

| Variável | Descrição |
|----------|-----------|
| `EMAIL_APPLICATION` | E-mail da aplicação para envio de notificações |
| `EMAIL_APPLICATION_PASSWORD` | Senha de aplicativo do e-mail (não é a senha normal da conta) |
| `G_CLIENT_ID` | Client ID do Google Cloud para utilizar OAuth2 |
| `G_CLIENT_SECRET` | Client Secret do Google Cloud para OAuth2 |
| `RESEND_API_KEY` | Chave de API do Resend para envio de e-mails |
| `RESEND_EMAIL` | E-mail remetente configurado no Resend |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco de dados PostgreSQL (ambiente local) |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco de dados PostgreSQL (ambiente local) |

> **Nota:** `G_CLIENT_ID` e `G_CLIENT_SECRET` são gerados após a criação das credenciais OAuth 2.0 no Google Cloud Console (veja seção [Configuração do OAuth2](#-configuração-do-oauth2))

### 🐳 Para uso com Docker Compose

1. **Crie o arquivo `.env`** na raiz do projeto:
   ```bash
   cp .env.example .env
   ```

2. **Edite o arquivo `.env`** e preencha com seus valores:
   ```dotenv
   EMAIL_APPLICATION=seu-email@exemplo.com
   EMAIL_APPLICATION_PASSWORD=sua-senha-app
   G_CLIENT_ID=seu-client-id.apps.googleusercontent.com
   G_CLIENT_SECRET=seu-client-secret
   RESEND_API_KEY=re_sua_chave_resend
   RESEND_EMAIL=noreply@seudominio.com
   SPRING_DATASOURCE_PASSWORD=sua-senha-db
   SPRING_DATASOURCE_USERNAME=seu-usuario-db
   ```

3. **Execute o Docker Compose** (ele carregará automaticamente o arquivo `.env`):
   ```bash
   docker compose up -d
   ```

### 💻 Para execução local (fora do container)

Você pode configurar as variáveis de ambiente diretamente no seu sistema operacional ou IDE.

#### 🍎 macOS / Linux

**Temporário (apenas para a sessão atual do terminal):**
```bash
export EMAIL_APPLICATION="seu-email@exemplo.com"
export EMAIL_APPLICATION_PASSWORD="sua-senha-app"
export G_CLIENT_ID="seu-client-id.apps.googleusercontent.com"
export G_CLIENT_SECRET="seu-client-secret"
export RESEND_API_KEY="re_sua_chave_resend"
export RESEND_EMAIL="noreply@seudominio.com"
export SPRING_DATASOURCE_PASSWORD="sua-senha-db"
export SPRING_DATASOURCE_USERNAME="seu-usuario-db"
```

**Permanente (adicione ao `~/.zshrc` ou `~/.bashrc`):**
```bash
# Abra o arquivo
nano ~/.zshrc  # ou ~/.bashrc para bash

# Adicione as variáveis no final do arquivo
export EMAIL_APPLICATION="seu-email@exemplo.com"
export EMAIL_APPLICATION_PASSWORD="sua-senha-app"
# ... adicione todas as variáveis

# Recarregue o arquivo
source ~/.zshrc  # ou source ~/.bashrc
```

#### 🪟 Windows

**PowerShell (temporário):**
```powershell
$env:EMAIL_APPLICATION="seu-email@exemplo.com"
$env:EMAIL_APPLICATION_PASSWORD="sua-senha-app"
$env:G_CLIENT_ID="seu-client-id.apps.googleusercontent.com"
$env:G_CLIENT_SECRET="seu-client-secret"
$env:RESEND_API_KEY="re_sua_chave_resend"
$env:RESEND_EMAIL="noreply@seudominio.com"
$env:SPRING_DATASOURCE_PASSWORD="sua-senha-db"
$env:SPRING_DATASOURCE_USERNAME="seu-usuario-db"
```

**Permanente (Variáveis de Sistema):**
1. Pressione `Win + R`, digite `sysdm.cpl` e pressione Enter
2. Vá para a aba **Avançado** → **Variáveis de Ambiente**
3. Em **Variáveis do usuário**, clique em **Novo**
4. Adicione cada variável com seu nome e valor
5. Clique em **OK** e reinicie o terminal/IDE

#### 🔧 IntelliJ IDEA

1. **Abra as configurações de execução:**
   - Vá em `Run` → `Edit Configurations...`

2. **Adicione as variáveis de ambiente:**
   - Encontre a seção **Environment variables**
   - Clique no ícone de pasta 📁 para abrir o editor
   - Adicione cada variável no formato `NOME=valor`
   - Ou cole todas de uma vez separadas por ponto e vírgula (`;` no Windows ou `:` no macOS/Linux):
     ```
     EMAIL_APPLICATION=seu-email@exemplo.com;EMAIL_APPLICATION_PASSWORD=sua-senha-app;G_CLIENT_ID=seu-client-id;G_CLIENT_SECRET=seu-secret;RESEND_API_KEY=re_sua_chave;RESEND_EMAIL=noreply@seudominio.com;SPRING_DATASOURCE_PASSWORD=senha;SPRING_DATASOURCE_USERNAME=usuario
     ```

3. **Salve e execute** o projeto

#### 🌱 Spring Tool Suite (STS) / Eclipse

1. **Clique com o botão direito** no projeto → **Run As** → **Run Configurations...**

2. **Selecione a configuração** da aplicação Spring Boot

3. **Vá para a aba Environment**

4. **Clique em Add** e adicione cada variável:
   - Name: `EMAIL_APPLICATION`
   - Value: `seu-email@exemplo.com`
   - Repita para todas as variáveis

5. **Clique em Apply** e depois em **Run**

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
   - Adicione as URI de redirecionamento:  
     ```
     http://localhost:8080/login/oauth2/code/google
     http://localhost:2300/auth-server/login/oauth2/code/google
     http://localhost:2310/auth-server/login/oauth2/code/google
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

### 📦 Executando com Docker Compose

O projeto utiliza arquivos Docker Compose dedicados por perfil:

- **`docker-compose.local.yml`** → suporte ao perfil local (somente PostgreSQL; a aplicação roda fora do Docker)
- **`docker-compose.docker.yml`** → perfil docker (PostgreSQL + Auth-Server)
- **`docker-compose.aws.yml`** → perfil aws (PostgreSQL + Auth-Server)

#### Pré-requisitos
- Docker e Docker Compose instalados
- Arquivo `.env` configurado (veja [Configuração de Variáveis de Ambiente](#️-configuração-de-variáveis-de-ambiente))

#### 🔧 Passos para Deploy

1. **Clone o repositório** (se ainda não o fez):
   ```bash
   git clone <url-do-repositorio>
   cd auth-server
   ```

2. **Configure o arquivo `.env`**:
   ```bash
   cp .env.example .env
   ```
   Em seguida, edite o `.env` com suas credenciais.

3. **Construa e inicie o perfil desejado**:
   ```bash
   docker compose -f docker-compose.docker.yml up -d --build
   ```
   
   > 💡 Arquivos disponíveis:
   > - `docker compose -f docker-compose.local.yml up -d`
   > - `docker compose -f docker-compose.docker.yml up -d --build`
   > - `docker compose -f docker-compose.aws.yml up -d --build`

4. **Verifique se os containers estão rodando**:
   ```bash
   docker compose -f docker-compose.docker.yml ps
   ```
   
   Você deverá ver os containers do perfil selecionado, como `postgres-auth` e `ms-auth-server`.

5. **Acompanhe os logs** (opcional):
   ```bash
   docker compose -f docker-compose.docker.yml logs -f ms-auth-server
   ```

#### 🛑 Parar os serviços

Para parar todos os containers:
```bash
docker compose -f docker-compose.docker.yml down
```

Para parar e remover volumes (⚠️ apaga dados do banco):
```bash
docker compose -f docker-compose.docker.yml down -v
```

#### 🔄 Reconstruir a aplicação

Se você fez alterações no código e precisa reconstruir:
```bash
docker compose -f docker-compose.docker.yml up -d --build ms-auth-server
```

#### 📍 Endpoints após Deploy

- **API Auth-Server**: `http://localhost:2300`
- **Swagger Docs**: `http://localhost:2300/auth-server/v3/api-docs`
- **PostgreSQL (Docker)**: `localhost:5432`
- **PostgreSQL (Local)**: `localhost:5050`

📁 Arquivos:
- [`docker-compose.local.yml`](docker-compose.local.yml)
- [`docker-compose.docker.yml`](docker-compose.docker.yml)
- [`docker-compose.aws.yml`](docker-compose.aws.yml)

---

### 💻 Executando Localmente (sem Docker)

Se preferir rodar a aplicação diretamente na sua máquina:

1. **Configure as variáveis de ambiente** (veja [seção de configuração](#️-configuração-de-variáveis-de-ambiente))

2. **Inicie apenas os bancos de dados com Docker**:
   ```bash
   docker compose -f docker-compose.local.yml up -d postgres-auth-local
   ```

3. **Execute a aplicação com Maven**:
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```
   
   Ou com Maven instalado:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. **Ou execute via IDE** (IntelliJ/Eclipse) com o profile `local` ativo

---

## 📘 Documentação da API

A aplicação disponibiliza documentação interativa via **Swagger UI** e endpoints para **Health Check**.

### 🌐 Acessando o Swagger UI

Após iniciar a aplicação, acesse a interface do Swagger nos seguintes endereços:

**Ambiente Local** (porta 2310):
```
http://localhost:2310/auth-server/swagger-ui/index.html
```

**Ambiente Docker** (porta 2300):
```
http://localhost:2300/auth-server/swagger-ui/index.html
```

### 🏥 Health Check

Verifique o status da aplicação através do endpoint do Actuator:

**Ambiente Local**:
```
http://localhost:2310/auth-server/actuator/health
```

**Ambiente Docker**:
```
http://localhost:2300/auth-server/actuator/health
```

### 📥 Importando para o Postman

Para usar a API no Postman, siga os passos:

1. **Acesse o Swagger UI** conforme os links acima

2. **Exporte a documentação em JSON**:
   - Na interface do Swagger UI, procure o link `/v3/api-docs` ou
   - Acesse diretamente:
     - Local: `http://localhost:2310/auth-server/v3/api-docs`
     - Docker: `http://localhost:2300/auth-server/v3/api-docs`

3. **Salve o conteúdo JSON** em um arquivo (ex: `auth-server-api.json`)

4. **Importe no Postman**:
   - Abra o Postman
   - Clique em **Import** no canto superior esquerdo
   - Selecione o arquivo JSON salvo
   - O Postman criará automaticamente uma coleção com todos os endpoints

> 💡 **Alternativa**: Você também pode usar o arquivo estático do Swagger disponível em [`src/main/resources/static/swagger.yml`](src/main/resources/static/swagger.yml)

### 📁 Arquivos de Documentação

- **Swagger YAML estático**: [`src/main/resources/static/swagger.yml`](src/main/resources/static/swagger.yml)
- **Documentação adicional**: [`docs/swagger/`](docs/Diagrams/)

---

## 📨 Serviço de Email

Este projeto utiliza o [**Resend**](https://resend.com) como serviço para envio de emails em:

- 🔁 Emails de verificação de conta  
- 🔐 Códigos de autenticação em duas etapas (2FA)  
- 🔑 Links de recuperação de senha
- ⏰ Notificações de bloqueio de conta por inatividade

### 🌐 Passo 1: Registrar um Domínio

Para enviar e-mails profissionais, você precisa de um domínio próprio.

#### Opções de Registro:

1. **Registro.br** (domínios `.br`): [https://registro.br](https://registro.br)
2. **GoDaddy**: [https://godaddy.com](https://godaddy.com)
3. **Namecheap**: [https://namecheap.com](https://namecheap.com)
4. **Google Domains**: [https://domains.google](https://domains.google)
5. **Cloudflare Registrar**: [https://cloudflare.com/products/registrar](https://cloudflare.com/products/registrar)

> 💡 **Dica**: O Cloudflare Registrar oferece preços competitivos sem margem de lucro adicional.

#### Como registrar:

1. Acesse um dos sites acima
2. Pesquise pelo domínio desejado (ex: `meudominio.com`)
3. Adicione ao carrinho e finalize a compra
4. Após a compra, você receberá acesso ao painel de gerenciamento DNS

---

### ☁️ Passo 2: Configurar o Cloudflare

O **Cloudflare** oferece gerenciamento de DNS gratuito, melhor performance e segurança para seu domínio.

#### 2.1 - Criar conta no Cloudflare

1. Acesse [https://dash.cloudflare.com/sign-up](https://dash.cloudflare.com/sign-up)
2. Crie sua conta gratuita

#### 2.2 - Adicionar seu domínio ao Cloudflare

1. No painel do Cloudflare, clique em **"Add a Site"** (Adicionar um site)
2. Digite seu domínio (ex: `meudominio.com`)
3. Escolha o plano **Free** (gratuito)
4. Clique em **"Continue"**

#### 2.3 - Atualizar os Nameservers

O Cloudflare fornecerá dois nameservers personalizados, algo como:

```
alice.ns.cloudflare.com
bob.ns.cloudflare.com
```

**Agora você precisa atualizar os nameservers no seu registrador:**

1. **Acesse o painel do registrador** onde você comprou o domínio (Registro.br, GoDaddy, etc.)
2. **Encontre a seção de Nameservers** (ou DNS/Name Servers)
3. **Substitua os nameservers padrão** pelos fornecidos pelo Cloudflare
4. **Salve as alterações**

> ⏱️ **Atenção**: A propagação DNS pode levar de alguns minutos até 48 horas.

#### 2.4 - Verificar ativação

1. Volte ao painel do Cloudflare
2. Aguarde até o status mudar para **"Active"** (Ativo)
3. Você receberá um e-mail confirmando a ativação

---

### 📧 Passo 3: Configurar o Resend

O **Resend** é a plataforma que enviará os e-mails da aplicação.

#### 3.1 - Criar conta no Resend

1. Acesse [https://resend.com/signup](https://resend.com/signup)
2. Crie sua conta (o plano gratuito permite **3.000 e-mails/mês**)

#### 3.2 - Obter a API Key

1. No dashboard do Resend, vá em **"API Keys"**
2. Clique em **"Create API Key"**
3. Dê um nome para a chave (ex: `auth-server-production`)
4. Copie a chave gerada (ela aparecerá apenas uma vez)
5. Salve no arquivo `.env`:
   ```env
   RESEND_API_KEY=re_sua_chave_aqui
   ```

#### 3.3 - Adicionar e verificar seu domínio

1. No dashboard do Resend, vá em **"Domains"**
2. Clique em **"Add Domain"**
3. Digite seu domínio (ex: `meudominio.com`)
4. O Resend fornecerá registros DNS para configuração

#### 3.4 - Configurar registros DNS no Cloudflare

O Resend fornecerá **3 tipos de registros DNS**:

**1. Registro SPF (TXT)**
```
Type: TXT
Name: @
Content: v=spf1 include:_spf.resend.com ~all
TTL: Auto
```

**2. Registro DKIM (TXT)**
```
Type: TXT
Name: resend._domainkey
Content: [valor fornecido pelo Resend]
TTL: Auto
```

**3. Registro DMARC (TXT)** - Opcional mas recomendado
```
Type: TXT
Name: _dmarc
Content: v=DMARC1; p=none; rua=mailto:dmarc@seudominio.com
TTL: Auto
```

**Como adicionar no Cloudflare:**

1. Vá para o painel do Cloudflare
2. Selecione seu domínio
3. Clique em **"DNS"** no menu lateral
4. Clique em **"Add record"** (Adicionar registro)
5. Para cada registro fornecido pelo Resend:
   - Selecione o **Type** (TXT)
   - Digite o **Name** (ex: `@`, `resend._domainkey`, `_dmarc`)
   - Cole o **Content** exatamente como fornecido
   - Mantenha **Proxy status** como **"DNS only"** (ícone de nuvem cinza)
   - Clique em **"Save"**

#### 3.5 - Verificar o domínio no Resend

1. Após adicionar todos os registros DNS no Cloudflare
2. Volte ao dashboard do Resend
3. Clique em **"Verify"** ao lado do seu domínio
4. Se os registros estiverem corretos, o status mudará para **"Verified"** ✅

> ⏱️ **Nota**: Pode levar alguns minutos para os registros DNS se propagarem.

#### 3.6 - Configurar o e-mail remetente

Após verificar o domínio, defina o e-mail que será usado para envio:

```env
RESEND_EMAIL=noreply@seudominio.com
```

> 💡 **Dica**: Use e-mails como `noreply@`, `notifications@` ou `auth@` para comunicações automáticas.

#### 3.7 - Testar o envio

Você pode testar o envio diretamente no painel do Resend:

1. Vá em **"Emails"** → **"Send test email"**
2. Digite um e-mail de destino
3. Clique em **"Send"**
4. Verifique se o e-mail chegou (confira também a pasta de spam)

---

### ✅ Passo 4: Configurar no Projeto

Certifique-se de que o arquivo `.env` está configurado corretamente:

```env
# Resend Configuration
RESEND_API_KEY=re_sua_chave_resend_aqui
RESEND_EMAIL=noreply@seudominio.com
```

As configurações já estão prontas em `application-local.yml`, `application-docker.yml` e `application-aws.yml`:

```yaml
resend:
  api:
    key: "${RESEND_API_KEY}"
    url: "https://api.resend.com/emails"
  sender: "${RESEND_EMAIL}"
```

Inicie a aplicação e os e-mails serão enviados automaticamente pelo Resend! 🚀

---

### 🔍 Verificação e Troubleshooting

#### Como testar se está funcionando:

1. **Registre um novo usuário** na aplicação
2. **Verifique se o e-mail de verificação** foi enviado
3. **Confira os logs** da aplicação para possíveis erros
4. **Acesse o dashboard do Resend** para ver o histórico de envios

#### Problemas comuns:

❌ **Erro: "Domain not verified"**
- Verifique se todos os registros DNS foram adicionados corretamente no Cloudflare
- Aguarde a propagação DNS (pode levar até 48 horas)
- Use ferramentas como [MXToolbox](https://mxtoolbox.com/SuperTool.aspx) para verificar os registros

❌ **E-mails caindo no spam**
- Configure o registro DMARC
- Adicione um registro DKIM válido
- Evite conteúdo suspeito nos e-mails
- Aqueça o domínio enviando poucos e-mails inicialmente

❌ **API Key inválida**
- Verifique se a chave está correta no arquivo `.env`
- Gere uma nova chave no painel do Resend se necessário

---

### 📁 Arquivos Relacionados

- 📦 Camada de serviço de email: `src/main/java/.../service/EmailService.java`  
- ⚙️ Configurações: 
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-docker.yml`
  - `src/main/resources/application-aws.yml`

---

## 📄 Licença

Este projeto está licenciado sob a **Licença MIT**.

---

## 🚀 Futuras Implementações

Este projeto segue um roadmap estruturado de funcionalidades planejadas para as próximas versões.

### 📊 Sobre o Versionamento

O projeto utiliza **Versionamento Semântico** (Semantic Versioning) no formato `MAJOR.MINOR.PATCH`:

- **Versão 1.0.0**: Primeira versão estável com todas as funcionalidades descritas na seção [Funcionalidades](#-funcionalidades)
- **Versões X.Y.0** (ex: 1.1.0, 1.2.0): Pequenas correções e melhorias, **sem novas funcionalidades**
- **Versões X.0.0** (ex: 2.0.0, 3.0.0): Incluem **novas funcionalidades** importantes
- **Versões X.Y.Z** (ex: 2.1.1, 3.0.2): Pequenas atualizações, correções de bugs e patches de segurança

---

### 🎯 Roadmap de Funcionalidades

#### 📦 Versão 1.1.0
**Expansão de Autenticação Social**

- 🔐 Login via **Microsoft** (Azure AD / Microsoft Account)
- 🍎 Login via **Apple** (Sign in with Apple)
- 🐙 Login via **GitHub** (OAuth2)
- 🔑 Criação e gerenciamento de **tokens para aplicações** (API Keys)
- 📋 Dashboard para visualização e revogação de tokens ativos

---

#### 📦 Versão 2.0.0
**Notificações Administrativas e Auditoria**

- 📧 Notificação via e-mail para **administradores** quando houver:
  - Mudanças de senhas de usuários
  - Tentativas de acesso suspeitas
  - Bloqueio de contas por inatividade
- 📝 Sistema de auditoria completo com histórico de ações
- 👥 Gerenciamento de perfis de administradores

---

#### 📦 Versão 3.0.0
**Relatórios e Métricas**

- 📊 Envio automático de **relatórios mensais** via e-mail contendo:
  - 📈 Número de novos registros
  - 🔐 Total de logins realizados
  - ✅ Quantidade de usuários ativos
  - ❌ Quantidade de usuários inativos
  - 📉 Taxa de conversão e retenção
- 📉 Dashboard de métricas em tempo real
- 📅 Geração de relatórios personalizados por período

---

### 💡 Sugestões e Contribuições

Tem alguma ideia para melhorar o **Auth-Server**? 

- envie um email para guilherme.f.h@hotmail.com com o assunto **Auth-Server**

**Obrigado pelo seu interesse** ✨

att  
**Guilherme Hahn** - [GitHub](https://github.com/HahnGuil)

---

## 🇬🇧 Versão em Inglês

Para a versão em inglês deste README, acesse o arquivo [`README.md`](./README.md).
