🏨 Guest House API

A RESTful API for managing a Guest House, built with Java 26 and Spring Boot. This project provides a backend solution for managing guests, rooms, accommodation packages, and bookings while ensuring room availability through business rules.

🚀 Features

- 👤 Guest Management (CRUD)
- 🛏️ Room Management (CRUD)
- 📦 Package Management (CRUD)
- 📅 Booking Management
- ✅ Guest Check-in
- 🚪 Guest Check-out
- 🔍 Automatic Room Availability Validation
- 📌 Booking Status Management
- ⚠️ Global Exception Handling
- 🧪 Unit Testing with JUnit 5 & Mockito 
- 🌐 RESTful API Architecture

🛠️ Tech Stack

- Java 26
- Spring Boot
- Spring Data JPA
- Hibernate
- Oracle Database
- Maven
- JUnit 5
- Mockito

📁 Project Structure

src
├── controller
├── service
├── repository
├── model
├── dto
├── exception
└── resources

⚙️ Getting Started

Prerequisites

- Java 26
- Maven
- Oracle Database

Clone the Repository

git clone https://github.com/linnkjoe/guest-house-api.git

Navigate to the Project

cd guest-house-api

Configure the Database

Update the "application.properties" file with your Oracle Database configuration.

Example:

spring.datasource.url=jdbc:oracle:thin:@localhost:1521:xe
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

Run the Application

mvn spring-boot:run

The API will be available at:

http://localhost:8080

📌 Main Endpoints

Resource| Endpoint
Guests| "/guests"
Rooms| "/rooms"
Packages| "/packages"
Bookings| "/bookings"

📖 Booking Workflow

1. Create a booking.
2. The system validates room availability.
3. Perform guest check-in.
4. Perform guest check-out.
5. The room becomes temporarily unavailable for cleaning.
6. After the cleaning period, the room becomes available again.

🧪 Running Tests

Execute the test suite using:

mvn test

Unit tests are being implemented using:

- JUnit 5 for test execution and assertions.
- Mockito for mocking dependencies and isolating business logic.


👨‍💻 Author

Nélio Moanga

Computer Science Student | Backend Developer

GitHub: https://github.com/linnkjoe

---

Feedback and suggestions are welcome.
