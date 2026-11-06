# 🏨 Motel API (Mabjaia Inn)

A RESTful API for complete management of the **Mabjaia Inn** Guest House, built with **Java 21** and **Spring Boot**. The solution provides advanced features for customer and room management, static and custom accommodation packages, business rules for check-in/check-out, automated cleaning and expiration workflows, an internal message/support channel, JWT authentication, Google OAuth 2.0 integration, and email verification via SMTP.

---

## 🚀 Features

- 👤 **User and Customer Management**:
  - Customer registration with email verification via a 6-digit OTP code sent through SMTP.
  - Social login integrated via **Google OAuth 2.0**.
  - Granular profiles and permissions (`ADMIN`, `GESTOR`, `CLIENTE`).
  - Forced password change on first login for new managers.
  - Mandatory `@mbj.inn` domain for administrative accounts (`ADMIN` / `GESTOR`).
- 🛏️ **Room Management**:
  - Full CRUD operations for rooms.
  - Room status tracking (`DISPONIVEL`, `RESERVADO`, `OCUPADO`, `EM_LIMPEZA`, `MANUTENCAO`).
  - Automated scheduled cleaning workflow (30 minutes) and post-cleaning availability reset.
- 📦 **Packages and Customization**:
  - Static package catalog with photos, request counts, and promotional discounts.
  - **Extras/Amenities (Features)** module (e.g., air conditioning, pool access, VIP service).
  - Dynamic pricing calculation for **Custom Packages (Custom Packs)** based on base stay, added extras, and extra hours.
- 📅 **Bookings and Stays**:
  - Strict validation of room availability and schedule overlap checks.
  - Simulated pre-payment workflow (50% deposit via Credit Card, M-Pesa, or e-Mola).
  - Stay extensions with automatic extra-hour rate calculation (`app.pricing.extra-hour-rate`).
  - Cancellation and immediate availability release.
  - Automatic alerts when checkout time is approaching (e.g., notification 10 minutes prior to closing).
- 💬 **Messaging and Contact Support**:
  - Bidirectional conversation channel organized by topic between customers and the guest house staff.
  - Support for threads initiated by either the customer or the staff (`ADMIN` / `GESTOR`).
- ℹ️ **About Us Page Management**:
  - Institutional content editable by the Admin (text, contact details, address, and image gallery).
- 📊 **Business Reports**:
  - Generation of monthly revenue reports, most requested packages, and top customers.
- 🔐 **Security and Documentation**:
  - Stateless authentication via JWT.
  - Interactive OpenAPI 3 / Swagger UI documentation.
  - Unit test coverage at the service layer using JUnit 5 and Mockito.

---

## 🛠️ Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot, Spring Security, Spring Data JPA, Hibernate
- **Database:** PostgreSQL
- **Authentication & Security:** JWT (JSON Web Tokens), OAuth2 Client (Google), BCrypt
- **Communication:** Spring Mail / SMTP
- **Documentation:** Springdoc OpenAPI / Swagger UI
- **Build & Testing:** Maven, JUnit 5, Mockito

---

## 🗄️ Database Model (Entity Relationship)

The diagram below represents the database structure used by the application:

![Database Entity Relationship Diagram](images/Diagram.png)

---

## ⚙️ Configuration and Setup

### Prerequisites

- **Java 21** installed and configured.
- **Maven 3.8+**
- Running **PostgreSQL** instance.
- Google Cloud Account with OAuth Client ID (optional for social login).
- SMTP Server (e.g., Gmail App Password) for sending email verification codes.

### 1. Clone the Repository

```bash
git clone https://github.com/linnkjoe/guest-house-api.git
cd guest-house-api
```

### 2. Local Configuration

Copy `.env.example` to `.env`, then set the PostgreSQL credentials and generate unique values for `JWT_SECRET` and `BOOTSTRAP_ADMIN_PASSWORD`. Never commit `.env` or real OAuth/SMTP credentials.

If a credential was previously pushed to a public repository, revoke or rotate it; adding it to `.gitignore` does not remove it from Git history.

```properties
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mabjaiainn
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<your-local-password>
JWT_SECRET=<unique-random-value-of-at-least-32-bytes>
BOOTSTRAP_ADMIN_PASSWORD=<unique-initial-admin-password>
```

`DDL_AUTO=update` creates/updates tables from the JPA entities for a new database. For production schema evolution, use versioned PostgreSQL migrations rather than relying on automatic schema updates. Existing Oracle data and Oracle-specific SQL migrations are not converted automatically.

### 3. Deploy on Render

1. Create a persistent PostgreSQL database on Render.
2. Create a **Web Service** from this repository and select **Docker** as the runtime. Set the service root to the backend project directory and the Dockerfile path to `Dockerfile`.
3. Configure the service environment variables from `.env.example`. Set `SPRING_DATASOURCE_URL` to the PostgreSQL **internal** JDBC URL (`jdbc:postgresql://<internal-host>:5432/<database>`), plus the database username and password from Render. Do not paste credentials into source files.
4. Generate a unique `JWT_SECRET` (at least 32 bytes) and set a strong `BOOTSTRAP_ADMIN_PASSWORD`. Configure `CORS_ALLOWED_ORIGINS` and OAuth redirect URLs with the deployed frontend/API URLs if those integrations are enabled.
5. Keep `DDL_AUTO=update` for the first deployment to create the schema. Back up the database before future schema changes.

The application listens on Render's `PORT` environment variable (default `8080`). A new PostgreSQL database starts empty; the first admin is created from the bootstrap environment variables. Do not reuse the Oracle database credentials or assume Oracle records have been migrated.

The Docker runtime uses the `Africa/Maputo` timezone for booking validation, scheduled tasks, and application-generated local timestamps.

Email verification uses SMTP. Render Free web services block outbound ports 25, 465, and 587, so Gmail SMTP on port 587 cannot send from a Free service. Use a paid backend service plan that permits port 587, or integrate an email provider's HTTPS API. SMTP attempts are limited to 10 seconds, and the registration request reports a timeout instead of waiting indefinitely.

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

The API will be available at: `http://localhost:8080`

---

## 📖 API Documentation (Swagger UI)

After starting the server, access the interactive UI to explore and test all available endpoints:

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Spec (JSON):** `http://localhost:8080/v3/api-docs`

---

## 🔐 Security and Authentication

### Authentication Rules

- **Conventional Login:** `POST /auth/login` returns a Bearer JWT containing `token`, `role`, and `clientId`.
- **Social Login (Google OAuth 2.0):** Employs the *Authorization Code* flow. The backend validates `state`, `nonce`, signature, and verified email. New users registering via Google complete registration by providing only their phone number.
- **Staff Rules (`ADMIN` / `GESTOR`):** Staff usernames must end with `@mbj.inn`. Newly created managers receive a temporary password and are blocked from performing operations until they log in and update their password (`PUT /users/me/password`).
- **Email Verification:** For standard client registration, a 6-digit OTP code is dispatched via SMTP. The code expires in 10 minutes (maximum of 5 attempts).

### Role-Based Access Control (RBAC) Matrix

| Endpoint / Resource Group | Public | CLIENTE | GESTOR | ADMIN |
|---|:---:|:---:|:---:|:---:|
| `POST /auth/login`, `POST /auth/register`, `GET /auth/email-available` | ✅ | ✅ | ✅ | ✅ |
| `POST /auth/google`, `POST /auth/verify-email` | ✅ | ✅ | ✅ | ✅ |
| `POST /users` (Staff user creation) | ❌ | ❌ | ❌ | ✅ |
| `PATCH /users/{id}/enabled` | ❌ | ❌ | ❌ | ✅ |
| `PUT /users/me/password` | ❌ | ✅ | ✅ | ✅ |
| `GET /api/v1/reports/*` (Financial/metric reports) | ❌ | ❌ | ❌ | ✅ |
| `GET/POST/DELETE /clients` | ❌ | ❌ | ✅ | ✅ |
| `GET /clients/{id}` | ❌ | Own | ✅ | ✅ |
| `GET /rooms/available`, `GET /packs` | ✅ | ✅ | ✅ | ✅ |
| `POST /rooms`, `PUT /update/room`, `PATCH /rooms/{id}/*` | ❌ | ❌ | ❌ | ✅ |
| `POST /packs`, `DELETE /packs/{id}` | ❌ | ❌ | ❌ | ✅ |
| `GET/POST/DELETE /features` | ❌ | View active | ✅ | ✅ |
| `POST /custom-packs/quote` | ❌ | ✅ | ✅ | ✅ |
| `/clients/{id}/custom-packs/*` | ❌ | Own | ✅ | ✅ |
| `GET /bookings`, `GET /future-bookings` | ❌ | ❌ | ✅ | ✅ |
| `POST /bookings/{id}/check-in`, `check-out` | ❌ | ❌ | ✅ | ✅ |
| `POST /bookings/{id}/extend/{newCheckOut}` | ❌ | Own | ✅ | ✅ |
| `GET /contact-messages/*` | ❌ | Own | ✅ | ✅ |

---

## 🧪 Unit Tests

Run the unit test suite to validate business logic in the Service layer:

```bash
mvn test
```

Test coverage includes:
- Pricing and custom stay calculation rules (`PricingServiceTest`).
- Booking validation and lifecycle (`BookingServiceTest`).
- OTP verification rules and Google OAuth integration (`EmailVerificationServiceTest`, `GoogleAuthServiceTest`).
- Automated room scheduling and status management (`RoomSchedulerTest`, `RoomServiceTest`).

---

## 📸 Screenshots

### Swagger UI - Overview
![Swagger Home](images/swagger-home.PNG)

### Booking Management
![Booking Endpoints](images/swagger-bookings.PNG)

### Sample API Response
![API Response](images/swagger-response.PNG)

---

## 👨‍💻 Author

**Nélio João Moanga**  
Computer Science Student | Software Developer  
Maputo, Mozambique  

- **GitHub:** [linnkjoe](https://github.com/linnkjoe)  
- **LinkedIn:** [Nélio Moanga](https://www.linkedin.com/in/linnkjoe/)
