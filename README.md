# Online Library Microservices

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-22-DD0031?logo=angular&logoColor=white)](https://angular.dev/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.4-231F20?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?logo=redis&logoColor=white)](https://redis.io/)

A full-stack online library management system built with a microservices architecture. Users can browse and order books, librarians manage the catalog and fulfill orders, and admins oversee the entire platform — all through a single Angular frontend communicating with a Spring Cloud Gateway.

---

## Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [API Endpoints](#api-endpoints)
- [Screenshots](#screenshots)

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Angular Frontend                      │
│                 (localhost:4200)                         │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP
┌─────────────────────▼───────────────────────────────────┐
│              Spring Cloud Gateway                        │
│                 (localhost:8080)                         │
│   JWT Auth · Rate Limiting (Redis) · CORS               │
└──────┬──────────────┬──────────────┬────────────────────┘
       │              │              │
┌──────▼──────┐ ┌─────▼──────┐ ┌───▼────────┐
│ User Service│ │Book Service│ │Order Service│
│  port 8083  │ │  port 8081 │ │  port 8082  │
│   user_db   │ │  book_db   │ │  order_db   │
└──────┬──────┘ └─────┬──────┘ └───┬─────────┘
       │              │             │
       └──────────────▼─────────────┘
                 Apache Kafka
          (async event communication)
```

### Microservices

| Service | Port | Responsibility |
|---|---|---|
| **gateway-service** | 8080 | Single entry point — routes requests, validates JWT tokens, applies rate limiting via Redis |
| **user-service** | 8083 | Authentication & authorization — registration, login, JWT issuance, user and librarian management |
| **book-service** | 8081 | Book catalog — CRUD for books, cover image uploads, stock management |
| **order-service** | 8082 | Order lifecycle — place, view, and manage book orders; communicates with book-service via Feign |
| **library-frontend** | 4200 | Angular SPA — role-based UI for readers, librarians, and admins |

### Infrastructure

| Component | Purpose |
|---|---|
| **PostgreSQL 15** | Separate database per service (`user_db`, `book_db`, `order_db`) |
| **Apache Kafka** | Async events between services (e.g., user registered → welcome flow) |
| **Redis 7.2** | JWT token blacklist (logout) + API rate limiting in the gateway |
| **Liquibase** | Database schema migrations per service |

---

## Tech Stack

**Backend**
- Java 21
- Spring Boot 4.0.6
- Spring Cloud Gateway 2025.1.1 (WebFlux)
- Spring Security + JJWT 0.12.5
- Spring Data JPA + Liquibase
- Spring Kafka
- OpenFeign (service-to-service HTTP)
- MapStruct 1.6.3
- Lombok

**Frontend**
- Angular 22
- Angular Material
- ngx-translate (i18n — English / Armenian)
- RxJS

**Infrastructure**
- Docker & Docker Compose
- PostgreSQL 15
- Apache Kafka (KRaft mode, no ZooKeeper)
- Redis 7.2

---

## Prerequisites

| Tool | Minimum Version |
|---|---|
| Docker | 24+ |
| Docker Compose | 2.20+ |
| Java (JDK) | 21 (for local builds only) |
| Node.js | 20+ (for local frontend dev only) |

> **Note:** To run the project with Docker Compose you only need Docker. Java and Node.js are only required if you want to run services outside of Docker.

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/online-library-microservices.git
cd online-library-microservices
```

### 2. Configure environment variables

```bash
cp .env.example .env
```

Open `.env` and fill in your values:

```env
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_jwt_secret_base64_encoded
GATEWAY_SECRET=your_internal_gateway_secret
KAFKA_CLUSTER_ID=your_kafka_cluster_id
```

> Generate a secure JWT secret: `openssl rand -base64 64`
> Generate a Kafka cluster ID: `docker run --rm confluentinc/cp-kafka:7.4.0 kafka-storage random-uuid`

### 3. Build and start all services

```bash
# Build all Spring Boot JARs
cd user-service  && ./mvnw clean package -DskipTests && cd ..
cd book-service  && ./mvnw clean package -DskipTests && cd ..
cd order-service && ./mvnw clean package -DskipTests && cd ..
cd gateway-service && ./mvnw clean package -DskipTests && cd ..

# Start everything
docker compose up -d
```

### 4. Start the frontend (optional — for development)

```bash
cd library-frontend
npm install
npm start
```

Open [http://localhost:4200](http://localhost:4200)

The API gateway is available at [http://localhost:8080](http://localhost:8080)

### Default ports at a glance

| Service | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API Gateway | http://localhost:8080 |
| Book Service | http://localhost:8081 |
| Order Service | http://localhost:8082 |
| User Service | http://localhost:8083 |
| PostgreSQL | localhost:5432 |
| Kafka | localhost:9092 |
| Redis | localhost:6379 |

---

## Environment Variables

All secrets are managed via a `.env` file at the project root. See [`.env.example`](.env.example) for the required variables.

| Variable | Description |
|---|---|
| `DB_PASSWORD` | PostgreSQL password for the `postgres` user |
| `JWT_SECRET` | Base64-encoded secret used to sign JWT tokens |
| `GATEWAY_SECRET` | Internal header secret shared between the gateway and backend services |
| `KAFKA_CLUSTER_ID` | Unique ID for the Kafka KRaft cluster |

> The `.env` file is listed in `.gitignore` and will never be committed.

---

## API Endpoints

All requests go through the gateway at `http://localhost:8080`.

### Auth (`/api/user`) — public

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/user/register` | Register a new user |
| `POST` | `/api/user/login` | Login and receive JWT |
| `POST` | `/api/user/logout` | Invalidate JWT (blacklist in Redis) |
| `POST` | `/api/user/refresh` | Refresh access token |

### Users (`/api/user`) — authenticated

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/user/profile` | Get current user profile |
| `PUT` | `/api/user/profile` | Update profile |
| `PUT` | `/api/user/change-password` | Change password |

### Admin (`/api/admin`) — ADMIN role

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/admin/users` | List all users |
| `GET` | `/api/admin/librarians` | List all librarians |
| `POST` | `/api/admin/librarians` | Create a librarian account |
| `DELETE` | `/api/admin/librarians/{id}` | Remove a librarian |
| `GET` | `/api/admin/stats` | Platform statistics |

### Books (`/api/books`)

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/books` | Public | List / search books |
| `GET` | `/api/books/{id}` | Public | Get book details |
| `GET` | `/api/books/{id}/cover` | Public | Download cover image |
| `POST` | `/api/books` | Librarian | Create a book |
| `PUT` | `/api/books/{id}` | Librarian | Update a book |
| `DELETE` | `/api/books/{id}` | Librarian | Delete a book |
| `POST` | `/api/books/{id}/cover` | Librarian | Upload cover image |

### Orders (`/api/orders`) — authenticated

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/orders` | Place a new order |
| `GET` | `/api/orders/my` | Get current user's orders |
| `GET` | `/api/orders` | Get all orders (Librarian/Admin) |
| `PUT` | `/api/orders/{id}/status` | Update order status (Librarian/Admin) |
| `DELETE` | `/api/orders/{id}` | Cancel an order |

---

## Screenshots

> Screenshots will be added after the first deployment.

| View | Preview |
|---|---|
| Home / Book Search | _coming soon_ |
| Book Details & Order | _coming soon_ |
| Librarian Dashboard | _coming soon_ |
| Admin Dashboard | _coming soon_ |

---

## License

This project is open-source and available under the [MIT License](LICENSE).
