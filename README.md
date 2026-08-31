# Online Art Gallery

A multi-tier art marketplace backend built with **Spring Boot, Java, and PostgreSQL**.

Art Gallery connects artists with customers, allowing artists to showcase and sell artwork while customers browse available pieces, manage a shopping cart, and place orders. The system is exposed as a REST API designed to support web and mobile clients.

This project was originally developed as a term project for **ECSE 321 - Introduction to Software Engineering** at McGill University. The project followed a complete software engineering lifecycle, encompassing requirements analysis, multi‑tier architecture design, implementation, validation, and automated deployment executed through agile sprints.

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
      Client (Web)
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
| Containerization  | Docker                                       |

## Running Locally

### Prerequisites

* JDK 8
* Docker for PostgreSQL, or a local PostgreSQL installation
* Git

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

The API will be available at:

```text
http://localhost:8080
```

The application will create the required database tables automatically according to the configured JPA settings.

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

The project is currently being prepared for deployment as a hosted backend API.

## Project Status

**Backend:** Feature-complete and undergoing final deployment preparation.

**Frontend:** Existing Vue.js client remain part of the original project and may require further modernization to align with the current backend API.

**Next step:** Deploy the Spring Boot API to a hosted environment and make the API publicly accessible.

## Contributors

* Ben Mwaniki
* Xirui Zhang
* Kaicheng Wu
* Kaan Gure
* Zeyang Xu
