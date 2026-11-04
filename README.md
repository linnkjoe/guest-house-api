# 🏨 Motel API (Mabjaia Inn)

A RESTful API for complete management of the **Mabjaia Inn** Guest House, built with **Java 26** and **Spring Boot**. The solution provides advanced features for customer and room management, static and custom accommodation packages, business rules for check-in/check-out, automated cleaning and expiration workflows, an internal message/support channel, JWT authentication, Google OAuth 2.0 integration, and email verification via SMTP.

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

- **Language:** Java 26
- **Framework:** Spring Boot, Spring Security, Spring Data JPA, Hibernate
- **Database:** Oracle Database
- **Authentication & Security:** JWT (JSON Web Tokens), OAuth2 Client (Google), BCrypt
- **Communication:** Spring Mail / SMTP
- **Documentation:** Springdoc OpenAPI / Swagger UI
- **Build & Testing:** Maven, JUnit 5, Mockito

---

## 🗄️ Database Model (Entity Relationship)

The diagram below represents the complete and updated database structure of the system on **Oracle Database**:

![Database Entity Relationship Diagram](Untitled%20diagram-2026-11-04-132447.jpg)

---

## ⚙️ Configuration and Setup

### Prerequisites

- **Java 26** installed and configured.
- **Maven 3.8+**
- Running **Oracle Database** instance.
- Google Cloud Account with OAuth Client ID (optional for social login).
- SMTP Server (e.g., Gmail App Password) for sending email verification codes.

### 1. Clone the Repository

```bash
git clone https://github.com/linnkjoe/guest-house-api.git
cd guest-house-api
```

### 2. Database Migrations & Scripts

The project uses `spring.jpa.hibernate.ddl-auto=validate`. Execute the scripts in the following order inside your Oracle instance before running the application for the first time:

1. `database/security-schema.sql` — Base schema for users, clients, and roles.
2. `database/packs-customization-schema.sql` — Tables for stay customization and additional features.
3. `database/manager-account-enabled-migration.sql` — Migration for manager account enablement.
4. `database/manager-first-login-password-migration.sql` — Support for forced password update on first access.
5. `database/booking-prepayment-migration.sql` — Pre-payment/deposit fields and reservation expiration deadlines.
6. `database/pack-catalog-image-promotion-migration.sql` — Package metrics, photos, and discounts.
7. `database/about-page-migration.sql` — Institutional About Us page structure.
8. `database/contact-messages-migration.sql` — Contact messages inbox.
9. `database/contact-message-replies-migration.sql` — Support for conversation threads and replies.
10. `database/staff-contact-message-migration.sql` — Permission for staff-initiated conversations.
11. `database/staff-username-domain-migration.sql` — Username migration for staff adhering to the `@mbj.inn` domain rule.

### 3. Environment Variables (`.env`)

Create or update the `.env` file at the root of your backend project with your credentials:

```properties
# Oracle Database
SPRING_DATASOURCE_URL=jdbc:oracle:thin:@localhost:1521:xe
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password

# Authentication & JWT
JWT_SECRET=YourSuperSecureSecretKeyThatIsAtLeast32CharactersLong
BOOTSTRAP_ADMIN_PASSWORD=InitialAdminPassword123!

# Google OAuth2 Integration
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
GOOGLE_REDIRECT_URI=http://localhost:5173/backend/auth/google/callback
GOOGLE_FRONTEND_REDIRECT_URI=http://localhost:5173/

# SMTP Server (Email / OTP Sending)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password
MAIL_FROM=your-email@gmail.com
```

### 4. Run the Application

```bash
mvn spring-boot:run
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

### Oracle Schema - Overview
![Diagram](images/Diagram.PNG)

### Swagger UI - Overview
![Swagger Home](images/swagger-home.PNG)

### Booking Management
![Booking Endpoints](images/swagger-bookings.PNG)

### Sample API Response
![API Response](images/swagger-response.PNG)

---

## 👨‍💻 Author

**Nélio João Moanga**  
Computer Science Student | Java / Spring Boot Backend Developer  
Maputo, Mozambique  

- **GitHub:** [linnkjoe](https://github.com/linnkjoe)  
- **LinkedIn:** [Nélio Moanga](https://www.linkedin.com/in/linnkjoe/)
