# Simple Outbox example

![CI](https://github.com/whiskels/outbox-example/actions/workflows/ci.yml/badge.svg)
[![codecov](https://codecov.io/gh/whiskels/outbox-example/graph/badge.svg?token=F51GAFZ63Q)](https://codecov.io/gh/whiskels/outbox-example)
[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fwhiskels%2Foutbox-example&count_bg=%233DC8C1&title_bg=%23555555&icon=&icon_color=%23E7E7E7&title=hits&edge_flat=false)](https://hits.seeyoufarm.com)

This repository showcases an example of a simple outbox pattern implementation using Spring Boot and Kafka.
[https://microservices.io/patterns/data/transactional-outbox.html](See: microservices.io - Transactional Outbox)

## Problem statement

Most use cases for applications that implement event-driven architecture involve persistence of some data and then
sending messages to a message broker like Kafka or RabbitMQ.

We want both things to happen, but with naive implementations we might face some issues:

- if we send event while transaction is still open - we might lose the data in the database if the transaction fails
- if we send event outside of transaction - we might lose the event if the sending fails

This repository aims to demonstrate both consistency anomalies and how to solve them using the outbox pattern.

## Setup

Java 21, Docker, Kafka, Postgres

Application consists of a multi-module Gradle project with two services:

- order-service - responsible for taking orders and sending events to Kafka
    - starts on port 8078
    - exposes endpoint POST /orders to create an order
      -  Endpoint accepts an optional argument simulationStrategy:
        - OUTBOX (default)
        - FAILED_COMMIT_ANOMALY
        - FAILED_BROKER_DELIVERY
    - provides Swagger-UI on http://localhost:8078/swagger-ui/index.html#/
    - uses Postgres to store order data
    - produces orders to Kafka
- logistics-service - consumes events and starts order processing
    - starts on port 8079
    - consumes orders from Kafka and logs the result

### Anomaly simulation

To simulate an anomaly our testing setup would be:

```java

@Service
@RequiredArgsConstructor
@Slf4j
class OrderService {
    private final OrderMapper orderMapper;
    private final CrudRepository<Order, UUID> orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactionl
    public OrderCreatedDto create(final OrderDto order) {
        var orderEntity = orderMapper.toEntity(order);
        log.info("Saved order from user {} with id {}", order.getUserId(), orderEntity.getId());
        orderEntity = orderRepository.save(orderEntity);
        var dto = orderMapper.toDto(orderEntity);
        log.info("Preparing to send order with id to kafka {}", orderEntity.getId());
        kafkaTemplate.send("orders", JsonUtil.toJson(dto))
                .whenComplete(
                        (result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send order with id {} to kafka", dto.getId(), ex);
                            } else if (result != null) {
                                log.info("Sent order with id {} to kafka", dto.getId());
                            }
                        });
    }
}
```

In a simple service we will be persisting a new entry in the database and also sending a message to Kafka.

### Failed Commit anomaly

To simulate the failed commit anomaly the above code is modified to throw an exception after the message is sent to
Kafka.
This would lead to a situation where the message is sent to Kafka, but the transaction is rolled back and the data is
not persisted in the database.
As the result, we can observe consumption of the message in the logistics-service, but the entry is not present in the
database of order-service

### Failed Broker Delivery anomaly

To simulate the failed broker delivery anomaly the above code is modified to throw an exception in the KafkaTemplate
call.
Since KafkaTemplate provides an instance of CompletableFuture transaction is not rolled back and the data is persisted
in the database, but no event is sent.
As the result, we can observe entry in the database of the order-service (and receive a response from the
order-service), but the message is not consumed by the logistics-service

## Outbox

Outbox pattern is used to resolve the anomalies mentioned above.
With this approach - we persist the event in the database in the same transaction as the data was persisted.
Then a separate scheduler is used to process the events.
This approach allows us to achieve at-least-once delivery semantics.
If memory was sent successfully, but database failed to be updated - event will be resent.

Pros:

- We still use non-blocking way of sending messages
- In case of failure we can retry sending the message

Cons:

- Additional overhead on implementation of the outbox
- Additional latency to message delivery

## Other approaches

### Integrated Transactional Messaging

Some messaging systems support transactions natively. For example, Kafka provides transactional APIs that can be used to
publish messages as part of a database transaction.

Pros:

- Simplifies the architecture by using Kafka's own transaction capabilities

Cons:

- Tightly couples your application logic with Kafka's transactional API.
- Longer response times due to the nature of the Kafka's producer (batching, retries, linger period, acks etc.)

### Database triggers

Using database triggers to publish events after the transaction commits. This approach is closely tied to the
capabilities of your database.
Pros:

- Ensures consistency and utilizes database features.

Cons:

- Depends on database-specific features and can be complex to manage.

## Out of scope

- Provision of partition keys to ensure message ordering inside partitions
- Retry of "stuck" messages - in current implementation there is a slight possibility of a some sort of a eadlock if
  some messages are
  stuck in the outbox table