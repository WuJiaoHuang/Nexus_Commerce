# Nexus Commerce

Nexus Commerce is a Spring Cloud microservice backend for an e-commerce workflow. It focuses on service decomposition, event-driven order processing, cache optimization, resilience, observability, and an AI assistant that calls backend business tools.

## Architecture

Core services:

- `api-gateway`: unified routing and authentication entry.
- `eureka`: service discovery.
- `user-service`: registration, login, JWT-related user data.
- `product-service`: product APIs with Redis cache-aside reads.
- `order-service`: order lifecycle and preview orchestration.
- `inventory-service`: inventory query, reservation, release, Kafka idempotent consumption.
- `ai-assistant-service`: tool-calling assistant for order, product, inventory, and after-sales questions.
- `security-platform`: shared JWT validation utilities.

Main workflow:

```text
Client
  -> API Gateway
  -> Order Service
  -> Kafka order-created event
  -> Inventory Service
  -> Kafka inventory-reserved / inventory-rejected event
  -> Order Service updates final order status
```

## Highlights

### Redis Cache

`product-service` uses cache-aside for product detail and product list queries:

```text
Request -> Redis -> miss -> MySQL -> write Redis -> response
```

Implemented safeguards:

- Empty-value cache for cache penetration.
- Per-key short lock for hot key breakdown.
- TTL jitter to reduce cache avalanche risk.
- Redis password is read from `REDIS_PASSWORD`; no real password is committed.

`inventory-service` caches hot inventory reads and refreshes cache after inventory mutation.

### Kafka Reliability

Order events include `eventId`, `eventType`, `orderId`, `userId`, `productId`, `quantity`, and `createdAt`.

`inventory-service` records consumed event IDs in `processed_event`, making reservation and cancellation idempotent. Consumer failures are retried and then routed to a dead-letter topic.

### Saga Consistency

The order-inventory flow uses eventual consistency:

- Order starts as `CREATED`.
- Inventory reservation success marks order as `RESERVED`.
- Inventory rejection marks order as `REJECTED:<reason>`.
- Order cancellation releases reserved inventory.

This keeps the order database and inventory database independently owned while still giving a clear compensation path.

### Resilience4j

`order-service` protects order preview calls with:

- RestTemplate connect/read timeout.
- Retry.
- Circuit breaker.
- Business fallback response instead of raw 500 errors.

### Observability

Spring Boot Actuator + Micrometer expose Prometheus metrics from business services. The local stack includes Prometheus and Grafana for:

- QPS.
- P95/P99 latency.
- Error rate.
- JVM metrics.
- Kafka consumer behavior.
- Database pool metrics.

### AI Assistant Tool Calling

`ai-assistant-service` is not only a chat wrapper. It identifies order/product-related prompts and calls backend tools:

- Order lookup.
- Product lookup.
- Inventory lookup.
- After-sales rule lookup.

Example prompt:

```text
我的订单为什么还没发货？orderId=xxx productId=yyy
```

The assistant calls the relevant services and returns a structured answer with tool results.

## Local Run

Create a local `.env` from the template:

```bash
cp .env.example .env
```

Set your local secrets in `.env`:

```text
SPRING_DATASOURCE_PASSWORD=<your-local-password>
REDIS_PASSWORD=<your-local-password>
APP_SECURITY_JWT_SECRET=<your-base64-secret>
```

Start local infrastructure:

```bash
docker compose up -d mysql redis zookeeper kafka prometheus grafana
```

Run services from separate terminals:

```bash
mvn -pl eureka spring-boot:run
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl product-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl order-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl inventory-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl ai-assistant-service spring-boot:run -Dspring-boot.run.profiles=local
```

Tables are created or updated by JPA with `spring.jpa.hibernate.ddl-auto=update`. The default database is `demo_db`.

Useful endpoints:

- Eureka: `http://localhost:8761`
- Product: `GET http://localhost:8081/product/{id}`
- Order preview: `POST http://localhost:8084/orders/preview`
- Inventory: `GET http://localhost:8085/inventory/{productId}`
- AI assistant: `POST http://localhost:8086/ai/assist`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

## Verification

Run tests:

```bash
mvn test
```

The test suite uses H2 for context tests so CI and reviewers do not need local MySQL credentials. Local manual runs use MySQL, Redis, and Kafka through environment variables.

Resume a failed Maven build:

```bash
mvn test -rf :<module-name>
```

## Interview Talking Points

- Why cache-aside was chosen and how penetration, breakdown, and avalanche are handled.
- Why order creation and inventory reservation are decoupled through Kafka.
- How `eventId` supports idempotent consumption.
- How Saga compensation works when inventory reservation fails.
- Why preview uses timeout, retry, circuit breaker, and fallback.
- What Prometheus/Grafana metrics help diagnose production issues.
- How AI tool calling connects LLM-style interaction with real backend services.
