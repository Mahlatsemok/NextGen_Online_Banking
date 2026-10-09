# NextGen Online Banking

A Spring Boot REST API for an online banking system, developed with a focus on secure banking operations, backend engineering, and quality assurance.

The application supports user registration, JWT authentication, account management, financial transactions, and payment processing. It includes automated testing, Apache JMeter performance testing, and a GitHub Actions continuous integration pipeline.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen)
![Maven](https://img.shields.io/badge/Build-Maven-blue)
![Testing](https://img.shields.io/badge/Testing-Automated-green)
![Performance](https://img.shields.io/badge/Performance-Apache%20JMeter-red)
![CI](https://github.com/Mahlatsemok/NextGen_Online_Banking/actions/workflows/ci.yml/badge.svg)

## Table of Contents

- [Overview](#overview)
- [Project Objectives](#project-objectives)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Core Features](#core-features)
- [API Overview](#api-overview)
- [Getting Started](#getting-started)
- [Database Configuration](#database-configuration)
- [Quality Assurance and Testing](#quality-assurance-and-testing)
- [Performance Testing Results](#performance-testing-results)
- [Continuous Integration](#continuous-integration)
- [Repository Structure](#repository-structure)
- [Security Considerations](#security-considerations)
- [Scope and Limitations](#scope-and-limitations)
- [Author](#author)

## Overview

NextGen Online Banking is a backend application that demonstrates how an online banking API can be designed, implemented, and tested using Java and Spring Boot.

The project combines application development with a structured quality assurance approach. It covers banking business rules, authentication and authorization, database persistence, transaction integrity, automated regression testing, and performance testing.

Testing is integrated into the development workflow to help identify defects, verify expected behaviour, and reduce the risk of regressions when application changes are introduced.

## Project Objectives

- Build a maintainable REST API using Java and Spring Boot.
- Implement secure user registration and authentication.
- Protect banking resources using Spring Security and JWT.
- Enforce account ownership and banking business rules.
- Support transaction consistency and idempotent transfers.
- Implement payment processing and payment requests.
- Validate application behaviour through automated tests.
- Test positive and negative API scenarios.
- Evaluate API behaviour under a controlled load using Apache JMeter.
- Automate build and test verification using GitHub Actions.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application development |
| Spring Boot | Backend application framework |
| Spring Web | REST API endpoints |
| Spring Security | Authentication and authorization |
| JSON Web Tokens (JWT) | Token-based authentication |
| Spring Data JPA | Database access and persistence |
| Hibernate | Object-relational mapping |
| H2 Database | Development and test database |
| PostgreSQL driver | PostgreSQL connectivity support |
| Maven | Build and dependency management |
| JUnit 5 | Automated testing |
| Spring Boot Test | Application testing support |
| MockMvc | HTTP endpoint testing |
| Apache JMeter 5.6.3 | API functional, negative, and load testing |
| GitHub Actions | Continuous integration |
| Git and GitHub | Version control and source hosting |

## Architecture

The application follows a layered backend architecture that separates HTTP handling, business logic, persistence, and security.

```text
                    API Clients
                        |
                        v
                REST Controllers
                        |
                        v
                  Request DTOs
                   Validation
                        |
                        v
                   Service Layer
                Business Rules and Logic
                        |
                        v
                 Repository Layer
                        |
                        v
                     Database

               Cross-cutting Concerns
             Spring Security | JWT
                Exception Handling

                  Quality Assurance
       Unit | API | Integration | Security
                Concurrency Tests
                        |
                        v
                 Apache JMeter

                Continuous Integration
                  GitHub Actions
                        |
                        v
                  Maven Build
                   and Tests
```

### Application Layers

**Controllers**

Expose HTTP endpoints and handle incoming requests and outgoing responses.

**DTOs and validation**

Define request and response structures and validate incoming data before it reaches business logic.

**Service layer**

Implements registration, authentication, account operations, transaction processing, and payment workflows.

**Repository layer**

Provides database access through Spring Data JPA.

**Security**

Uses Spring Security and JWT-based authentication to protect resources and enforce access control.

**Exception handling**

Centralizes application errors and validation failures to provide consistent API responses.

## Core Features

### 1. User Registration

- User registration with request validation.
- Duplicate-email detection.
- Password hashing using BCrypt.
- Structured registration responses that do not expose password hashes.
- Validation and error handling for invalid requests.

### 2. Authentication and Authorization

- User login.
- JWT-based authentication.
- Protected API endpoints.
- Authorization checks for administrative resources.
- Logout and token revocation support.
- Security tests for unauthorized and forbidden access.

### 3. Account Management

- Create and retrieve bank accounts.
- Support multiple accounts per user.
- Account balance management.
- Deposit and withdrawal operations.
- Account status management.
- Freeze and close account operations.
- Account ownership enforcement.
- Transaction history retrieval.

### 4. Transaction Processing

- Deposits and withdrawals.
- Transfers between accounts.
- Transfers involving different users.
- Transaction history.
- Insufficient-funds validation.
- Transaction integrity and rollback checks.
- Idempotency support for transfers.
- Concurrency testing for simultaneous operations.

### 5. Payment Processing

- Standard payments.
- QR payments.
- Scheduled payments.
- Payment requests.
- Accepting and declining payment requests.
- Payment references.
- Payment validation and authorization checks.
- Idempotency controls where implemented.

### 6. Quality Assurance

- Unit testing.
- Repository testing.
- Controller and API testing.
- Integration testing.
- Security and authorization testing.
- Negative test scenarios.
- Transaction integrity testing.
- Concurrency testing.
- JMeter functional and performance testing.
- Automated build and test execution through GitHub Actions.

## API Overview

The application uses the `/api` base path.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register a user |
| POST | `/api/auth/login` | Authenticate a user |
| GET | `/api/auth/profile` | Retrieve the authenticated user's profile |
| POST | `/api/auth/logout` | Log out and revoke the applicable token |
| GET | `/api/accounts` | Retrieve the user's accounts |
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts/{accountNumber}` | Retrieve account details |
| POST | `/api/accounts/{accountNumber}/deposit` | Deposit funds |
| POST | `/api/accounts/{accountNumber}/withdraw` | Withdraw funds |
| POST | `/api/accounts/{accountNumber}/freeze` | Freeze an account |
| POST | `/api/accounts/{accountNumber}/close` | Close an account |
| POST | `/api/accounts/{accountNumber}/transfer` | Initiate a transfer |
| GET | `/api/accounts/{accountNumber}/transactions` | Retrieve transaction history |
| POST | `/api/payments` | Create a standard payment |
| POST | `/api/payments/qr` | Process a QR payment |
| POST | `/api/payments/scheduled` | Create a scheduled payment |
| POST | `/api/payment-requests` | Create a payment request |
| GET | `/api/payment-requests/{requestReference}` | Retrieve a payment request |
| POST | `/api/payment-requests/{requestReference}/accept` | Accept a payment request |
| POST | `/api/payment-requests/{requestReference}/decline` | Decline a payment request |
| GET | `/api/admin/dashboard` | Access the protected admin dashboard |

**Note:** This table is an overview of the API. Verify individual mappings against the current controllers if any routes have changed.

Protected endpoints require the authentication mechanism configured by the application. For JWT-protected requests, use the following HTTP header:

```http
Authorization: Bearer <your-jwt-token>
```

## Getting Started

### Prerequisites

Install the following:

- Java Development Kit (JDK) 21
- Apache Maven, or the Maven Wrapper if included in the repository
- Git
- Visual Studio Code or another Java-compatible IDE

Apache JMeter 5.6.3 is required to execute the included JMeter test plans.

### 1. Clone the Repository

```bash
git clone https://github.com/Mahlatsemok/NextGen_Online_Banking.git
cd NextGen_Online_Banking
```

### 2. Verify Java and Maven

```bash
java -version
mvn -version
```

Confirm that Java 21 is available and Maven is correctly configured.

### 3. Run Automated Tests

```bash
mvn clean test
```

This command cleans the previous build output, compiles the project, and executes the Maven test suite.

**Current status:** The automated test suite is passing.

### 4. Start the Application

```bash
mvn spring-boot:run
```

The application is configured to run at:

```text
http://localhost:8080
```

Keep the application running when making manual API requests or executing JMeter test plans.

## Database Configuration

The default development configuration uses an in-memory H2 database.

```text
jdbc:h2:mem:nextgenbanking
```

The H2 console is configured at:

```text
http://localhost:8080/h2-console
```

An in-memory database is useful for development and testing. However, its data is not retained after the database instance shuts down.

The project includes a PostgreSQL runtime driver for PostgreSQL connectivity. Configure the relevant datasource settings and credentials before using PostgreSQL.

Do not commit production database credentials or other secrets to the repository.

## Quality Assurance and Testing

Quality assurance is a core part of this project. Automated tests verify application behaviour at multiple levels.

### Automated Test Coverage

| Test category | Purpose |
|---|---|
| Unit tests | Verify service logic and business rules |
| Repository tests | Verify persistence and repository queries |
| Controller tests | Verify HTTP responses, request validation, and endpoint behaviour |
| Integration tests | Verify interactions between application components |
| Authentication tests | Verify login and authentication behaviour |
| Authorization tests | Verify access restrictions and account ownership |
| Transaction integrity tests | Verify financial operation consistency and rollback behaviour |
| Idempotency tests | Verify duplicate transfer handling |
| Concurrency tests | Evaluate simultaneous transaction scenarios |
| Regression tests | Recheck existing behaviour after changes |

### Running the Test Suite

```bash
mvn clean test
```

Maven Surefire test reports are generated under:

```text
target/surefire-reports/
```

These reports can be used to review individual test results and investigate failures.

### Apache JMeter Test Plans

The repository contains three JMeter test plans.

| Test plan | File | Purpose |
|---|---|---|
| Functional testing | `jmeter/functional/banking-functional-tests.jmx` | Exercise banking API workflows |
| Negative testing | `jmeter/negative/banking-negative-tests.jmx` | Verify rejection of invalid requests |
| Load testing | `jmeter/performance/banking-load-test.jmx` | Exercise API workflows under a configured workload |

Supporting test data is stored in:

```text
jmeter/data/
```

The data files support scenarios involving performance users and invalid registration inputs.

### Executing JMeter Tests

1. Start the Spring Boot application.
2. Open Apache JMeter 5.6.3.
3. Open the required `.jmx` test plan.
4. Verify the configured base URL, request data, and test credentials.
5. Run the test plan.
6. Review the Summary Report or Aggregate Report.
7. Investigate failed requests and examine response bodies where necessary.

Important metrics include:

- Total samples.
- Error percentage.
- Average response time.
- Median and percentile response times.
- Maximum response time.
- Throughput.

JMeter performance results depend on the hardware, application configuration, database, and workload used during testing.

## Performance Testing Results

A load test was executed against the NextGen Online Banking API using Apache JMeter 5.6.3.

The test covered 11 API workflows, with 25 samples configured for each workflow.

### Overall Results

| Metric | Result |
|---|---:|
| Total samples | 275 |
| Average response time | 29 ms |
| Median response time | 13 ms |
| 90th percentile response time | 45 ms |
| 95th percentile response time | 165 ms |
| 99th percentile response time | 198 ms |
| Maximum response time | 370 ms |
| Error rate | 0.00% |
| Throughput | 9.47 requests/second |

### Results by API Workflow

| API workflow | Samples | Average response time | Error rate |
|---|---:|---:|---:|
| Login API | 25 | 176 ms | 0% |
| Protected API — Get Accounts | 25 | 13 ms | 0% |
| Create Account | 25 | 12 ms | 0% |
| Deposit API | 25 | 12 ms | 0% |
| Withdraw API | 25 | 16 ms | 0% |
| Get Transaction History | 25 | 9 ms | 0% |
| Create Destination Account | 25 | 10 ms | 0% |
| Transfer API | 25 | 17 ms | 0% |
| Payment API | 25 | 21 ms | 0% |
| Scheduled Payment | 25 | 16 ms | 0% |
| QR Payment | 25 | 17 ms | 0% |

### Findings

- All 11 tested API workflows reported a 0% error rate in this run.
- The test exercised authentication, account management, deposits, withdrawals, transfers, transaction history, and payment processing.
- Login had the highest average response time at 176 ms.
- The maximum observed response time was 370 ms.
- Scheduled payment requests completed without reported errors in this run.
- The measured throughput was approximately 9.47 requests per second.

### Conclusion

The test run completed with no reported request errors across 275 samples. The observed response times provide a baseline for future performance comparisons.

These results represent one test run in a specific environment. They do not establish production capacity or guarantee performance under higher or sustained traffic.

## Continuous Integration

The project uses GitHub Actions to automate build and test verification.

Workflow location:

```text
.github/workflows/ci.yml
```

The workflow is configured to run on pushes and pull requests targeting `main`.

### Pipeline Process

1. Check out the repository.
2. Set up Java 21 using Temurin.
3. Configure Maven dependency caching.
4. Execute the Maven build and automated tests.

The pipeline command is:

```bash
mvn clean test
```

### Current CI Status

The automated tests and GitHub Actions pipeline are passing.

This provides a repeatable quality check whenever changes are pushed or a pull request targets the main branch.

JMeter load testing is maintained separately from the standard CI workflow so that performance workloads do not unnecessarily slow down routine build and regression checks.

## Repository Structure

```text
NextGen_Online_Banking/
├── .github/
│   └── workflows/
│       └── ci.yml
├── jmeter/
│   ├── data/
│   ├── functional/
│   │   └── banking-functional-tests.jmx
│   ├── negative/
│   │   └── banking-negative-tests.jmx
│   └── performance/
│       └── banking-load-test.jmx
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/nextgen/onlinebanking/
│   │   └── resources/
│   └── test/
│       └── java/
├── pom.xml
└── README.md
```

The structure above highlights the main application, testing, and CI components. Individual package and file names may differ as the project evolves.

## Security Considerations

The project implements authentication, authorization, password hashing, request validation, and account ownership checks.

As an educational project, it should not be used to process real financial transactions or sensitive production banking data.

Before production deployment, additional assessment would be required for:

- Secure secret and credential management.
- Login rate limiting and account-abuse prevention.
- Production database security and backup procedures.
- Token expiry, revocation, and lifecycle management.
- Audit logging and sensitive-data handling.
- Monitoring, alerting, and incident response.
- Deployment hardening and regulatory requirements.

## Scope and Limitations

NextGen Online Banking is a project designed to demonstrate backend engineering and quality assurance practices.

It is not a production-certified banking platform. Its current configuration and test results are intended for development, learning, and demonstration purposes.

The performance results are specific to the environment and workload used during testing. Further testing with larger workloads, sustained traffic, and defined performance thresholds would be required before making production-readiness claims.

## Author

**Mahlatsemok**

GitHub: [Mahlatsemok](https://github.com/Mahlatsemok)

## Project Purpose

This project demonstrates practical experience with:

- Java and Spring Boot backend development.
- REST API design and validation.
- Authentication and authorization testing.
- Database integration.
- Financial transaction integrity.
- Automated unit and integration testing.
- Negative and security testing.
- Apache JMeter performance testing.
- Continuous integration with GitHub Actions.
