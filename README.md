# Square Users (SU) — User Management Microservice

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Port](https://img.shields.io/badge/Port-8081-blue.svg)](#)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%20%2F%20H2-blue.svg)](#)
[![OpenAPI](https://img.shields.io/badge/Documentation-Swagger%20UI-green.svg)](#-interactive-swagger-ui-documentation)

Spring Boot microservice responsible for managing user profiles (creation, retrieval, deletion, unique UUID generation) and serving as an inter-service existence check endpoint for the [**Square Games (SG)**](https://github.com/ImRobot777/spring_square_games) game engine microservice.

---

## 🏗️ Architecture & Technical Choices

The service adheres to layered architecture principles, the *Database-per-Service* pattern, and modern *Stateless* security standards:
- **Stateless Security & Authentication (`config`, `service`)**:
  - **Spring Security 6**: Centralized in `SecurityConfig` with `SessionCreationPolicy.STATELESS` and CSRF protection disabled.
  - **BCrypt Hashing**: All passwords are salted and hashed with `BCryptPasswordEncoder` prior to persistence.
  - **Asymmetric JWT Engine (RS256) & Custom Claims**: Issuance of JWT tokens signed with a 2048-bit RSA private key (`private_key.pem`). Tokens embed custom claims `"userId"` (`UUID`) and `"roles"`, constructed via `CustomUserDetails` without redundant database queries.
  - **Method Security (RBAC & ABAC)**: `@EnableMethodSecurity` enabled. Fine-grained declarative protection via SpEL: `@PreAuthorize("hasRole('ADMIN')")` for privileged operations and `@PreAuthorize("#pseudo == authentication.name")` for self-account management.
- **REST Presentation Layer (`controller`)**: Strict CRUD endpoints adhering to standard HTTP status codes (`200 OK`, `204 NO CONTENT`, `404 NOT FOUND`, `400 BAD REQUEST`, `401 UNAUTHORIZED`, `403 FORBIDDEN`), with interactive OpenAPI 3 documentation powered by SpringDoc.
- **Business Layer (`service`)**: Incoming data validation, automatic `UUID` assignment, BCrypt password hashing, default role assignment (`ROLE_USER`), and JWT token issuance (`JwtService`).
- **Data Transfer Objects (`dto`)**: Immutable Java records (`UserCreationParams`, `UserResponse`) ensuring strict boundary isolation between network JSON payloads and internal persistence entities (guaranteeing `passwordHash` is never exposed).
- **Persistence Layer (`dao`, `entity`)**: Relational persistence using **Spring Data JPA** and **Hibernate**. `UserEntity` persists the user account alongside `passwordHash` and `roles`.
- **Full Data Isolation**: Dedicated PostgreSQL Docker container (`su-postgres`), isolated on host port `5433`.

```text
[ Square Games (SG) ] ──(GET /users/{userId}/valid)──> [ UserController ] (@RestController - Port 8081)
                                                               │
[ HTTP Client / Admin ] ──(POST /users, GET, DELETE)─────────┤
                                                               ▼
                                                      [ UserServiceImpl ] (@Service)
                                                               │
                                                               ▼
                                                       [ JpaUserDao ] (@Repository)
                                                               │
                                                               ▼
                                                    [ UserEntityRepository ] (Spring Data JPA)
                                                               │
                                                               ▼
                                                  [ PostgreSQL Container (Port 5433) ]
```

---

## 📋 Prerequisites

1. **Java Development Kit (JDK) 21** or higher:
   ```bash
   java -version
   ```
2. **Docker** (to run the PostgreSQL database):
   ```bash
   docker --version
   ```
3. **Maven Wrapper** (included directly in the project via `./mvnw`).

---

## 🗄️ Database Infrastructure (Docker)

The service runs with its own dedicated PostgreSQL 16 container.
> ⚠️ **Port Configuration Note**: To avoid collision with the default PostgreSQL port (`5432`) used by Square Games, the host port is mapped to **`5433`** (forwarded to internal container port `5432`).

```bash
# 1. Create Docker volume for persistent data retention
docker volume create su-postgres-data

# 2. Start dedicated PostgreSQL container
docker run -d \
  --name su-postgres \
  -p 5433:5432 \
  -e POSTGRES_DB=square_users \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -v su-postgres-data:/var/lib/postgresql/data \
  postgres:16
```

To restart an existing container:
```bash
docker start su-postgres
```

---

## ⚙️ Configuration & Environment Variables

The microservice can be configured through system environment variables (*12-Factor App* methodology). Each variable has a default fallback for local development:

| Environment Variable | Description | Local Default |
|---|---|---|
| `SU_SERVER_PORT` | HTTP server listening port | `8081` |
| `SU_DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5433/square_users` |
| `SU_DB_USER` | Database username | `postgres` |
| `SU_DB_PASSWORD` | Database password | `postgres` |
| `SU_JWT_PRIVATE_KEY_PATH` | Path to RSA private key (used to sign JWT tokens) | `classpath:certs/private_key.pem` |
| `SU_JWT_PUBLIC_KEY_PATH` | Path to RSA public key (used to verify JWT tokens) | `classpath:certs/public_key.pem` |

---

## 🚀 Running the Application

### Option A: PostgreSQL Profile (Recommended / Production)
Ensure the `su-postgres` container is running on port `5433`, then launch:

```bash
./mvnw spring-boot:run
```
The application starts on port **8081** and connects to `jdbc:postgresql://localhost:5433/square_users`.

### Option B: H2 Profile (Lightweight / Docker-free Mode)
To run the service instantly in memory without Docker dependencies:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
*H2 web console available at: `http://localhost:8081/h2-console` (JDBC URL: `jdbc:h2:mem:square_users`, User: `sa`, empty password).*

---

## 📖 Interactive Swagger UI Documentation

Once the application is running, the interactive Swagger UI documentation is directly accessible in your browser:

👉 **Swagger UI Interface**: [`http://localhost:8081/swagger-ui/index.html`](http://localhost:8081/swagger-ui/index.html)  
👉 **OpenAPI 3 Specification (JSON)**: [`http://localhost:8081/v3/api-docs`](http://localhost:8081/v3/api-docs)

---

## 🌐 Endpoint Guide & `curl` Examples

### 1. Create a New User (`POST /users`)
Creates an account with a BCrypt-hashed password and automatically generates a unique UUID.
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "pseudo": "Alice",
    "email": "alice@test.com",
    "password": "secretPassword123"
  }'
```
*Response HTTP 200 OK:*
```json
{
  "id": "b8f05e32-1234-4a56-b789-0123456789ab",
  "pseudo": "Alice",
  "email": "alice@test.com",
  "roles": "ROLE_USER"
}
```

### 2. Get User Profile (`GET /users/{userId}`)
Retrieves an account by UUID.
```bash
curl -X GET http://localhost:8081/users/b8f05e32-1234-4a56-b789-0123456789ab
```
*If the user does not exist, the API returns `404 NOT FOUND`.*

### 3. Verify User Existence (`GET /users/{userId}/valid`)
Lightweight endpoint for inter-service existence checks (consumed by `Square Games`):
```bash
curl -X GET http://localhost:8081/users/b8f05e32-1234-4a56-b789-0123456789ab/valid
```
*Response HTTP 200 OK:* `true` (or `false` if the UUID is unknown).

### 4. Delete a User (`DELETE /users/{userId}`)
Permanently removes a user account.
```bash
curl -X DELETE http://localhost:8081/users/b8f05e32-1234-4a56-b789-0123456789ab
```
*Response: HTTP `204 NO CONTENT` (success with empty body).*

### 5. Authenticate and Obtain a JWT (`POST /auth/login`)
Authenticates the user with username and password, then issues an asymmetric JWT token signed with RSA 2048 bits (RS256):
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Alice",
    "password": "secretPassword123"
  }'
```
*Response HTTP 200 OK:*
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```
*(If credentials are invalid, the API returns `401 UNAUTHORIZED`).*

---

## 🧪 Automated Test Suite Execution

The microservice is covered by a suite of unit and integration tests validating the web controller, auth controller, service layer, JWT filter, and DAO with **JUnit 5**, **Mockito**, and **MockMvc**:

```bash
# Run all tests (27 tests, 0 failures)
./mvnw clean test -Dspring.profiles.active=h2
```

---

## 🔄 Architectural Role: Authorization Server in the Ecosystem

In our microservices ecosystem:
1. **Square Users (`SU` - Port 8081)** acts as the **Authorization Server**:
   - Manages user lifecycles and secure BCrypt password hashing.
   - Issues asymmetric JWT tokens signed with its 2048-bit RSA private key via `POST /auth/login`.
   - Embeds immutable `userId` and role claims directly in the token payload.
2. **Square Games (`SG` - Port 8080)** acts as the **Stateless Resource Server**:
   - Holds `SU`'s public key (`public.pem`) to verify token signatures in memory instantly.
   - Requires zero network calls to `SU` to identify game creators (maximum scalability, zero latency).
3. **Inter-Service Opponent Validation**:
   - The lightweight `GET /users/{userId}/valid` endpoint is used by Square Games' `RestClient` to verify that invited opponents exist before persisting a game.

To clone and run the game engine microservice:
👉 [Square Games (SG) GitHub Repository](https://github.com/ImRobot777/spring_square_games)
