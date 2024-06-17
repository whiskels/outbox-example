package com.whiskels.order.service.anomaly.broker;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Component
class CorruptedKafkaTemplate extends KafkaTemplate<String, String> {

    @Override
    public CompletableFuture<SendResult<String, String>> send(final String topic, final String data) {
        return CompletableFuture.failedFuture(new RuntimeException("Simulated Kafka exception"));
    }

    public CorruptedKafkaTemplate(final ProducerFactory<String, String> producerFactory) {
        super(producerFactory);
    }

    @Override
    public boolean supportsAsyncExecution() {
        return super.supportsAsyncExecution();
    }

    @Override
    public ConsumerRecord<String, String> receive(final String topic, final int partition, final long offset) {
        return super.receive(topic, partition, offset);
    }

    @Override
    public ConsumerRecords<String, String> receive(final Collection<TopicPartitionOffset> requested) {
        return super.receive(requested);
    }
}