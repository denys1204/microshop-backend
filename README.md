# 🛒 MicroShop - Cloud-Native E-Commerce Platform

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen.svg)
![Microservices](https://img.shields.io/badge/Architecture-Microservices-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## 📝 About The Project
MicroShop is a scalable, cloud-native backend for an e-commerce platform built with **Spring Boot** and **Spring Cloud**. This project demonstrates modern microservices architecture principles, including service discovery, API routing, asynchronous event-driven communication, distributed tracing, and resilience patterns.

## 🛠️ Tech Stack

### Core & Frameworks
* **Language:** Java 21
* **Framework:** Spring Boot 3.5.11, Spring Cloud
* **API Gateway:** Spring Cloud Gateway
* **Configuration:** Spring Cloud Config

### Databases & Caching
* **Relational DB:** PostgreSQL (with Flyway for migrations)
* **NoSQL DB:** MongoDB
* **Caching:** Redis (Spring Data Redis)

### Communication & Resilience
* **Synchronous:** Spring Cloud OpenFeign
* **Asynchronous (Event-Driven):** Apache Kafka + Spring Cloud Stream
* **Resilience:** Resilience4j (Circuit Breaker, Retry, Rate Limiter)

### Security
* **Auth:** Spring Security + JWT
* **OAuth2:** Spring Authorization Server

### Observability & Monitoring
* **Metrics:** Prometheus + Grafana + Micrometer
* **Distributed Tracing:** Jaeger / OpenTelemetry
* **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana)

### DevOps, Testing & CI/CD
* **Containerization:** Docker
* **Orchestration:** Kubernetes
* **Testing:** JUnit 5, Mockito, Testcontainers, WireMock
* **CI/CD:** GitHub Actions
* **Documentation:** OpenAPI 3.0 (Swagger)
* **External API:** Stripe Payment API

---

## 📦 Microservices Architecture

The platform consists of several independent microservices, each managing its own database to ensure loose coupling:

1. **`gateway-service`**: The single entry point to the system. Handles routing and JWT validation.
2. **`config-server`**: Centralized configuration management for all microservices.
3. **`auth-service`**: Handles user registration, authentication, and JWT token issuance.
4. **`product-service`**: Manages the product catalog. Uses **PostgreSQL** and **Redis** for high-performance read operations.
5. **`review-service`**: Manages customer reviews. Uses **MongoDB** for flexible, document-based storage.
6. **`order-service`**: Handles order creation and lifecycle. Publishes events to **Kafka**.
7. **`payment-service`**: Listens to Kafka events and integrates with **Stripe API** for payment processing. Implements the Saga pattern for distributed transactions.

---

## 🚀 Getting Started (Local Development)

### Prerequisites
* JDK 21
* Maven
* Docker & Docker Compose

### Installation & Run
1. Clone the repository:
   ```bash
   git clone [https://github.com/denys1204/microshop-backend.git]
   cd microshop-backend
   ```
2. Start the infrastructure (Databases, Kafka, Redis, Observability tools) using Docker Compose:
   ```bash
   docker-compose up -d
   ```
3. Build the project using Maven:
   ```bash
   mvn clean install -DskipTests
   ```
4. Run the microservices. (Make sure to start `config-server` and `gateway-service` first).

---

## 🗺️ Development Roadmap
- [x] Phase 1: Core Services setup (Product, Review) with PostgreSQL and MongoDB.
- [ ] Phase 2: Inter-service communication (OpenFeign) & Resilience4j.
- [ ] Phase 3: Security, Spring Cloud Gateway, and Authorization Server.
- [ ] Phase 4: Event-driven architecture with Kafka & Redis caching.
- [ ] Phase 5: Payment integration (Stripe) and Saga Pattern.
- [ ] Phase 6: Observability (Prometheus, Grafana, ELK, Jaeger).
- [ ] Phase 7: Dockerization, CI/CD with GitHub Actions, and Kubernetes deployment.
