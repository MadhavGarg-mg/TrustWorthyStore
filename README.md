# TrustWorthyStore

A secure, Spring Boot–based app store with a lightweight Python/Flask file‑hash checker.  
Users can register, upload APKs with SHA‑256–verified metadata, and download apps.  
Admins can manage users, issue warnings, and clear suspensions.

---

## Table of Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Prerequisites](#prerequisites)
4. [Getting Started](#getting-started)
   - [Clone the Repo](#clone-the-repo)
   - [Configuration](#configuration)
   - [Build & Run Locally](#build--run-locally)
5. [Running Tests](#running-tests)
6. [Docker Setup](#docker-setup)
7. [Endpoints & Usage](#endpoints--usage)
8. [License](#license)

---

## Features

- **User Registration & Login** (Spring Security)
- **File Upload + SHA‑256 Validation** via external Flask checker
- **Role-based Access** (USER, DEVELOPER, ADMIN)
- **Warning & Suspension** workflow on hash mismatch
- **Admin Dashboard** for user management

---

## Architecture

```
┌──────────────┐          ┌────────────────────┐
│  Frontend    │ -------> │  Controllers,      │
└──────────────┘          │  Services, Repos   │
                          └────────────────────┘
                                    ^
                                    │ HTTP (JSON / HTML)
                          ┌────────────────────┐
                          │  Flask Checker     │
                          └────────────────────┘
```

---

## Prerequisites

- **Java 17** (or later)
- **Maven 3.6+**
- **Python 3.8+**
- **Docker 20.10+** & **Docker Compose 1.27+**

---

## Getting Started

### Clone the Repo

```bash
git clone https://github.com/MadhavGarg-mg/TrustWorthyStore.git
cd TrustWorthyStore
```

### Configuration

All secrets and connection strings are now loaded via a `.env` file.

> Create a file named `.env` at the **project root** (next to `docker-compose.yml`) with:
> 
```dotenv
# MySQL database
DATASOURCE_NAME=" "
DATASOURCE_URL="jdbc:mysql://mysql:3306/${DATASOURCE_NAME}"
DATASOURCE_USERNAME=" "
DATASOURCE_PASSWORD=" "

# Flask checker
CHECKER_URL="http://checker:5000/check"

# Admin bootstrapping
ADMIN_USERNAME=" "
ADMIN_PASSWORD=" "

# SSL keystore
SSL_KEY_STORE_PASSWORD=" "
```
>> **Do not** commit `.env`; add it to your `.gitignore`.

Spring Boot’s `application.properties` should use these placeholders:
```properties
spring.datasource.url=${DATASOURCE_URL}
spring.datasource.username=${DATASOURCE_USERNAME}
spring.datasource.password=${DATASOURCE_PASSWORD}

environment.checker.url=${CHECKER_URL}

server.port=8443
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEY_STORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```
---

#### Build & Run Locally

#### 1. Start Flask Checker

```bash
cd checker
pip install -r requirements.txt
export FLASK_APP=checker.py
flask run --host=0.0.0.0 --port=5000

```

#### 2. Build & Run Spring Boot


Ensure your .env file (from the Configuration section) is present in the project root before running these commands.

```bash
cd ..
mvn spring-boot:run
```

- Application listens on **https://localhost:8443**

---

## Running Tests

### Java

```bash
mvn test
```

---

## Docker Setup

### Up & Running

```bash
docker-compose up --build
```

Go to: https://localhost:8443

```diff
## Docker Setup

>> **Ensure your `.env` file is present** (in the same directory as your `docker-compose.yml`).  
>> Docker Compose will automatically load it and inject the variables into each service.
```

## Endpoints & Usage

| Path                          | Method | Description                                 |
| ----------------------------- | ------ | ------------------------------------------- |
| `/`                           | GET    | Home page                                   |
| `/register`                   | GET/POST | User registration form & submit          |
| `/login`                      | GET    | Login form                                  |
| `/app-upload`                 | GET/POST | Upload APK + metadata                    |
| `/app-download`               | GET    | List available apps                        |
| `/app-download/{id}`          | GET    | Download APK file                          |
| `/app-download/{id}/metadata` | GET    | Retrieve raw metadata JSON                 |
| `/admin` / `/admin/dashboard` | GET    | Admin dashboard (requires ADMIN role)      |
| `/admin/addAdmin`             | POST   | Add new admin                              |
| `/admin/users/data`           | GET    | JSON list of all users                     |
| `/admin/users/manageSuspensions` | GET | View suspended users                       |
| `/admin/user/{id}/unsuspend`  | POST   | Unsuspend & clear warnings for a user      |

---

## License

This project is licensed under the MIT License.  
See [LICENSE](LICENSE) for details.
