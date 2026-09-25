# Square Users (SU) — Microservice de Gestion des Utilisateurs

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Port](https://img.shields.io/badge/Port-8081-blue.svg)](#)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%20%2F%20H2-blue.svg)](#)
[![OpenAPI](https://img.shields.io/badge/Documentation-Swagger%20UI-green.svg)](#-documentation-interactive-swagger-ui)

Microservice Spring Boot responsable de la gestion des profils utilisateurs (création, consultation, suppression, attribution d'UUID unique) et point de contrôle d'existence inter-services pour le microservice de jeux [**Square Games (SG)**](https://github.com/ImRobot777/spring_square_games).

---

## 🏗️ Architecture & Choix Techniques

Le service applique les principes de l'architecture logicielle en couches, du patron *Database-per-Service* et d'une sécurité *Stateless* moderne :
- **Sécurité & Authentification Stateless (`config`, `service`)** :
  - **Spring Security 6** : Configuration centralisée dans `SecurityConfig` avec `SessionCreationPolicy.STATELESS` et protection CSRF désactivée.
  - **Hachage BCrypt** : Tous les mots de passe sont salés et hachés avec `BCryptPasswordEncoder` avant persistance.
  - **Moteur JWT Asymétrique (RS256) & Custom Claims** : Émission de jetons JWT signés avec une clé privée RSA 2048 bits (`private_key.pem`). Les jetons intègrent les claims personnalisés `"userId"` (`UUID`) et `"roles"`, construits grâce à `CustomUserDetails` sans requêtes SQL redondantes.
  - **Sécurité des Méthodes (RBAC & ABAC)** : Activation de `@EnableMethodSecurity`. Protection déclarative fine via SpEL : `@PreAuthorize("hasRole('ADMIN')")` pour les actions privilégiées et `@PreAuthorize("#pseudo == authentication.name")` pour l'auto-gestion de compte.
- **Couche Présentation REST (`controller`)** : Endpoints CRUD stricts, respect des codes HTTP standards (`200 OK`, `204 NO CONTENT`, `404 NOT FOUND`, `400 BAD REQUEST`, `401 UNAUTHORIZED`, `403 FORBIDDEN`), et documentation interactive OpenAPI 3 avec SpringDoc.
- **Couche Métier (`service`)** : Validation des données entrantes, attribution automatique d'un identifiant `UUID`, hachage du mot de passe avec BCrypt, attribution du rôle par défaut (`ROLE_USER`), et émission de jetons JWT (`JwtService`).
- **Objets de Transfert (`dto`)** : Enregistrements Java (`record`) immutables (`UserCreationParams`) garantissant un découplage strict entre les flux réseaux JSON et les entités internes.
- **Couche Persistance (`dao`, `entity`)** : Persistance relationnelle avec **Spring Data JPA** et **Hibernate**. L'entité `UserEntity` stocke le profil avec son `passwordHash` et son `role`.
- **Isolation Complète des Données** : Square Users dispose de son propre conteneur Docker PostgreSQL dédié (`su-postgres`), isolé du conteneur de jeux sur le port hôte `5433`.

```text
[ Square Games (SG) ] ──(GET /users/{userId}/valid)──> [ UserController ] (@RestController - Port 8081)
                                                              │
[ Client HTTP / Admin ] ──(POST /users, GET, DELETE)─────────┤
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
                                                 [ Conteneur PostgreSQL (Port 5433) ]
```

---

## 📋 Prérequis

1. **Java Development Kit (JDK) 21** ou supérieur :
   ```bash
   java -version
   ```
2. **Docker** (pour exécuter la base de données PostgreSQL) :
   ```bash
   docker --version
   ```
3. **Maven Wrapper** (inclus directement dans le projet via `./mvnw`).

---

## 🗄️ Infrastructure Base de Données (Docker)

Le service s'exécute avec sa propre base PostgreSQL 16 conteneurisée.
> ⚠️ **Note sur les Ports** : Pour éviter tout conflit avec le port PostgreSQL par défaut (`5432`) utilisé par Square Games, le port hôte de ce conteneur est configuré sur **`5433`** (avec redirection vers le port interne `5432`).

```bash
# 1. Créer le volume Docker pour la rétention persistante
docker volume create su-postgres-data

# 2. Démarrer le conteneur PostgreSQL dédié
docker run -d \
  --name su-postgres \
  -p 5433:5432 \
  -e POSTGRES_DB=square_users \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -v su-postgres-data:/var/lib/postgresql/data \
  postgres:16
```

Pour redémarrer un conteneur arrêté :
```bash
docker start su-postgres
```

---

## 🚀 Démarrage de l'Application

### Option A : Profil PostgreSQL (Recommandé / Production)
Assurez-vous que le conteneur `su-postgres` est démarré sur le port `5433`, puis lancez :

```bash
./mvnw spring-boot:run
```
L'application démarre sur le port **8081** et se connecte à `jdbc:postgresql://localhost:5433/square_users`.

### Option B : Profil H2 (Mode Léger / Sans Docker)
Pour lancer le microservice instantanément en mémoire sans aucune dépendance Docker :

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
*Console web H2 disponible sur : `http://localhost:8081/h2-console` (JDBC URL : `jdbc:h2:mem:square_users`, User : `sa`, mot de passe vide).*

---

## 📖 Documentation Interactive Swagger UI

Dès le démarrage de l'application, la documentation interactive Swagger UI est directement accessible dans votre navigateur :

👉 **Interface Swagger UI** : [`http://localhost:8081/swagger-ui/index.html`](http://localhost:8081/swagger-ui/index.html)  
👉 **Spécification OpenAPI 3 (JSON)** : [`http://localhost:8081/v3/api-docs`](http://localhost:8081/v3/api-docs)

---

## 🌐 Guide des Endpoints & Exemples `curl`

### 1. Créer un nouvel utilisateur (`POST /users`)
Crée un compte avec mot de passe haché par BCrypt et génère automatiquement un identifiant UUID unique.
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "pseudo": "Alice",
    "email": "alice@test.com",
    "password": "secretPassword123"
  }'
```
*Réponse HTTP 200 OK :*
```json
{
  "id": "b8f05e32-1234-4a56-b789-0123456789ab",
  "pseudo": "Alice",
  "email": "alice@test.com",
  "passwordHash": "$2a$10$w...hachageBCryptSecurise...",
  "role": "ROLE_USER"
}
```

### 2. Consulter un profil utilisateur (`GET /users/{userId}`)
Recherche un compte par son UUID.
```bash
curl -X GET http://localhost:8081/users/b8f05e32-1234-4a56-b789-0123456789ab
```
*Si l'utilisateur n'existe pas, l'API répond proprement avec un code `404 NOT FOUND`.*

### 3. Vérifier la validité d'un identifiant (`GET /users/{userId}/valid`)
Endpoint léger dédié aux appels inter-services (notamment consommé par `Square Games`) :
```bash
curl -X GET http://localhost:8081/users/b8f05e32-1234-4a56-b789-0123456789ab/valid
```
*Réponse HTTP 200 OK :* `true` (ou `false` si l'UUID est inconnu).

### 4. Supprimer un utilisateur (`DELETE /users/{userId}`)
Supprime définitivement un compte utilisateur.
```bash
curl -X DELETE http://localhost:8081/users/b8f05e32-1234-4a56-b789-0123456789ab
```
*Réponse : Code HTTP `204 NO CONTENT` (succès sans corps).*

### 5. S'authentifier et obtenir un JWT (`POST /auth/login`)
Authentifie l'utilisateur via son pseudo et mot de passe, puis délivre un jeton JWT asymétrique signé avec RSA 2048 bits (RS256) :
```bash
curl -X POST http://localhost:8081/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "Alice",
    "password": "secretPassword123"
  }'
```
*Réponse HTTP 200 OK :*
```json
{
  "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```
*(Si les identifiants sont erronés, l'API répond en `401 UNAUTHORIZED`).*

---

## 🧪 Exécution des Tests Automatisés

Le microservice est couvert par une suite de tests unitaires et d'intégration validant le contrôleur web, le contrôleur d'authentification, la couche service, le filtre JWT et le DAO avec **JUnit 5**, **Mockito** et **MockMvc** :

```bash
# Exécution de l'intégralité de la suite de tests (22 tests, 0 échec)
./mvnw clean test -Dspring.profiles.active=h2
```

---

## 🔄 Rôle Architectural : Authorization Server dans l'Écosystème

Dans notre écosystème microservices :
1. **Square Users (`SU` - Port 8081)** agit comme **Authorization Server** :
   - Gère le cycle de vie des utilisateurs et le hachage sécurisé BCrypt des mots de passe.
   - Délivre des jetons JWT asymétriques signés par sa clé privée RSA 2048 bits via `POST /auth/login`.
   - Inclut l'identifiant immuable `userId` et les rôles directement dans le payload du jeton.
2. **Square Games (`SG` - Port 8080)** agit comme **Resource Server Stateless** :
   - Dispose de la clé publique de `SU` (`public.pem`) pour vérifier instantanément la signature du jeton en mémoire vive.
   - Ne sollicite aucun appel réseau vers `SU` pour identifier le créateur d'une partie (scalabilité maximale et zéro latence).
3. **Validation Inter-Services des Adversaires** :
   - L'endpoint léger `GET /users/{userId}/valid` reste utilisé par le `RestClient` de Square Games pour valider l'existence des adversaires invités dans une partie avant de persister celle-ci.

Pour cloner et démarrer le microservice de jeux :
👉 [Dépôt GitHub Square Games (SG)](https://github.com/ImRobot777/spring_square_games)
