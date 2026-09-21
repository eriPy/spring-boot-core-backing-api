# Core Banking System API

A RESTful API built with Java and Spring Boot that simulates the backend logic of a core banking system. It strictly handles account management, secure transaction processing (deposits, withdrawals, transfers), and an automated penalty system for suspicious activity.

## About the Project
This project was built to implement complex security flows in a financial context. Instead of simple passwords, the system uses a strict JWT-based authorization flow and short-lived OTPs (One-Time Passwords) for critical operations. It utilizes Redis for caching, rate-limiting, and temporary token storage to prevent brute-force attacks, alongside a relational database to maintain transactional integrity and account history.

## Technologies
*   **Java 17+**
*   **Spring Boot** (Web, Security, Data JPA)
*   **JSON Web Tokens (JWT)** (io.jsonwebtoken)
*   **Redis** (Spring Data Redis) for rate-limiting and temporary state.
*   **PostgreSQL** (via Hibernate/JPA)
*   **Swagger / OpenAPI 3** for API documentation.

## Structure
The project follows a standard Spring Boot layered architecture:

*   **`controllers`**: Exposes the REST API endpoints. Includes `AccountController` for user details and `OperationController` for transactions.
*   **`service`**: Contains the core business logic.
    *   `OperationService`: Handles transaction execution (deposit, withdrawal, transfer) and OTP generation[cite: 27].
    *   `SecurityService`: A critical module that handles the penalty logic, account suspensions, and rate-limiting validations against Redis[cite: 28].
    *   `TokenService`: Responsible for signing, generating, and parsing JWTs[cite: 29].
*   **`model`**: Contains JPA Entities (`Account`, `Operation`, `AuthToken`, `AccountPenalties`, `SuspendedAccount`).
*   **`repository`**: Spring Data JPA interfaces for database interaction[cite: 21, 22, 23, 24, 25].
*   **`config`**: Security filters (`JwtAuthFilter`, `SecurityConfig`) and OpenAPI configuration[cite: 1, 2, 3].
*   **`dto`**: Data Transfer Objects used for API requests and responses[cite: 6, 7, 8, 9, 10, 11].
*   **`exception`**: Custom exceptions (`AccountException`, `RateLimitExceededException`, etc.)[cite: 12, 13, 14, 15].

## Security & Penalty System
This system features an aggressive security mechanism to prevent abuse:

1.  **JWT Authentication:** All private routes require a `Bearer` token in the header[cite: 1].
2.  **Redis Rate Limiting:** The `SecurityService` checks Redis to see if an account is temporarily banned (5 or 24-minute blocks) before allowing requests[cite: 28].
3.  **Strike System (`AccountPenalties`)**:
    *   If a user requests more than 5 OTP tokens within a 5-minute window, the system triggers a penalty[cite: 28].
    *   **Strikes**: Accumulating 3 strikes results in an account ban/suspension[cite: 28].
    *   **Suspensions**: A 1st ban suspends the account for 1 day. A 2nd ban suspends it for 7 days. A 3rd ban closes the account permanently[cite: 28]. Suspended accounts are tracked in the `SuspendedAccount` table[cite: 20].

## How to use

### 1. Get Initial Authorization
Before doing anything, get a session JWT.
*   **Endpoint:** `GET /api/me/auth/{id}`
*   **Action:** Returns a JWT. Use this token in the `Authorization` header (`Bearer <token>`) for all subsequent requests.

### 2. Performing a Transaction
Transactions require a 3-step validation process.

*   **Step A - Request OTP:** `GET /api/transactions/token/{token_type}` (e.g., `withdrawal`, `transfer`). The system generates a 6-digit alphanumeric token.
*   **Step B - Validate OTP:** `POST /api/transactions/token/{operation_type}`. Send the 6-digit token in the body (`TokenRequest`). The server responds with a temporary, specific JWT for this operation.
*   **Step C - Execute:** `POST /api/transactions/transaction`. Send the specific operation JWT, the `operation` type, the `amount`, and the `payee_id` (if transferring) in the body (`OperationRequest`).

### 3. Account Management
*   **View Transactions:** `GET /api/me/transactions/{id}`
*   **View Specific Transaction:** `GET /api/me/transaction/{transactionId}`
*   **Disable Account:** `PATCH /api/me/disable` (Requires account balance to be zero).

## 💡 Architecture & Lessons Learned
As this project scaled, certain architectural limitations became apparent—particularly around the multi-step JWT and OTP validation flows for sensitive transactions. While the current implementation successfully enforces strict security and transactional integrity, handling temporary state tokens across sequential requests highlighted important lessons in **scalability and design continuity**. These insights will be directly applied to future, larger-scale backend architectures to ensure cleaner separation of concerns and smoother token lifecycles from day one.