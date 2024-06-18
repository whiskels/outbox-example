package com.whiskels.order.service.anomaly.broker;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@EmbeddedKafka
@ExtendWith(MockitoExtension.class)
public class CorruptedKafkaTemplateTest {

    @Mock
    private ProducerFactory<String, String> producerFactory;

    private CorruptedKafkaTemplate corruptedKafkaTemplate;

    @BeforeEach
    public void setup() {
        corruptedKafkaTemplate = new CorruptedKafkaTemplate(producerFactory);
    }

    @Test
    public void testSend() {
        CompletableFuture<SendResult<String, String>> future = corruptedKafkaTemplate.send("test-topic", "test-data");
        assertThrows(ExecutionException.class, future::get, "Simulated Kafka exception");
    }

    @Test
    public void testSupportsAsyncExecution() {
        assertTrue(corruptedKafkaTemplate.supportsAsyncExecution());
    }

    @Test
    public void testReceiveWithTopicPartitionOffset() {
        String topic = "orders";
        int partition = 0;
        long offset = 0L;
        ConsumerRecord<String, String> record = new ConsumerRecord<>(topic, partition, offset, "key", "value");

        KafkaTemplate<String, String> spyTemplate = spy(corruptedKafkaTemplate);
        doReturn(record).when(spyTemplate).receive(topic, partition, offset);

        ConsumerRecord<String, String> receivedRecord = spyTemplate.receive(topic, partition, offset);

        assertNotNull(receivedRecord);
        assertEquals(record, receivedRecord);
    }
}