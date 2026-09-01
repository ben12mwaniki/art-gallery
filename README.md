# Online Art Gallery

A multi-tier art marketplace backend built with **Spring Boot, Java, and PostgreSQL**.

Art Gallery connects artists with customers, allowing artists to showcase and sell artwork while customers browse available pieces, manage a shopping cart, and place orders. The system is exposed as a REST API designed to support web and mobile clients.

This project was originally developed as a term project for **ECSE 321 - Introduction to Software Engineering** at McGill University. The project followed a complete software engineering lifecycle, encompassing requirements analysis, multi-tier architecture design, implementation, validation, and automated deployment executed through agile sprints.

The original team consisted of:

1. Ben Mwaniki
2. Xirui Zhang
3. Kaicheng Wu
4. Kaan Gure
5. Zeyang Xu

The original work is publicly available in this repository. Since the original project, I have independently maintained and substantially refactored the backend, improving its API design, validation, data integrity, testing, and documentation while extending the business model with order checkout and purchase history.

## Features

### 🖼️ Artwork Management

Artists can:

* Create and manage artwork listings
* Set price, available quantity, discount, and commission percentage
* Provide descriptions for artwork
* Associate artwork with an artist
* Remove artwork while preserving relevant historical order information

### 🛍️ Shopping Cart

Customers can:

* Create and manage a personal shopping cart
* Browse available artwork
* Add artwork to their cart
* Specify quantities
* Remove individual items
* Empty their cart
* Validate requested quantities against available inventory

### 💳 Checkout & Orders

Customers can:

* Checkout their shopping cart
* Automatically create an order from the contents of their cart
* View their order history
* Retrieve individual orders

During checkout, the backend:

1. Validates the customer and shopping cart
2. Validates that requested artwork is still active
3. Verifies sufficient inventory
4. Creates `OrderItem` records containing purchase-time information
5. Decrements artwork inventory
6. Creates the order
7. Clears the customer's shopping cart

`OrderItem` stores a snapshot of relevant artwork information at the time of purchase, including the list price, effective unit price, discount, commission, artwork name, and description. This allows historical orders to retain purchase information even if the underlying artwork is subsequently modified or removed.

### 👤 User Accounts

The system supports three user roles:

* **Artists** — manage their artwork catalogue
* **Customers** — manage their account, shopping cart, and order history
* **Administrators** — manage platform-level information

## API Design

The backend exposes a RESTful HTTP API with separate request and response DTOs.

### Request DTOs

Client-provided data is submitted through request bodies rather than placing large amounts of information in URL parameters. Request DTOs also provide declarative input validation using Jakarta/Java Bean Validation annotations.

For example, adding an item to a shopping cart uses a request body containing the artwork ID and requested quantity.

### Response DTOs

Responses are converted from domain entities into dedicated response DTOs rather than directly serializing JPA entities.

This prevents the API from exposing the persistence model and allows responses to present only the information relevant to the client. It also avoids exposing internal entity relationships through JSON serialization.

### Error Handling

The API uses a global exception handler to provide consistent error responses.

The handler currently provides responses for:

* Invalid business or request arguments — `400 Bad Request`
* Request validation failures — `400 Bad Request`
* Missing resources — `404 Not Found`
* Unexpected server errors — `500 Internal Server Error`

Validation errors include both a human-readable summary and field-level validation messages.

## Application Architecture

Art Gallery follows a layered backend architecture:

```text
     Client (Web / Mobile)
             |
             v
      REST Controller
             |
             v
        Service Layer
             |
             v
      Repository (DAO)
             |
             v
       PostgreSQL Database
```

The application separates:

* **Controllers** — HTTP request/response handling and DTO conversion
* **Services** — business rules, validation, transactions, and application logic
* **Repositories** — persistence and database access
* **Models** — domain entities
* **DTOs** — API request and response representations

This separation keeps the backend modular, testable, and independent of the client applications.

For more detailed architecture information:

➡️ [Backend documentation](./GallerySystem-Backend/backend-documentation.md)

## Testing

The backend has been thoroughly tested at multiple levels.

### Unit Tests

Business logic and individual components are tested independently to verify expected behavior and error conditions.

### Persistence Tests

The persistence layer is tested against the application's JPA entities and PostgreSQL database interactions, including entity relationships, cascading behavior, foreign-key constraints, and deletion behavior.

### Controller Integration Tests

The REST API is exercised through HTTP requests against the Spring controllers to verify complete request/response behavior, including:

* Request validation
* HTTP status codes
* Request DTO handling
* Response DTO conversion
* Error responses
* Controller-to-service integration

The test suite is intended to validate both normal application flows and invalid or exceptional inputs.

Run the complete test suite with:

```bash
./gradlew test
```

Test reports are generated at:

```text
build/reports/tests/test/index.html
```

## API Documentation

The REST API is documented using **OpenAPI/Swagger** annotations.

Once the application is running, the generated API documentation can be accessed through the Swagger UI.

The API documentation describes the available endpoints, HTTP methods, request bodies, response types, and endpoint descriptions.

The deployed API is publicly accessible at:

**[Online Gallery API](https://p01--art-gallery--95bbvq7j5jmw.code.run/)**

The root endpoint currently displays the application's online gallery response.

## Deployment & Containerization

The backend is containerized using **Docker**.

The project includes a Dockerfile for building the Spring Boot application into a portable container image. PostgreSQL is also configured through Docker Compose for local development.

The standard local workflow is therefore:

```bash
docker compose up
```

To stop the application and database:

```bash
docker compose down
```

This provides a consistent development environment without requiring a locally installed PostgreSQL server or manually configured database credentials.

The application is hosted using **Northflank**. The deployed Spring Boot container runs as a continuously available service with PostgreSQL provided separately as a managed database service.

The hosted API is available at:

```text
https://p01--art-gallery--95bbvq7j5jmw.code.run/
```

## Technology Stack

| Layer             | Technology                                   |
| ----------------- | -------------------------------------------- |
| Backend           | Spring Boot / Java                           |
| Database          | PostgreSQL                                   |
| Persistence       | Spring Data JPA / Hibernate                  |
| Build Tool        | Gradle                                       |
| API               | REST                                         |
| API Documentation | OpenAPI / Swagger                            |
| Testing           | JUnit 5, Mockito, Spring integration testing |
| Frontend          | Vue.js                                       |
| Containerization  | Docker / Docker Compose                      |
| Hosting           | Northflank                                   |

## Running Locally

### Prerequisites

* Docker
* Docker Compose
* Git

A local JDK is not required for the standard Docker-based workflow.

### Setup

Clone the repository:

```bash
git clone https://github.com/ben12mwaniki/art-gallery.git
cd art-gallery
```

The backend and frontend are maintained as separate project directories. The backend's Docker configuration is located under:

```text
GallerySystem-Backend/
```

### Running the Application with Docker Compose

Navigate to the backend directory:

```bash
cd GallerySystem-Backend
```

Start the application and PostgreSQL database:

```bash
docker compose up
```

Docker Compose starts the required services and connects the Spring Boot application to PostgreSQL using the configured container environment.

The API will be available at:

```text
http://localhost:8080
```

Swagger UI is available through the local application once the backend has started.

To stop the services:

```bash
docker compose down
```

To stop the services and remove the database volume as well:

```bash
docker compose down -v
```

> **Warning:** Removing the volume deletes the local PostgreSQL data stored by Docker Compose.

### Alternative Local Development

Running the backend directly with Gradle remains useful for development, debugging, and testing:

```bash
./gradlew bootRun
```

However, Docker Compose is the recommended standard setup because it provides both the application and its PostgreSQL dependency in a reproducible environment.

## Project Evolution

This repository began as a university software engineering project and has since undergone independent development.

Major backend improvements include:

* Added an `OrderItem` entity to support checkout and preserve purchase-time order information
* Implemented shopping cart checkout and order history
* Added purchase-time price and artwork snapshots to order items
* Refactored API endpoints to use structured request bodies and request DTOs
* Added Bean Validation to API request objects
* Refactored API responses to use dedicated response DTOs rather than exposing JPA entities
* Added centralized global exception handling
* Hardened service-layer validation and replaced failure-prone null handling with explicit application exceptions
* Improved data-integrity handling across entity relationships and deletion operations
* Built a comprehensive automated test suite covering unit, persistence, and controller integration behavior
* Added OpenAPI/Swagger API documentation
* Containerized the backend and PostgreSQL development environment using Docker and Docker Compose
* Deployed the backend API to Northflank

The backend API is now **feature-complete and deployed as a functional hosted service**. Further development is focused primarily on authentication, frontend modernization, and additional production-oriented improvements.

## Project Status

**Backend:** Feature-complete, containerized, tested, documented, and deployed as a publicly accessible API.

**Frontend:** The existing Vue.js client remains part of the original project and requires further modernization to align with the current backend API.

**Hosting:** The Spring Boot API is deployed on Northflank and is publicly accessible.

**Next steps:**

1. **Add authentication and authorization** — introduce secure user authentication and role-based access control for artists, customers, and administrators.
2. **Evolve the frontend** — modernize the Vue.js client and integrate it fully with the current REST API.
3. **Continue automated testing** — expand coverage as authentication and frontend/API integration are introduced.
4. **Improve production readiness** — add health checks, more robust deployment configuration, monitoring, and other operational improvements as the application matures.
5. **Refine API documentation** — continue improving OpenAPI descriptions and examples as the API evolves.

## Contributors

* Ben Mwaniki
* Xirui Zhang
* Kaicheng Wu
* Kaan Gure
* Zeyang Xu
