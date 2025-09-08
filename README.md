# Simple Outbox Pattern Implementation with Spring Boot and Kafka

![CI](https://github.com/whiskels/outbox-example/actions/workflows/ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/whiskels/outbox-example/graph/badge.svg?token=F51GAFZ63Q)](https://codecov.io/gh/whiskels/outbox-example)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fwhiskels%2Foutbox-example&count_bg=%233DC8C1&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

Demo project to illustrate the benefits of using
the [microservices.io - Transactional Outbox](https://microservices.io/patterns/data/transactional-outbox.html) pattern.

In event-driven architecture, applications often need to persist data and send messages to a message broker like Kafka
or RabbitMQ. However, naive implementations may encounter the following issues:

- **Sending event while transaction is open**: Data might be lost if the transaction fails.
- **Sending event outside of transaction**: Event might be lost if sending fails.

This repository demonstrates these consistency anomalies and provides a solution using the outbox pattern.

## Table of Contents

- [Setup](#setup)
    - [Prerequisites](#prerequisites)
    - [Project Structure](#project-structure)
    - [Running the Application](#running-the-application)
- [Anomaly Simulation](#anomaly-simulation)
    - [Order Service Code Example](#order-service-code-example)
    - [Failed Commit Anomaly](#failed-commit-anomaly)
    - [Failed Broker Delivery Anomaly](#failed-broker-delivery-anomaly)
- [Outbox Pattern](#outbox-pattern)
    - [Pros](#pros)
    - [Cons](#cons)
- [Other Approaches](#other-approaches)
    - [Integrated Transactional Messaging](#integrated-transactional-messaging)
    - [Database Triggers](#database-triggers)
- [Out of Scope](#out-of-scope)

## Setup

### Components

| Component         | Port | Description                                        |
|-------------------|------|----------------------------------------------------|
| order-service     | 8078 | Simulates order creation, produces events          |
| logistics-service | 8079 | Consumes order events                              |
| Postgres          | 5432 | Relational database for services                   |
| Kafka             | 9092 | Message broker for event streaming                 |
| Conduktor         | 80   | Management UI for Kafka cluster                    |
| Prometheus        | 9090 | Metrics collection and monitoring                  |
| Loki              | 3100 | Centralized log aggregation                        |
| Tempo             | 3200 | Distributed tracing backend                        |
| Grafana           | 3000 | Visualization dashboards for metrics, logs, traces |
| Order simulator   |      | Simulates order creation                           |

### Key Features

- Transactional Outbox Pattern: Guarantees at-least-once delivery of events, preventing data inconsistencies.
- Anomaly Simulation: The ability to simulate common failure modes (e.g., database commit failure, broker delivery
failure) to demonstrate the effectiveness of the outbox pattern.
- Observability: Integrated metrics, tracing, and logging to provide insights into the system's behavior.

### Running the application

Build java containers and run compose:
`docker-compose up --build`

SIMULATION_STRATEGY environment variable can be set to one of the following values:

- `NONE` - naive send-and-forget approach
- `FAILED_COMMIT_ANOMALY` - simulates a failure after sending the message to Kafka but before committing the transaction
- `FAILED_BROKER_ANOMALY` - simulates a failure during message sending to Kafka
- `OUTBOX` - message production via outbox pattern
- `null` - random strategy will be chosen (weights: 90% NONE, 5% FAILED_COMMIT_ANOMALY, 5% FAILED_BROKER_ANOMALY)

Example:
`SIMULATION_STRATEGY=OUTBOX docker-compose up --build`

### Project Structure

This application consists of a multi-module Gradle project with two services:

#### Order Service

- **Responsibilities**: Taking orders and sending events to Kafka.
- **Endpoint**: `POST /orders`
    - Optional header: `X-Simulation-Strategy` with possible values:
        - OUTBOX
        - FAILED_COMMIT_ANOMALY
        - FAILED_BROKER_DELIVERY
        - NONE
- **Swagger UI**: [http://localhost:8078/swagger-ui/index.html](http://localhost:8078/swagger-ui/index.html)

### Example Request

To create an order, use the following example:

```sh
curl -X 'POST' \
  'http://localhost:8078/orders' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -H 'X-Simulation-Strategy: OUTBOX' \
  -d '{
  "userId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "items": [
    {
      "productId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "quantity": 1
    }
  ]
}'
```

#### Logistics Service

- **Responsibilities**: Consumes events and processes orders.

## Anomaly Simulation mechanism

### Order Service Code Example

```java

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractOrderService implements SimulatedOrderService {
    private final OrderMapper orderMapper;
    private final CrudRepository<Order, UUID> orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    @Override
    @Transactional
    public OrderCreatedDto create(final OrderDto order) {
        var orderEntity = orderRepository.save(orderMapper.toEntity(order));
        log.info("Saved order from user {} with id {}", order.getUserId(), orderEntity.getId());
        var dto = orderMapper.toDto(orderEntity);
        log.info("Preparing to send order with id to kafka {}", orderEntity.getId());
        kafkaTemplate.send(topic, JsonUtil.toJson(dto))
                .whenComplete(
                        (result, ex) -> {
                            if (result != null && ex == null) {
                                log.info("Sent order with id {} to kafka", dto.getId());
                            } else {
                                log.error("Failed to send order with id {} to kafka", dto.getId(), ex);
                            }
                        });
        return dto;
    }
}

```

### Failed Commit Anomaly

To simulate the failed commit anomaly, the exception is thrown inside transaction after the message is sent to Kafka.

This results in the message being sent to Kafka, but the transaction being rolled back, so the data is not persisted in
the database. The logistics-service will consume the message, but the entry will not be
present in the order-service database.

### Failed Broker Delivery Anomaly

To simulate the failed broker delivery anomaly KafkaTemplate is modified to throw an exception in
the `send()` call. The data is persisted in the database, but no event is sent. The order-service database will
have the entry, but the message will not be consumed by the logistics-service.
> This example also demonstrates an often misunderstood concept of the `KafkaTemplate` - since `send()` is asynchronous
> and returns a `ListenableFuture`, the exception is not thrown immediately, but rather when the future is completed.
> This means that the exception is not caught by the `@Transactional` method and the transaction is committed.


> **_Naive attempt to resolve by awaiting future completion:_**
>
> Before implementing the outbox pattern, a naive attempt can be made to resolve the consistency issues by awaiting the
> completion of the future returned by the `KafkaTemplate.send` method.
> This approach involves blocking the transaction until the Kafka broker confirms the message delivery. While this
> ensures that the transaction would only commit if the message was successfully sent, it introduces significant latency
> and blocking behavior into the system. Additionally, this approach does not fully address the issue of failed commits.

## Outbox Pattern

The outbox pattern resolves the anomalies mentioned above by persisting the event in the database within the same
transaction as the data. A separate scheduler then processes the events, ensuring at-least-once delivery semantics.
> **_Why does the outbox pattern only guarantee the at-least-once delivery?_**
>
> The outbox pattern does not provide exactly-once delivery semantics because the message delivery is not transactional.
> If the message is sent but the scheduler fails before marking the message as processed, the message will be sent
> again.

### Pros

- Non-blocking message sending
- Retry mechanism in case of failure

### Cons

- Additional implementation overhead
- Increased message delivery latency

## Other Approaches

### Integrated Transactional Messaging

Some messaging systems, like Kafka, support transactions natively.

**Pros**:

- Simplifies architecture by using Kafka's transactional capabilities

**Cons**:

- Tightly couples application logic with Kafka's transactional API
- Longer response times due to Kafka's producer characteristics

### Database Triggers

Using database triggers to publish events after the transaction commits.

**Pros**:

- Ensures consistency using database features

**Cons**:

- Depends on database-specific features
- Can be complex to manage

## Out of Scope

- Message order preservation
- Retry of "stuck" messages, which may cause deadlocks in the outbox table
- Concurrent outbox processing
- Locking mechanism
- Idempotency on producer for exactly-once semantics
