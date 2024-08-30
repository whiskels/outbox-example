package com.whiskels.order.service.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
@RequiredArgsConstructor
class OutboxProducerScheduler {
    private final OutboxProducer producer;

    @Scheduled(fixedRate = 1000)
    void sendScheduled() {
        producer.send();
    }
}
