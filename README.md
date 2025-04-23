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

1. **Spring Boot**  
   Edit `src/main/resources/application.properties`

   To Ensure that the project works
   ```properties
   # Flask checker URL
   environment.checker.url=http://localhost:5000/check
   spring.datasource.url=jdbc:mysql://localhost:3306/appstore
   spring.datasource.username = user
   spring.datasource.password= Password123
   ```

2. **Flask Checker**  
   Located under `checker`:

    - `checker.py` implements `check` endpoint.
    - `requirements.txt` lists dependencies.

---

### Build & Run Locally

#### 1. Start Flask Checker

```bash
cd checker
pip install -r requirements.txt
export FLASK_APP=checker.py
flask run --host=0.0.0.0 --port=5000
```

#### 2. Build & Run Spring Boot

```bash
cd ..
mvn clean package
java -jar target/appstore-0.0.1-SNAPSHOT.jar
```

- Application listens on **https://localhost:8443**
- Checker listens on **http://localhost:5000**
- Database listens on **http://localhost:3306**

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

- **AppStore** → https://localhost:8443
- **Checker** → http://localhost:5000
- **Database** → http://localhost:3306

---

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
