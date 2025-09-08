package com.whiskels.order.api.service.outbox;

import com.whiskels.order.api.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
class OutboxProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;

    void send() {
        var events = outboxEventRepository.findNotSentBatch();
        var futures = events.stream()
                .map(event -> kafkaTemplate.send(event.getTopic(), event.getEvent())
                        .whenComplete(
                                (result, ex) -> {
                                    if (result != null && ex == null) {
                                        var setAt = LocalDateTime.now();
                                        log.info("Sent event {} to topic {} at {}", event.getId(), event.getTopic(), setAt);
                                        outboxEventRepository.markSent(event.getId(), LocalDateTime.now());
                                    } else {
                                        log.error("Failed to send event {}:", event.getId(), ex);
                                    }
                                }
                        ))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
    }
}
