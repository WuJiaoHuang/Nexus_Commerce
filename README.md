# Nexus Commerce

Nexus Commerce 是一个基于 Spring Cloud 的电商后端微服务项目。当前第一阶段聚焦已有主链能力：Gateway、JWT、Eureka、MySQL、Redis、Kafka、订单库存 Saga、Resilience4j 和基础可观测性。

本阶段没有引入 Doris、analytics-service、Elasticsearch、RabbitMQ、Redisson、Kubernetes、Flink、RocketMQ，也没有新增微服务。

## 架构

当前主工程模块：

- `eureka`：服务注册与发现。
- `api-gateway`：统一入口、路由和 JWT 鉴权。
- `user-service`：用户注册、登录和 JWT 生成。
- `product-service`：商品管理，使用 MySQL 持久化和 Redis Cache Aside。
- `order-service`：订单创建、订单状态流转、订单预览容错。
- `inventory-service`：库存查询、预留、释放、Kafka 幂等消费。
- `security-platform`：共享 JWT 校验组件。
- `ai-assistant-service`：保留已有模块，本阶段不继续增强。

核心调用链：

```text
Client
  -> API Gateway
  -> User / Product / Order / Inventory

Order Service
  -> Kafka order-created
  -> Inventory Service
  -> Kafka inventory-reserved / inventory-rejected / inventory-released
  -> Order Service
```

## 本地运行

复制环境变量模板：

```bash
cp .env.example .env
```

按本地环境设置 `.env`。示例值只用于开发，不要提交真实密码或个人 JWT secret。

常用变量：

```text
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/demo_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=app
SPRING_DATASOURCE_PASSWORD=app
REDIS_PASSWORD=nexus_dev
APP_SECURITY_JWT_SECRET=<base64-encoded-256-bit-dev-secret>
```

启动基础设施：

```bash
docker compose up -d mysql redis zookeeper kafka prometheus grafana
```

启动服务：

```bash
mvn -pl eureka spring-boot:run
mvn -pl user-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl product-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl order-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl inventory-service spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl api-gateway spring-boot:run
```

如果本机 3306 已被占用，可以改用：

```bash
MYSQL_PORT=13307 docker compose up -d mysql redis zookeeper kafka
```

并同步设置：

```text
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:13307/demo_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

## 业务能力

### Gateway 和 JWT

`api-gateway` 路由以下路径：

- `/user/**`
- `/product/**`
- `/orders/**`
- `/inventory/**`

`POST /user` 和 `POST /user/login` 是公开接口。其他业务接口需要 `Authorization: Bearer <jwt>`。

JWT secret 通过 `APP_SECURITY_JWT_SECRET` 注入。Gateway 使用 `security-platform` 的 `JwtTokenValidator` 解析 token，并在校验后写入可信的用户上下文请求头。

### Product + Redis

`product-service` 使用 MySQL 保存商品数据，使用 Redis 做商品详情缓存：

```text
GET Product
  -> Redis hit
  -> Redis miss
  -> MySQL
  -> write Redis
```

当前已包含空值缓存、短锁和 TTL jitter。商品写入后会删除相关缓存，避免继续读取旧值。

### Order + Inventory + Kafka

订单创建后，`order-service` 保存订单并发送 `order-created` 事件。事件包含：

- `eventId`
- `eventType`
- `orderId`
- `userId`
- `productId`
- `quantity`
- `createdAt`

`inventory-service` 消费订单事件后判断库存：

- 库存足够：预留库存，发送 `inventory-reserved`，订单更新为 `RESERVED`。
- 库存不足：发送 `inventory-rejected`，订单更新为 `REJECTED:<reason>`。

### Saga 取消补偿

已预留库存的订单取消时：

```text
Order RESERVED
  -> cancel
  -> order-cancelled
  -> Inventory release
  -> inventory-released
  -> Order CANCELLED
```

库存预留和释放使用 `processed_event` 做 Kafka 消费幂等，重复事件不会重复扣减或重复释放库存。

### Kafka Retry / DLT

`inventory-service` 配置了有限重试和死信 Topic：

- 重试：`DefaultErrorHandler` + `FixedBackOff`
- DLT：`inventory-dead-letter`

默认不会触发测试失败。需要验证 DLT 时，可以临时设置：

```text
APP_KAFKA_ENABLE_TEST_FAILURE=true
```

然后向 `order-created` 发送包含 `forceFailure=true` 的测试消息。失败日志包含 `eventId` 和 `orderId`，超过重试次数后消息进入 `inventory-dead-letter`。

### Resilience4j

`order-service` 的订单预览接口使用 Resilience4j：

- timeout
- retry
- circuit breaker
- fallback

当 `product-service` 不可用时，`POST /orders/preview` 返回业务降级响应，而不是直接暴露 raw 500。

## Resilience & Observability

### Resilience4j

本次容错改造作用在 `ai-assistant-service` 已有的真实 HTTP 工具调用上：

```text
ai-assistant-service
        |
        +--> order-service
        |
        +--> product-service
        |
        +--> inventory-service
```

三条下游调用分别配置独立的 Resilience4j instance：

- `orderService`
- `productService`
- `inventoryService`

每个调用都启用：

- Retry
- CircuitBreaker
- Fallback

当下游服务不可用时，AI Assistant 返回业务降级结果，例如 `Inventory service is temporarily unavailable`，而不是直接向用户暴露 500。

### Monitoring

主要 Spring Boot 服务通过 Actuator 暴露 Prometheus 指标：

```text
Spring Boot Services
       |
       | /actuator/prometheus
       v
   Prometheus
       |
       v
    Grafana
```

当前监控覆盖：

- QPS
- P95
- P99
- 5xx
- JVM Heap
- Threads
- GC
- CPU
- CircuitBreaker
- Retry

访问地址：

```text
Prometheus: http://localhost:9090
Grafana:    http://localhost:3000
```

### AIOps Sentinel 观测接口

`AIOps Sentinel` 当前通过以下三类接口观测本项目，不需要额外日志平台或链路追踪组件：

- Prometheus Metrics：各 Spring Boot 服务的 `/actuator/prometheus`。
- Actuator Health：各 Spring Boot 服务的 `/actuator/health`。
- 文件日志：各服务写入项目根目录下的 `logs/*.log`。

本项目已为主要服务配置日志文件路径：

```text
logs/api-gateway.log
logs/user-service.log
logs/product-service.log
logs/order-service.log
logs/inventory-service.log
```

`order-service` 的订单预览链路会在下游异常时写入 WARN 日志。停掉 `product-service` 后调用：

```text
POST http://localhost:8084/orders/preview
```

可以得到业务降级响应，同时 `logs/order-service.log` 会出现 product-service 调用失败和 fallback 触发记录，供 AIOps Sentinel 的 `query_logs` 工具检索。

## 测试

运行全仓测试：

```bash
mvn clean test
```

打包：

```bash
mvn clean package
```

当前主工程不包含 `monolithic` 模块；如果本地 Maven 仓库不可写，可以临时指定项目内仓库：

```bash
mvn -Dmaven.repo.local=.m2repo clean test
mvn -Dmaven.repo.local=.m2repo clean package
```

