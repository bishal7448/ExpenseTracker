# JWT Authentication Low-Level Design (LLD) & Sequence Diagram

## 1. Overview
This Low-Level Design (LLD) document provides a step-by-step technical breakdown of the JSON Web Token (JWT) lifecycle, class interactions, execution sequence, and cross-microservice integration within the **Expense Tracker App**.

- **Primary Reference**: [Excalidraw Diagram - JWT Auth LLD & Templatization Flow](https://excalidraw.com/#json=jf5EorV2iGUKGhuqwND4C,UYUXW1P3Gv3OGs54gWtF8w)
- **Target Audience**: Backend Developers, Integration Engineers

---

## 2. Low-Level Component Interactions

### 2.1 Sequence Diagram: Login & Token Issuance

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant AuthCtrl as AuthController
    participant AuthMgr as AuthenticationManager
    participant UserDetailsService as UserDetailsServiceImpl
    participant DB as SQL Database
    participant JWTService as JWTService
    participant RefreshService as RefreshTokenService
    participant EventPublisher as EventPublisher

    Client->>AuthCtrl: POST /api/v1/auth/login { email, password }
    AuthCtrl->>AuthMgr: authenticate(UsernamePasswordAuthenticationToken)
    AuthMgr->>UserDetailsService: loadUserByUsername(email)
    UserDetailsService->>DB: SELECT * FROM users WHERE email = ?
    DB-->>UserDetailsService: User Entity (hashed password, roles)
    UserDetailsService-->>AuthMgr: UserDetails Object
    AuthMgr->>AuthMgr: Verify password using BCrypt
    alt Authentication Failed
        AuthMgr-->>AuthCtrl: BadCredentialsException
        AuthCtrl-->>Client: 401 Unauthorized
    else Authentication Successful
        AuthMgr-->>AuthCtrl: Authentication Object
        AuthCtrl->>JWTService: generateAccessToken(userDetails)
        JWTService-->>AuthCtrl: Access Token (JWT String)
        AuthCtrl->>RefreshService: createRefreshToken(user.getId())
        RefreshService->>DB: INSERT INTO refresh_tokens (user_id, token, expiry)
        DB-->>RefreshService: Saved RefreshToken Entity
        RefreshService-->>AuthCtrl: RefreshToken String
        AuthCtrl->>EventPublisher: publishEvent(UserLoginSuccessEvent)
        AuthCtrl-->>Client: 200 OK { accessToken, refreshToken, tokenType: "Bearer", expiresIn }
    end
```

---

### 2.2 Sequence Diagram: Request Interception & Token Validation

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant JWTFilter as JWTAuthFilter
    participant JWTService as JWTService
    participant UserDetailsService as UserDetailsServiceImpl
    participant SecCtx as SecurityContextHolder
    participant Controller as Protected Endpoint (Controller)

    Client->>JWTFilter: Request HTTP GET /api/v1/expenses (Header: Authorization: Bearer <JWT>)
    JWTFilter->>JWTFilter: Extract token from Authorization header
    alt No Token or invalid Header
        JWTFilter->>Controller: Continue Filter Chain (SecurityContext empty)
    else Token Present
        JWTFilter->>JWTService: extractUsername(token)
        JWTService-->>JWTFilter: email / username
        JWTFilter->>UserDetailsService: loadUserByUsername(email)
        UserDetailsService-->>JWTFilter: UserDetails
        JWTFilter->>JWTService: isTokenValid(token, userDetails)
        alt Token Invalid or Expired
            JWTFilter-->>Client: 401 Unauthorized (Expired JWT)
        else Token Valid
            JWTFilter->>SecCtx: setAuthentication(UsernamePasswordAuthenticationToken)
            JWTFilter->>Controller: Chain proceed (Request authenticated)
            Controller-->>Client: 200 OK Response Data
        end
    end
```

---

### 2.3 Sequence Diagram: Refresh Token Rotation Flow

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client App
    participant TokenCtrl as TokenController / AuthController
    participant RefreshService as RefreshTokenService
    participant DB as SQL Database
    participant JWTService as JWTService

    Client->>TokenCtrl: POST /api/v1/auth/refresh { refreshToken }
    TokenCtrl->>RefreshService: findByToken(refreshToken)
    RefreshService->>DB: SELECT * FROM refresh_tokens WHERE token = ?
    DB-->>RefreshService: RefreshToken Entity
    alt Token Not Found or Expired
        RefreshService-->>TokenCtrl: RefreshTokenException
        TokenCtrl-->>Client: 403 Forbidden ("Refresh token expired or revoked")
    else Token Valid
        RefreshService->>RefreshService: verifyExpiration(token)
        RefreshService->>JWTService: generateAccessToken(user)
        JWTService-->>RefreshService: New Access Token
        RefreshService->>RefreshService: Rotate Token (Generate new RefreshToken & delete old)
        RefreshService->>DB: UPDATE refresh_tokens SET token = new_token, expiry = new_expiry
        RefreshService-->>TokenCtrl: New Access Token + New Refresh Token
        TokenCtrl-->>Client: 200 OK { accessToken, refreshToken }
    end
```

---

## 3. Microservice Integration: Templatization & Ledger Service Flow

As shown in the architecture diagrams, downstream services interact with `AuthService` and consume user context dynamically:

```mermaid
flowchart LR
    subgraph Client Space
        Client[Client App]
    end

    subgraph Service Ecosystem
        Auth[AuthService]
        Templatization[Templatization Service]
        Ledger[Ledger Service]
    end

    Client -->|1. Request: Spending Data| Templatization
    Templatization -->|2. Verify User Auth Token| Auth
    Auth -->|3. Validated Session User ID| Templatization
    Templatization -->|4. Send Processed Ledger Data| Ledger
    Templatization -->|5. Dynamic Placeholder Binding p1| Templatization
```

### 3.1 Dynamic Template Placeholder Binding Mechanics
- **Trigger**: Client requests a template output (e.g. spending notification report).
- **Backend Placeholder Payload**:
  $$\text{Placeholder } p_1 = \{\text{ spending\_category\_evaluation }\trunk\}$$
- **Evaluated Categories**:
  - `-> Risk`: High budget variance / abnormal spending.
  - `-> Stable`: Within allocated budget limits.
  - `-> Not at all`: No activity detected.
- **Output**: Templatization Service binds $p_1$ dynamically and passes the formatted payload to destination services (e.g., Notification Service, Email Gateway, Ledger Service).

---

## 4. Class & Method Mapping Specification

| Service / Class | Method Signature | Input Parameters | Output / Return Type | Purpose |
| :--- | :--- | :--- | :--- | :--- |
| `JWTService` | `generateToken(UserDetails userDetails)` | `UserDetails` | `String` (JWT) | Generates signed 15-minute access token |
| `JWTService` | `extractUsername(String token)` | `String` | `String` | Extracts `sub` claim (user email) |
| `JWTService` | `isTokenValid(String token, UserDetails userDetails)` | `String`, `UserDetails` | `boolean` | Verifies signature & expiration timestamp |
| `RefreshTokenService` | `createRefreshToken(Long userId)` | `Long` | `RefreshToken` | Creates & persists refresh token in DB |
| `RefreshTokenService` | `verifyExpiration(RefreshToken token)` | `RefreshToken` | `RefreshToken` | Checks if refresh token is past `expiryDate` |
| `JWTAuthFilter` | `doFilterInternal(...)` | `HttpServletRequest`, `HttpServletResponse`, `FilterChain` | `void` | Intercepts HTTP request & sets security context |
