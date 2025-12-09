# CoopCredit - Integral Credit Application System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/Tests-109%20Passed-brightgreen.svg)]()

A professional credit application management system for cooperatives, built with **Hexagonal Architecture** (Ports & Adapters), implementing enterprise-grade security, observability, and microservices patterns.

---

## 📋 Table of Contents

- [System Description](#-system-description)
- [Hexagonal Architecture](#-hexagonal-architecture-diagram)
- [Technology Stack](#-technology-stack)
- [Getting Started](#-getting-started)
- [API Endpoints](#-api-endpoints)
- [Security & Roles](#-security--roles)
- [Credit Evaluation Flow](#-credit-evaluation-flow)
- [Observability & Monitoring](#-observability--monitoring)
- [Testing](#-testing)
- [Project Structure](#-project-structure)

---

## 📖 System Description

**CoopCredit** is a savings and credit cooperative that previously managed credit applications through spreadsheets and manual validations, causing:

- Inconsistent credit histories
- Errors in application approvals
- Lack of risk evaluation traceability
- Long delays in credit studies
- No secure authentication or access control

This system solves these problems by providing:

| Feature | Description |
|---------|-------------|
| **Affiliate Management** | Register, update, and manage cooperative members |
| **Credit Applications** | Create and track credit requests with full lifecycle |
| **Automated Risk Evaluation** | Integration with external risk service + internal policies |
| **JWT Security** | Stateless authentication with role-based access control |
| **Circuit Breaker** | Resilience pattern for external service failures |
| **Full Observability** | Metrics, health checks, and structured logging |
| **Containerized Deployment** | Docker & Docker Compose ready |

### Microservices

| Service | Port | Description |
|---------|------|-------------|
| `credit-application-service` | 8080 | Main service with hexagonal architecture |
| `risk-central-mock-service` | 8081 | External risk evaluation mock service |
| `PostgreSQL` | 5432 | Relational database |
| `Prometheus` | 9091 | Metrics collection |
| `Grafana` | 3000 | Metrics visualization |

---

## 🏛️ Hexagonal Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              INFRASTRUCTURE LAYER                                │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                           ADAPTERS (IN)                                      │ │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐          │ │
│  │  │  REST Controller │  │  REST Controller │  │   Auth Controller │          │ │
│  │  │   /api/afiliados │  │  /api/solicitudes│  │   /api/auth       │          │ │
│  │  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘          │ │
│  └───────────┼──────────────────────┼──────────────────────┼───────────────────┘ │
│              │                      │                      │                     │
│              ▼                      ▼                      ▼                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                           PORTS (IN) - Use Cases                             │ │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐          │ │
│  │  │ GestionarAfiliado│  │CrearSolicitud    │  │ EvaluarSolicitud │          │ │
│  │  │     UseCase      │  │    UseCase       │  │     UseCase      │          │ │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘          │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                      │                                           │
│                                      ▼                                           │
│  ╔═════════════════════════════════════════════════════════════════════════════╗ │
│  ║                           DOMAIN LAYER (PURE)                               ║ │
│  ║  ┌──────────────────────────────────────────────────────────────────────┐   ║ │
│  ║  │                         DOMAIN MODELS                                 │   ║ │
│  ║  │  ┌──────────┐  ┌────────────────┐  ┌──────────────────┐              │   ║ │
│  ║  │  │ Afiliado │  │SolicitudCredito│  │ EvaluacionRiesgo │              │   ║ │
│  ║  │  │  (Entity)│  │    (Entity)    │  │     (Entity)     │              │   ║ │
│  ║  │  └──────────┘  └────────────────┘  └──────────────────┘              │   ║ │
│  ║  ├──────────────────────────────────────────────────────────────────────┤   ║ │
│  ║  │                       DOMAIN POLICIES                                 │   ║ │
│  ║  │  ┌──────────────────────────────────────────────────────────────┐    │   ║ │
│  ║  │  │ • Quota/Income Ratio ≤ 40%    • Max Amount = 5x Salary      │    │   ║ │
│  ║  │  │ • Min Seniority = 6 months    • Risk Score Validation       │    │   ║ │
│  ║  │  └──────────────────────────────────────────────────────────────┘    │   ║ │
│  ║  ├──────────────────────────────────────────────────────────────────────┤   ║ │
│  ║  │                      DOMAIN EXCEPTIONS                                │   ║ │
│  ║  │  AfiliadoNoEncontrado │ DocumentoDuplicado │ AfiliadoInactivo        │   ║ │
│  ║  └──────────────────────────────────────────────────────────────────────┘   ║ │
│  ╚═════════════════════════════════════════════════════════════════════════════╝ │
│                                      │                                           │
│                                      ▼                                           │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                          PORTS (OUT) - Interfaces                            │ │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐          │ │
│  │  │AfiliadoRepository│  │SolicitudRepository│  │  RiskCentralPort │          │ │
│  │  │      Port        │  │       Port       │  │  (External API)  │          │ │
│  │  └────────┬─────────┘  └────────┬─────────┘  └────────┬─────────┘          │ │
│  └───────────┼──────────────────────┼──────────────────────┼───────────────────┘ │
│              │                      │                      │                     │
│              ▼                      ▼                      ▼                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                          ADAPTERS (OUT)                                      │ │
│  │  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐          │ │
│  │  │   JPA Adapter    │  │   JPA Adapter    │  │  REST Client     │          │ │
│  │  │  (PostgreSQL)    │  │  (PostgreSQL)    │  │ (risk-central)   │          │ │
│  │  │                  │  │                  │  │ + Circuit Breaker │          │ │
│  │  └──────────────────┘  └──────────────────┘  └──────────────────┘          │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            EXTERNAL SERVICES                                     │
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐        │
│  │     PostgreSQL     │  │ risk-central-mock  │  │  Prometheus/Grafana│        │
│  │     (Database)     │  │    (Risk Score)    │  │   (Monitoring)     │        │
│  └────────────────────┘  └────────────────────┘  └────────────────────┘        │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Architecture Principles

| Principle | Implementation |
|-----------|----------------|
| **Domain Isolation** | Pure domain models with no framework dependencies |
| **Dependency Inversion** | Ports define interfaces, adapters implement them |
| **Single Responsibility** | Each use case handles one business operation |
| **Open/Closed** | New adapters can be added without modifying domain |
| **Testability** | Domain logic easily testable with mocked ports |

---

## 🚀 Technology Stack

### Core
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 3.4.0 | Application framework |
| Spring Data JPA | 3.4.0 | Data persistence |
| Spring Security | 6.4.0 | Authentication & authorization |
| PostgreSQL | 16 | Relational database |

### Security & Validation
| Technology | Purpose |
|------------|---------|
| JWT (jjwt) | Stateless authentication tokens |
| BCrypt | Password encryption |
| Bean Validation | Input validation |

### Resilience & Observability
| Technology | Purpose |
|------------|---------|
| Resilience4j | Circuit breaker pattern |
| Spring Actuator | Health checks & metrics |
| Micrometer | Metrics collection |
| Prometheus | Metrics storage |
| Grafana | Metrics visualization |

### Development & Testing
| Technology | Purpose |
|------------|---------|
| MapStruct | Object mapping |
| Lombok | Boilerplate reduction |
| Flyway | Database migrations |
| JUnit 5 | Unit testing |
| Mockito | Mocking framework |
| Testcontainers | Integration testing |
| Swagger/OpenAPI | API documentation |

---

## 🔧 Getting Started

### Prerequisites

- **Java 21+**
- **Maven 3.9+**
- **Docker & Docker Compose**

### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/yourusername/CoopCredit.git
cd CoopCredit

# Start all services
docker-compose up --build

# Or with monitoring stack (Prometheus + Grafana)
./run-monitoring.sh
```

**Services will be available at:**
| Service | URL |
|---------|-----|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |
| Risk Central | http://localhost:8081 |
| Prometheus | http://localhost:9091 |
| Grafana | http://localhost:3000 (admin/admin) |

### Option 2: Local Development

```bash
# 1. Start PostgreSQL
docker run -d --name postgres \
  -e POSTGRES_DB=coopcredit \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# 2. Start risk-central-mock-service
cd risk-central-mock-service
mvn spring-boot:run

# 3. Start credit-application-service (new terminal)
cd credit-application-service
mvn spring-boot:run
```

---

## 🎯 API Endpoints

### Authentication

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/registro` | Register new user | Public |
| POST | `/api/auth/login` | Login & get JWT token | Public |

### Affiliates

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/afiliados` | Create affiliate | ADMIN |
| GET | `/api/afiliados` | List all affiliates | ADMIN, ANALISTA |
| GET | `/api/afiliados/{id}` | Get affiliate by ID | ADMIN, ANALISTA, Owner |
| PUT | `/api/afiliados/{id}` | Update affiliate | ADMIN |
| GET | `/api/afiliados/documento/{doc}` | Find by document | ADMIN, ANALISTA |

### Credit Applications

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/solicitudes` | Create application | ADMIN, ANALISTA, AFILIADO |
| GET | `/api/solicitudes` | List applications | ADMIN, ANALISTA |
| GET | `/api/solicitudes/{id}` | Get by ID | ADMIN, ANALISTA, Owner |
| POST | `/api/solicitudes/{id}/evaluar` | Evaluate application | ADMIN, ANALISTA |
| GET | `/api/solicitudes/afiliado/{id}` | Get by affiliate | ADMIN, ANALISTA, Owner |
| GET | `/api/solicitudes/estado/{estado}` | Filter by status | ADMIN, ANALISTA |

### Risk Central Mock Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/risk-evaluation` | Evaluate credit risk |
| GET | `/risk-evaluation/health` | Health check |

---

## 🔐 Security & Roles

### Authentication Flow

```
┌──────────┐         ┌──────────────┐         ┌──────────────┐
│  Client  │         │   API        │         │  JWT Service │
└────┬─────┘         └──────┬───────┘         └──────┬───────┘
     │   POST /auth/login   │                        │
     │──────────────────────>                        │
     │   {username,password}│                        │
     │                      │   validate credentials │
     │                      │───────────────────────>│
     │                      │   generate token       │
     │                      │<───────────────────────│
     │   {token, user info} │                        │
     │<──────────────────────                        │
     │                      │                        │
     │   GET /api/resource  │                        │
     │   Authorization: Bearer <token>               │
     │──────────────────────>                        │
     │                      │   validate token       │
     │                      │───────────────────────>│
     │                      │   extract claims       │
     │                      │<───────────────────────│
     │   Response           │                        │
     │<──────────────────────                        │
```

### Roles & Permissions

| Role | Permissions |
|------|-------------|
| **ROLE_ADMIN** | Full system access, manage affiliates, evaluate all applications |
| **ROLE_ANALISTA** | View affiliates, evaluate PENDING applications |
| **ROLE_AFILIADO** | View own data, create and view own applications |

### Test Users

| Username | Password | Role | Email |
|----------|----------|------|-------|
| `admin` | `admin123` | ADMIN | admin@coopcredit.com |
| `analista` | `analista123` | ANALISTA | analista@coopcredit.com |
| `juanperez` | `afiliado123` | AFILIADO | juan.perez@email.com |

### Example: Login & Use Token

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# 2. Use token in requests
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/afiliados
```

---

## 📊 Credit Evaluation Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         CREDIT EVALUATION FLOW                               │
└─────────────────────────────────────────────────────────────────────────────┘

  ┌──────────┐                                           ┌──────────────────┐
  │ AFILIADO │                                           │  ANALISTA/ADMIN  │
  └────┬─────┘                                           └────────┬─────────┘
       │                                                          │
       │  1. POST /api/solicitudes                                │
       │  {afiliadoId, monto, plazo, tasaPropuesta}               │
       │─────────────────────────────────────────>                │
       │                                                          │
       │  ┌─────────────────────────────────────┐                 │
       │  │ Solicitud Created (PENDIENTE)       │                 │
       │  │ - Validates affiliate is ACTIVE     │                 │
       │  │ - Validates amount > 0              │                 │
       │  │ - Validates term in months          │                 │
       │  └─────────────────────────────────────┘                 │
       │                                                          │
       │<─────────────────────────────────────────                │
       │  Response: SolicitudDTO (id, estado: PENDIENTE)          │
       │                                                          │
       │                                                          │
       │                     2. POST /api/solicitudes/{id}/evaluar│
       │                     ─────────────────────────────────────>
       │                                                          │
       │                     ┌────────────────────────────────────┐
       │                     │      EVALUATION PROCESS            │
       │                     │                                    │
       │                     │  a) Call risk-central-mock-service │
       │                     │     POST /risk-evaluation          │
       │                     │     {documento, monto, plazo}      │
       │                     │                                    │
       │                     │  b) Receive risk score:            │
       │                     │     {score: 642, nivel: "MEDIO"}   │
       │                     │                                    │
       │                     │  c) Apply internal policies:       │
       │                     │     ✓ Quota/Income ≤ 40%           │
       │                     │     ✓ Amount ≤ 5x Salary           │
       │                     │     ✓ Seniority ≥ 6 months         │
       │                     │     ✓ Risk level != ALTO           │
       │                     │                                    │
       │                     │  d) Create EvaluacionRiesgo        │
       │                     │     (score, level, approved/reason)│
       │                     │                                    │
       │                     │  e) Update Solicitud:              │
       │                     │     APROBADO or RECHAZADO          │
       │                     │                                    │
       │                     │  f) All within @Transactional      │
       │                     └────────────────────────────────────┘
       │                                                          │
       │                     <─────────────────────────────────────
       │                     Response: SolicitudDTO with evaluation
       │                                                          │
       │  3. GET /api/solicitudes/{id}                            │
       │─────────────────────────────────────────>                │
       │                                                          │
       │<─────────────────────────────────────────                │
       │  Response: Full application with evaluation result       │
```

### Evaluation Policies

| Policy | Rule | Rejection Message |
|--------|------|-------------------|
| Quota/Income Ratio | Monthly payment ≤ 40% of salary | "La cuota excede el 40% del ingreso mensual" |
| Max Amount | Amount ≤ 5x monthly salary | "El monto solicitado excede 5 veces el salario" |
| Min Seniority | Affiliate for ≥ 6 months | "El afiliado no cumple la antigüedad mínima de 6 meses" |
| Risk Level | Score cannot be "ALTO" | "Score de riesgo muy bajo" |

---

## 📈 Observability & Monitoring

### Health Endpoints

```bash
# Application health
curl http://localhost:8080/actuator/health

# Detailed health with components
curl http://localhost:8080/actuator/health | jq
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}
```

### Metrics Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/metrics` | All available metrics |
| `/actuator/metrics/http.server.requests` | HTTP request metrics |
| `/actuator/metrics/jvm.memory.used` | JVM memory usage |
| `/actuator/prometheus` | Prometheus format metrics |

### Grafana Dashboard

Access Grafana at **http://localhost:3000** (admin/admin)

**Dashboard Panels:**
- 🚀 **Throughput** - Requests per second
- ⏱️ **Latency P95** - 95th percentile response time
- ✅ **Success Rate** - Percentage of successful requests
- 🔌 **Circuit Breaker** - State (CLOSED/OPEN/HALF-OPEN)
- 💚 **Service Health** - UP/DOWN status
- 💾 **Heap Memory** - JVM memory usage
- 🧵 **Threads** - Active thread count
- 📊 **HTTP Status Distribution** - Pie chart of response codes

### Log Format

```
2025-12-09 17:15:23 INFO  c.c.c.a.s.EvaluarSolicitudService - Evaluando solicitud ID: 1
2025-12-09 17:15:23 INFO  c.c.c.i.a.o.e.r.RiskCentralClient - Consultando risk-central para documento: 1017654311
2025-12-09 17:15:23 INFO  c.c.c.a.s.EvaluarSolicitudService - Evaluación completada: APROBADO
```

---

## 🧪 Testing

### Test Summary

```
Tests run: 109, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Run Tests

```bash
# Unit tests only
cd credit-application-service
mvn test

# All tests including integration
mvn verify

# With coverage report
mvn test jacoco:report
```

### Test Categories

| Category | Framework | Description |
|----------|-----------|-------------|
| Unit Tests | JUnit 5 + Mockito | Domain logic & use cases |
| Integration Tests | Spring Boot Test + MockMvc | REST controllers with security |
| E2E Tests | Testcontainers | Full flow with real PostgreSQL |

### Test Files

```
src/test/java/
├── application/service/
│   └── EvaluarSolicitudServiceTest.java    # Use case tests
├── infrastructure/adapter/in/web/
│   ├── AfiliadoControllerIntegrationTest.java
│   ├── SolicitudCreditoControllerIntegrationTest.java
│   ├── AuthControllerIntegrationTest.java
│   └── CreditoE2EIntegrationTest.java      # Full flow test
└── infrastructure/config/
    └── JwtServiceTest.java                  # JWT token tests
```

---

## 🗂️ Project Structure

```
CoopCredit/
├── credit-application-service/           # Main microservice
│   ├── src/main/java/com/coopcredit/credit/
│   │   ├── domain/                        # DOMAIN LAYER (Pure)
│   │   │   ├── model/                     # Entities: Afiliado, SolicitudCredito, etc.
│   │   │   ├── exception/                 # Domain exceptions
│   │   │   ├── policy/                    # Credit policies
│   │   │   └── validation/                # Domain validators
│   │   │
│   │   ├── application/                   # APPLICATION LAYER
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   ├── mapper/                    # MapStruct mappers
│   │   │   ├── port/                      # Ports (interfaces)
│   │   │   │   ├── in/                    # Input ports (use cases)
│   │   │   │   └── out/                   # Output ports (repositories)
│   │   │   └── service/                   # Use case implementations
│   │   │
│   │   └── infrastructure/                # INFRASTRUCTURE LAYER
│   │       ├── adapter/
│   │       │   ├── in/web/                # REST Controllers
│   │       │   └── out/
│   │       │       ├── persistence/       # JPA Adapters
│   │       │       └── external/risk/     # Risk Central Client
│   │       └── config/                    # Security, Swagger, etc.
│   │
│   ├── src/main/resources/
│   │   ├── application.yml                # Configuration
│   │   └── db/migration/                  # Flyway migrations
│   │
│   ├── src/test/                          # Tests
│   └── Dockerfile                         # Multi-stage build
│
├── risk-central-mock-service/             # Risk evaluation mock
│   ├── src/main/java/com/coopcredit/risk/
│   │   ├── controller/                    # REST endpoint
│   │   ├── service/                       # Risk calculation
│   │   └── model/                         # DTOs
│   └── Dockerfile
│
├── monitoring/                            # Observability stack
│   ├── prometheus.yml                     # Prometheus config
│   └── grafana/
│       ├── dashboards/                    # Grafana dashboards
│       └── provisioning/                  # Auto-provisioning
│
├── docker-compose.yml                     # Basic deployment
├── docker-compose-monitoring.yml          # With Prometheus/Grafana
├── run-monitoring.sh                      # Start with monitoring
├── run-tests.sh                           # Run all tests
├── GUIA_USUARIO.md                        # Spanish user guide
└── README.md                              # This file
```

---

## ⚙️ Configuration

### Application Properties

```yaml
# Credit Policies (configurable)
coopcredit:
  politicas:
    relacion-cuota-ingreso-maxima: 0.40   # Max 40% of income
    multiplicador-salario-monto-maximo: 5  # Max 5x salary
    antiguedad-minima-meses: 6             # Min 6 months seniority

# JWT Configuration
jwt:
  secret: your-256-bit-secret-key-here
  expiration: 86400000  # 24 hours in ms

# Circuit Breaker
resilience4j.circuitbreaker:
  instances:
    risk-central:
      slidingWindowSize: 10
      failureRateThreshold: 50
      waitDurationInOpenState: 30s
```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👨‍💻 Author

Developed as a demonstration of enterprise-grade hexagonal architecture with Spring Boot.

**Module 6 Performance Test** - Java + Spring Boot

---

## 📞 Support

For questions or issues, please open an issue in the repository.

---

> **Note**: This is an educational project demonstrating best practices in:
> - Hexagonal Architecture (Ports & Adapters)
> - Microservices Communication
> - JWT Security
> - Circuit Breaker Pattern
> - Observability (Metrics & Logging)
> - Containerization with Docker
> - Comprehensive Testing (Unit, Integration, E2E)
