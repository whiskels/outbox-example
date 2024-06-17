# Simple Outbox example
This repository showcases an example of a simple outbox pattern implementation using Spring Boot and Kafka.

## Problem statement

TODO add link to microservices.io
TODO describe transactional producer and diff from 2PC

Most use cases for applications that require event-driven architecture involve persistence of some data and then sending messages to a message broker like Kafka or RabbitMQ

We want both things to happen, but with naive implementations we might face some issues:
```java
class PaymentService {
    private AccountService accountService;
    private KafkaProducer kafkaProducer;

    @Transactional
    public void processPayment(User user, Payment payment) {
        // Deduct amount from user's account
        final var transactionResult = accountService.deductAmount(user, payment.getAmount());

        // Send an event to Kafka indicating that the payment was processed
        kafkaProducer.send(new PaymentProcessedEvent(user, payment, transactionResult));

        // If the transaction fails after this point, the payment record in the database will roll back,
        // but the Kafka event stating that payment was processed will still be sent out.
    }
}
```

```java
class PaymentService {
    private AccountService accountService;
    private KafkaProducer kafkaProducer;

    public void processPayment(User user, Payment payment) {
        // Deduct amount from user's account
        // This operation is now transactional within the AccountService
        final var transactionResult = accountService.deductAmount(user, payment.getAmount());

        // Send an event to Kafka indicating that the payment was processed
        // This operation is outside of the transactional scope of deductAmount
        kafkaProducer.send(new PaymentProcessedEvent(user, payment, transactionResult));

        // If kafkaProducer is unable to send the message - information about the payment is lost
    }
}
```
