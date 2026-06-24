# Health Core

Health Core is a two-service Spring Boot system for managing patients and creating a billing account for each newly created patient.

## Overview

This repository contains:

- `patient-service` — a REST + JPA microservice for patient CRUD operations.
- `billing-service` — a gRPC microservice that creates a billing account when the patient service creates a new patient.

The services communicate like this:

1. A client calls `patient-service` to create a patient.
2. `patient-service` stores the patient in the database.
3. After the patient is saved, `patient-service` calls `billing-service` over gRPC.
4. `billing-service` returns a billing account response.

## Project structure

```text
health-core/
├── patient-service/
└── billing-service/
```

There is no shared parent `pom.xml` at the repository root. Build and run each microservice from its own directory.

## Microservices at a glance

| Service | Type | Purpose | Main ports |
|---|---|---|---|
| `patient-service` | REST + JPA + gRPC client | Manage patients and call billing after patient creation | `4000` |
| `billing-service` | gRPC server | Receive billing account creation requests | `4001` HTTP, `9001` gRPC |

## `patient-service`

### What it does

`patient-service` exposes REST endpoints for patient CRUD and persists patients through Spring Data JPA.

When a patient is created:

- the service validates the request,
- saves the patient,
- then calls `billing-service` via gRPC to create a billing account.

### Main classes

- `com.frztech.patientService.PatientServiceApplication` — Spring Boot entry point
- `controller/PatientController` — REST endpoints
- `service/PatientService` — business logic
- `repository/PatientRepository` — JPA repository
- `model/Patient` — JPA entity
- `grpc/BillingServiceGrpcClient` — gRPC client for billing
- `exception/GlobalExceptionHandler` — validation and business error handling
- `mapper/PatientMapper` — DTO/entity conversion

### REST API

Base path: `/patients`

#### `GET /patients`
Returns all patients.

Example:

```bash
curl http://localhost:4000/patients
```

#### `POST /patients`
Creates a new patient and triggers a billing-account creation request.

Request body:

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "address": "123 Main Street",
  "dateOfBirth": "1995-04-17",
  "registeredDate": "2026-06-25"
}
```

Example:

```bash
curl -X POST http://localhost:4000/patients \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "address": "123 Main Street",
    "dateOfBirth": "1995-04-17",
    "registeredDate": "2026-06-25"
  }'
```

Validation rules:

- `name` — required, max 100 characters
- `email` — required, must be a valid email address
- `address` — required
- `dateOfBirth` — required, expected as an ISO date string (`YYYY-MM-DD`)
- `registeredDate` — required on create

#### `PUT /patients/{id}`
Updates an existing patient.

Example:

```bash
curl -X PUT http://localhost:4000/patients/<patient-id> \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe Updated",
    "email": "john.updated@example.com",
    "address": "456 Main Street",
    "dateOfBirth": "1995-04-17",
    "registeredDate": "2026-06-25"
  }'
```

Notes:

- The service looks up the patient by UUID.
- Duplicate emails are rejected.
- `registeredDate` is not used in the update mapping logic.

#### `DELETE /patients/{id}`
Deletes a patient by UUID.

Example:

```bash
curl -X DELETE http://localhost:4000/patients/<patient-id>
```

### Data model

`Patient` fields:

- `id` — generated UUID
- `name` — required
- `email` — required, unique
- `address` — required
- `dateOfBirth` — required `LocalDate`
- `registeredDate` — required `LocalDate`

### Error handling

`patient-service` returns structured `400 Bad Request` responses for:

- validation failures
- duplicate email addresses
- missing patient IDs

### gRPC client configuration

`patient-service` connects to `billing-service` using these properties:

- `billing.service.address` — default: `localhost`
- `billing.service.grpc.port` — default: `9001`

These can be supplied as Spring Boot environment variables if needed:

- `BILLING_SERVICE_ADDRESS`
- `BILLING_SERVICE_GRPC_PORT`

## `billing-service`

### What it does

`billing-service` implements the `BillingService` gRPC contract and responds to billing account creation requests.

The current implementation is intentionally simple:

- it logs the incoming request,
- returns a fixed `accountId`,
- returns a fixed `status` of `Complete`.

### Main classes

- `com.faraaz.billing_service.BillingServiceApplication` — Spring Boot entry point
- `grpc/BillingGrpcService` — gRPC server implementation

### gRPC contract

Proto file: `src/main/proto/billing_service.proto`

Service:

- `BillingService/CreateBillingAccount(BillingRequest) returns (BillingResponse)`

Request message:

- `patientId`
- `name`
- `email`

Response message:

- `accountId`
- `status`

### Server ports

`billing-service` starts:

- HTTP server on `4001`
- gRPC server on `9001`

## Prerequisites

- Java 25
- Maven 3.9+ or the bundled Maven wrapper (`./mvnw`)
- Docker, if you want to run the services in containers
- PostgreSQL, either running locally or as a Docker container before starting `patient-service`

## Local setup

### 1) Start `billing-service`

Run it first so the patient service can reach the gRPC endpoint.

```bash
cd billing-service
./mvnw spring-boot:run
```

### 2) Start `patient-service`

In a second terminal:

```bash
cd patient-service
./mvnw spring-boot:run
```

If you need to point `patient-service` to a different billing host, pass the connection settings as environment variables:

```bash
cd patient-service
BILLING_SERVICE_ADDRESS=localhost BILLING_SERVICE_GRPC_PORT=9001 ./mvnw spring-boot:run
```

### Local URLs

- `patient-service`: `http://localhost:4000`
- `billing-service`: gRPC on `localhost:9001`

## Docker setup

Each microservice has its own Dockerfile.

Before starting the project, create a PostgreSQL instance that `patient-service` can reach on the Docker network. The service expects the database host to be available as `patient-service-db` on the `docker-internal` network, with credentials passed through environment variables.

### 1) Create the Docker network

```bash
docker network create docker-internal
```

### 2) Start PostgreSQL

```bash
docker run --name=patient-service-db -d \
  --network docker-internal \
  -e POSTGRES_DB=patient_service_db \
  -e POSTGRES_USER=<username>> \
  -e POSTGRES_PASSWORD=<password> \
  -p 5432:5432 \
  postgres:16
```

Use the same database name and credentials in the `patient-service` environment variables shown below.

### Build the images

```bash
docker build -t billing-service:v1 ./billing-service
docker build -t patient-service:v1 ./patient-service
```

### Run `billing-service`

```bash
docker run --name=billing-service -d --network docker-internal -p 4001:4001 -p 9001:9001 billing-service:v1
```

### Run `patient-service`

```bash
docker run --name=patient-service -d --network docker-internal -e SPRING_DATASOURCE_URL=jdbc:postgresql://patient-service-db:5432/patient_service_db -e SPRING_DATASOURCE_USERNAME=<username> -e SPRING_DATASOURCE_PASSWORD=<password> -e SPRING_JPA_HIBERNATE_DDL_AUTO=update -e SPRING_SQL_INIT_MODE=always -p 4000:4000 patient-service:v1
```

### Docker notes

- `patient-service` must be able to reach PostgreSQL at `patient-service-db:5432` on the `docker-internal` network.
- `billing-service` and `patient-service` should both run on the same Docker network (`docker-internal`) so the services can communicate.
- If you change the database name, username, or password, update the `SPRING_DATASOURCE_*` environment variables accordingly.

## Configuration reference

### `patient-service`

| Property | Default | Purpose |
|---|---:|---|
| `spring.application.name` | `patient-service` | Spring application name |
| `server.port` | `4000` | REST server port |
| `billing.service.address` | `localhost` | Billing gRPC host |
| `billing.service.grpc.port` | `9001` | Billing gRPC port |
| `SPRING_DATASOURCE_URL` | required for PostgreSQL | JDBC URL for the patient database |
| `SPRING_DATASOURCE_USERNAME` | required for PostgreSQL | PostgreSQL username |
| `SPRING_DATASOURCE_PASSWORD` | required for PostgreSQL | PostgreSQL password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` in the Docker example | Hibernate schema strategy |
| `SPRING_SQL_INIT_MODE` | `always` in the Docker example | Run SQL initialization scripts when present |

Optional standard Spring Boot database properties if you want to externalize the data source:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `SPRING_JPA_DATABASE_PLATFORM`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`

### `billing-service`

| Property | Default | Purpose |
|---|---:|---|
| `spring.application.name` | `billing-service` | Spring application name |
| `server.port` | `4001` | HTTP server port |
| `grpc.server.port` | `9001` | gRPC server port |

## Build commands

Build each service individually:

```bash
cd patient-service
./mvnw clean package
```

```bash
cd billing-service
./mvnw clean package
```

## OpenAPI / Swagger

`patient-service` includes Springdoc OpenAPI support. After the service starts, the API documentation is typically available at:

- `/swagger-ui/index.html`
- `/v3/api-docs`

## Troubleshooting

- Start PostgreSQL and make sure it is reachable before starting `patient-service`.
- Start `billing-service` before creating patients.
- If `patient-service` cannot connect to billing, check `BILLING_SERVICE_ADDRESS` and `BILLING_SERVICE_GRPC_PORT`.
- If you switch to a different PostgreSQL container name, database name, or credentials, update the `SPRING_DATASOURCE_*` environment variables to match.

## Summary

- Use `patient-service` for patient CRUD.
- Use `billing-service` for billing-account creation over gRPC.
- Run both services locally or in Docker, with `billing-service` available on gRPC port `9001`.

