# Online Art Gallery 

A multi-tier art marketplace backend built with Spring Boot and PostgreSQL.

Art Gallery connects artists with customers, allowing artists to showcase and sell their artwork while customers browse, purchase, and manage orders through a personal shopping cart. The system is built as a REST API designed to serve both a web frontend and an Android client.

This project was originally developed as a term project for **ECSE 321 - Introduction to Software Engineering** at McGill University, Fall 2020, following a full software engineering lifecycle: requirements gathering, multi-tier system design, implementation, validation, and an automated release pipeline. Other contributors besides myself (ben12mwaniki) on the original team were:

1. Xirui Zhang
2. Kaicheng Wu
3. Kaan Gure
4. Zeyang Xu

The original work is made public here. Since then, I have continued independently maintaining, refactoring, and extending the project, including hardening backend validation, fixing data-integrity bugs, and rebuilding the automated test suite.

<!-- ## Live Demo

🔗 *[Add deployed application link here]*

## Screenshots

*[Add screenshots of the web frontend here]* -->

## Features

### 🖼️ Artwork Management
Artists can:
- List, update, and remove art pieces
- Set price, available quantity, discount, and commission percentage
- Attach a description to each piece

### 🛍️ Shopping & Orders
Customers can:
- Browse available art pieces
- Add items to a personal shopping cart
- Select delivery method (pickup or delivery)
- Place and view orders

### 👤 User Accounts
The system supports three distinct roles, each with their own account management:
- **Artists** — manage their own catalogue of art pieces
- **Customers** — manage a shipping address, shopping cart, and order history
- **Administrators** — oversee the platform

## Technology Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot (Java) |
| Database | PostgreSQL |
| Build Tool | Gradle |
| Testing | JUnit 5, Mockito |
| Frontend | Vue.js, Android |

## Application Overview

Art Gallery follows a layered backend architecture:

```
Client (Web / Android)
        |
        v
  REST Controller
        |
        v
   Service Layer
        |
        v
 Repository (DAO) Layer
        |
        v
  PostgreSQL Database
```

The application separates HTTP handling, business logic, and data access into distinct layers to keep the codebase modular and testable.

For detailed architecture information:
➡️ [Backend documentation](/GallerySystem-Backend/backend-documentation.md)
<!-- ➡️ *[Link to frontend documentation page]* -->

## Running Locally

### Prerequisites
- JDK 11
- Docker (for PostgreSQL) — or a local PostgreSQL 16 installation

### Setup

Clone the repository:
```bash
git clone https://github.com/ben12mwaniki/art-gallery.git
cd art-gallery/GallerySystem-Backend
```

Start a PostgreSQL instance matching the expected configuration:
```bash
docker run --name gallery-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=artgallery-db \
  -p 5432:5432 \
  -d postgres:15
```

### Running the Application

Start the Spring Boot server using the Gradle wrapper:
```bash
./gradlew bootRun
```

The application will be available at:
```
http://localhost:8080
```

Tables are created automatically on first run.

## Testing

Run the automated test suite:
```bash
./gradlew test
```

Test reports are generated at `build/reports/tests/test/index.html`.

## Project Status

Actively maintained. Currently completing a backend validation and test-coverage pass while migrating the Vue.js frontend to Vite. Backend work includes hardening input validation across create/update operations, closing data-integrity gaps (e.g. enforcing unique emails and validating entity relationships), and rebuilding the JUnit test suite to match the current implementation.

## Contributors
- Ben Mwaniki
- Xirui Zhang
- Kaicheng Wu
- Kaan Gure
- Zeyang Xu
