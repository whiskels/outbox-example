package com.whiskels.order.service.outbox;

import com.whiskels.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
class OutboxProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;

    void send() {
        var events = outboxEventRepository.findNotSentBatch();
        events.forEach(event -> kafkaTemplate.send(event.getTopic(), event.getEvent()).whenComplete(
                (result, ex) -> {
                    if (result != null && ex == null) {
                        event.setSent(LocalDateTime.now());
                        log.info("Sent event {} to topic {} at {}", event.getId(), event.getTopic(), event.getSent());
                        outboxEventRepository.save(event);
                    } else {
                        log.error("Failed to send event {}:", event.getId(), ex);
                    }
                }
        ));
    }
}
