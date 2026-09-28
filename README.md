# DigiSecureBank 🏦

A **microservices-based digital banking backend** built with **Java 21 and Spring Boot**, designed to demonstrate secure banking operations, event-driven communication, fraud detection, payment processing, and fault-tolerant service-to-service communication.

The system is composed of independent microservices for authentication, account management, transactions, payments, fraud detection, and notifications, with a centralized API Gateway.

---

## 🏗️ Architecture

```text
                         ┌─────────────────────┐
                         │      API Gateway     │
                         │ Spring Cloud Gateway │
                         └──────────┬──────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
       ┌─────────────┐       ┌─────────────┐       ┌─────────────┐
       │ Auth Service│       │Account      │       │ Transaction │
       │             │       │Service      │       │ Service     │
       └─────────────┘       └─────────────┘       └──────┬──────┘
                                                           │
                              ┌────────────────────────────┼──────────────┐
                              │                            │              │
                              ▼                            ▼              ▼
                       ┌─────────────┐             ┌─────────────┐ ┌─────────────┐
                       │   Kafka     │             │    Redis    │ │  Payment    │
                       │ Event Bus   │             │             │ │  Service    │
                       └──────┬──────┘             └─────────────┘ └──────┬──────┘
                              │                                            │
                    ┌─────────┴──────────┐                                 │
                    ▼                    ▼                                 ▼
             ┌─────────────┐     ┌───────────────┐                 ┌─────────────┐
             │    Fraud    │     │ Notification  │                 │  Razorpay   │
             │  Detection  │     │    Service    │                 │   Gateway   │
             └─────────────┘     └───────────────┘                 └─────────────┘

                              ┌─────────────┐
                              │    MySQL    │
                              │  Database   │
                              └─────────────┘
```

---

## ✨ Key Features

### 🔐 Authentication & Authorization

* JWT-based authentication
* Spring Security OAuth2 Resource Server
* Protected REST APIs
* JWT propagation for authenticated service-to-service requests
* Centralized API access through Spring Cloud Gateway

### 👤 Account Management

* Customer/account management
* Account balance operations
* Transaction-aware account updates
* MySQL persistence using Spring Data JPA
* JPA auditing support

### 💸 Transactions

* Banking transaction processing
* Sender and receiver account validation
* Transaction state management
* Event-driven transaction processing using Apache Kafka
* OTP-based transaction verification
* Saga-style compensation for failed or rejected transactions

### 🚨 Fraud Detection

The fraud detection service evaluates transactions using rule-based checks including:

* Transaction velocity
* Unusual transaction amounts
* Transaction amount relative to account balance
* Redis-backed temporary transaction state

Suspicious transactions can trigger additional verification before completion.

### 💳 Payment Processing

* Razorpay payment gateway integration
* Payment order creation
* Payment status handling
* Successful and failed payment event processing
* Kafka-based payment event propagation

### 📨 Event-Driven Communication

Apache Kafka is used to decouple services and communicate domain events such as:

* `transaction.initiated`
* `transaction.completed`
* `transaction.refunded`
* `fraud.detected`
* `fraud.check.clean`
* `verification.required`
* `payment.completed`
* `payment.failed`

### 🛡️ Fault Tolerance

Inter-service calls use **Resilience4j** to improve reliability through:

* Circuit Breaker
* Retry
* Fallback handling

### ⚡ Redis

Redis is used for short-lived and high-speed application state including:

* OTP storage and expiration
* Transaction velocity tracking
* Fraud detection state
* Temporary transaction-related data

### 🐳 Containerized Infrastructure

Docker Compose provisions the infrastructure required by the application:

* MySQL 8
* Redis
* Apache Kafka
* ZooKeeper

All infrastructure containers communicate through a dedicated Docker bridge network.

---

## 🧩 Microservices

| Service                     | Responsibility                                      |
| --------------------------- | --------------------------------------------------- |
| **API Gateway**             | Central entry point and request routing             |
| **Auth Service**            | User authentication and JWT-related functionality   |
| **Account Service**         | Customer accounts and account operations            |
| **Transaction Service**     | Banking transactions and transaction workflow       |
| **Payment Service**         | Razorpay payment integration and payment processing |
| **Fraud Detection Service** | Rule-based transaction fraud detection              |
| **Notification Service**    | Consumes banking events and handles notifications   |

---

## 🛠️ Technology Stack

### Backend

* **Java 21**
* **Spring Boot**
* **Spring MVC / REST APIs**
* **Spring Security**
* **Spring Data JPA**
* **Hibernate**
* **Spring Cloud Gateway**
* **Spring Cloud OpenFeign**
* **Spring RestClient**

### Security

* **JWT**
* **JJWT**
* **Spring Security OAuth2 Resource Server**

### Messaging & Distributed Systems

* **Apache Kafka**
* **Spring Kafka**
* **Resilience4j**

  * Circuit Breaker
  * Retry
  * Fallback

### Databases & Storage

* **MySQL 8**
* **Redis**
* **Spring Data Redis**

### Payments

* **Razorpay Java SDK**

### Infrastructure

* **Docker**
* **Docker Compose**
* **Apache ZooKeeper**

### Build & Development

* **Maven**
* **Lombok**
* **Spring Boot Actuator**
* **Jakarta Bean Validation**

---

## 🔄 Transaction Flow

A typical transaction follows a distributed workflow:

```text
Client
  │
  ▼
API Gateway
  │
  ▼
Transaction Service
  │
  ├── Validate authentication
  │
  ├── Validate accounts
  │
  ├── Process transaction
  │
  └── Publish transaction.initiated
              │
              ▼
       Fraud Detection
              │
        ┌─────┴─────┐
        │           │
      Clean      Suspicious
        │           │
        │           ▼
        │      OTP Verification
        │           │
        │      ┌────┴────┐
        │      │         │
        │    Valid     Invalid
        │      │         │
        │      │         ▼
        │      │    Compensation
        │      │
        └──────┴──────────────►
                    │
                    ▼
          Transaction Completed
```

This provides a Saga-style workflow where failed or rejected operations can trigger compensating actions rather than relying on a distributed database transaction.

---

## 🚨 Fraud Detection Flow

The Fraud Detection Service evaluates transactions using multiple rules.

```text
Transaction Event
       │
       ▼
Fraud Detection Service
       │
       ├── Transaction Velocity Check
       │
       ├── Amount Anomaly Check
       │
       └── Balance Percentage Check
       │
       ▼
  ┌────┴─────┐
  │          │
Clean     Suspicious
  │          │
  ▼          ▼
Continue   OTP / Verification
             │
             ▼
       Complete or Compensate
```

Redis provides fast access to temporary transaction and fraud-detection state.

---

## 📨 Kafka Event Flow

Kafka acts as the event backbone between loosely coupled services.

```text
Transaction Service
        │
        │ transaction.initiated
        ▼
Fraud Detection Service
        │
        ├──────── fraud.check.clean ────────► Transaction Service
        │
        └──────── verification.required ───► Transaction Service
                                                   │
                                                   ▼
                                             Notification Service
```

Payment and transaction lifecycle events are also consumed by downstream services.

---

## 🛡️ Resilience

Service-to-service communication is protected using **Resilience4j**.

```text
Transaction Service
       │
       ▼
 Account Service
       │
       ├── Success ───────► Continue
       │
       └── Failure
             │
        ┌────┴─────┐
        │          │
      Retry    Circuit Breaker
        │          │
        └────┬─────┘
             │
             ▼
          Fallback
```

This prevents repeated failures in downstream services from unnecessarily propagating through the application.

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

* Java 21
* Maven
* Docker
* Docker Compose
* Git

---

### 1. Clone the Repository

```bash
git clone https://github.com/Prasanna-cpu/DigiSecureBank.git

cd DigiSecureBank
```

---

### 2. Start Infrastructure

From the project root:

```bash
docker compose up -d
```

This starts:

* MySQL
* Redis
* Kafka
* ZooKeeper

The Docker Compose configuration creates a dedicated `banking-network` bridge network and persists MySQL data through a Docker volume.

---

### 3. Configure Environment Variables

Create the required environment configuration for each service.

Sensitive credentials such as:

```text
JWT secrets
Database credentials
Kafka configuration
Redis configuration
Razorpay API credentials
```

should be supplied through environment variables or local `.env` configuration rather than committed to Git.

---

### 4. Build the Services

Each service is a Maven project.

From an individual service directory:

```bash
mvn clean package
```

You can then start the Spring Boot application using:

```bash
mvn spring-boot:run
```

Start the services in the required dependency order according to your local configuration.

---

## 📁 Project Structure

```text
DigiSecureBank/
│
├── auth-service/
│   └── Authentication & JWT
│
├── account-service/
│   └── Account management
│
├── transaction-service/
│   └── Transaction processing
│
├── payment-service/
│   └── Razorpay payment integration
│
├── fraud-detection-service/
│   └── Fraud detection & verification
│
├── notification-service/
│   └── Event-driven notifications
│
├── gateway/
│   └── API Gateway
│
├── docker-compose.yml
└── .gitignore
```

---

## 🔑 Engineering Concepts Demonstrated

This project focuses on practical backend and distributed-systems concepts:

* Microservices Architecture
* RESTful APIs
* API Gateway Pattern
* JWT Authentication
* OAuth2 Resource Server
* Service-to-Service Communication
* Event-Driven Architecture
* Apache Kafka
* Redis
* Saga-style Compensation
* Circuit Breaker Pattern
* Retry & Fallback
* Database Transactions
* JPA/Hibernate
* Payment Gateway Integration
* Rule-Based Fraud Detection
* Containerized Infrastructure
* Docker Compose

---

## 📌 Future Improvements

Potential improvements include:

* Add centralized service discovery
* Introduce distributed tracing with OpenTelemetry
* Add centralized configuration management
* Improve automated integration and end-to-end test coverage
* Add Kafka dead-letter topics and retry topics
* Introduce idempotency for payment and transaction operations
* Add Prometheus/Grafana monitoring dashboards
* Add CI/CD pipeline
* Add API documentation with OpenAPI/Swagger

---

## 👨‍💻 Author

**Prasanna Kumar M**

Backend Developer | Java | Spring Boot | Microservices

[GitHub](https://github.com/Prasanna-cpu)
