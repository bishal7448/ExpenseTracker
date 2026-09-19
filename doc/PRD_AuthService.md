# Product Requirements Document (PRD): AuthService

## 1. Overview & Vision
The **AuthService** is a foundational, centralized microservice in the **Expense Tracker App** ecosystem. It is responsible for identity management, user authentication, role-based authorization, JWT token management, and security audit logging across all current and future microservices (e.g., Ledger Service, Templatization Service, Notification Service).

- **Service Name**: `AuthService`
- **Scope**: Global Expense Tracker Platform
- **Primary Source**: [Notion PRD](https://app.notion.com/p/Expense-Tracker-App-3b3f14429b7c80eeb3b4f34ee0882991?source=copy_link)
- **Target Audience**: Developers, System Architects, Security Auditors

---

## 2. Goals & Objectives
1. **Stateless Authentication**: Implement secure, scalable JSON Web Token (JWT) authentication across microservices.
2. **Centralized User Management**: Single source of truth for user credentials, profiles, roles, and status.
3. **Role-Based Access Control (RBAC)**: Enforce granular permissions (e.g., `USER`, `ADMIN`, `SERVICE_ACCOUNT`).
4. **Token Lifecycle Management**: Support short-lived access tokens and secure refresh token rotation/revocation.
5. **Security & Cryptography Standards**: Enforce BCrypt password hashing, TLS transport security, and token signature validation.

---

## 3. Functional Requirements

### 3.1 User Registration & Profile Management
- **Endpoint**: `POST /api/v1/auth/register`
- **Input Fields**:
  - `email` (String, Required, Unique, Valid Email Format)
  - `password` (String, Required, Min 8 characters, complex rule)
  - `firstName` (String, Required)
  - `lastName` (String, Optional)
- **Validation**:
  - Reject duplicate emails (`409 Conflict`).
  - Hash passwords using **BCrypt** (salt factor $\ge 10$) before persistence.
  - Assign default role (`ROLE_USER`).

### 3.2 User Authentication (Login)
- **Endpoint**: `POST /api/v1/auth/login`
- **Input Fields**:
  - `email` (String, Required)
  - `password` (String, Required)
- **Output Fields**:
  - `accessToken` (String, JWT Access Token, Expiration: 15 mins)
  - `refreshToken` (String, Secure Refresh Token UUID, Expiration: 7 days)
  - `tokenType` (String, `"Bearer"`)
  - `expiresIn` (Number, seconds)
  - `user` (Object: `id`, `email`, `roles`)

### 3.3 Token Refresh & Rotation
- **Endpoint**: `POST /api/v1/auth/refresh`
- **Input Fields**:
  - `refreshToken` (String, Required)
- **Behavior**:
  - Validate refresh token against database/cache.
  - If valid and not expired, generate a **new** Access Token and rotate the Refresh Token.
  - Revoke old refresh tokens upon usage to prevent replay attacks.

### 3.4 Token Validation (Internal Microservice Endpoint)
- **Endpoint**: `POST /api/v1/auth/validate` or header-based filter validation
- **Behavior**:
  - Verify signature using HMAC-SHA256 / RSA.
  - Check expiration (`exp` claim).
  - Extract claims (`sub`, `roles`, `userId`).

### 3.5 Logout & Revocation
- **Endpoint**: `POST /api/v1/auth/logout`
- **Behavior**:
  - Invalidate the active Refresh Token in storage.
  - (Optional) Add Access Token JTI to Redis blacklist until expiration.

---

## 4. Non-Functional Requirements
- **Performance**: Sub-50ms response time for token validation calls; sub-150ms for login/register.
- **Scalability**: Stateless JWT verification allows horizontal scaling of downstream microservices without querying `AuthService` on every request.
- **High Availability**: 99.9% uptime requirement; database replication for auth store.
- **Security**:
  - No plain-text passwords stored anywhere.
  - HTTPS only.
  - CORS strictly configured for trusted client origins.
  - Rate limiting on login/register endpoints to prevent brute-force attacks.

---

## 5. Developer Integration Mapping
| Field / Concept | Data Type | Description | Source / Requirement |
| :--- | :--- | :--- | :--- |
| `userId` | UUID / Long | Unique primary key for user account | Database / JWT `sub` claim |
| `email` | String | User's primary login credential | Registration Payload |
| `passwordHash` | String | BCrypt encrypted hash | AuthService Internal Storage |
| `roles` | Array[String] | Granted permissions (e.g., `ROLE_USER`, `ROLE_ADMIN`) | User Entity & JWT Claim |
| `accessToken` | String (JWT) | Signed token sent in `Authorization: Bearer <token>` | Issued on Login/Refresh |
| `refreshToken` | String | Persistent token used to obtain new access tokens | Stored in DB / Redis |
