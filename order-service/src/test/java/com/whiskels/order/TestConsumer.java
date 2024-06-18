package com.whiskels.order;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

public class TestConsumer {
    private final CountDownLatch latch = new CountDownLatch(1);
    @KafkaListener(topics = "${producer.topic}")
    public void listen(ConsumerRecord<?, ?> consumerRecord) {
        latch.countDown();
    }


    public boolean messageConsumed() {
        return latch.getCount() == 0;
    }
}
