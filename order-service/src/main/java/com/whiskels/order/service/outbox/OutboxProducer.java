package com.whiskels.order.service.outbox;

import com.whiskels.order.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
class OutboxProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxEventRepository outboxEventRepository;

    @Scheduled(fixedRate = 1000)
    public void send() {
        var events = outboxEventRepository.findAllNotSent();
        events.forEach(event -> {
            kafkaTemplate.send(event.getTopic(), event.getEvent()).whenComplete(
                    (result, ex) -> {
                        if (result != null) {
                            event.setSent(LocalDateTime.now());
                            log.error("Sent event {} at {}", event.getId(), event.getSent());
                            outboxEventRepository.save(event);
                        } else if (ex != null) {
                            log.error("Failed to send event {}:", event.getId(), ex);
                        }
                    }
            );
        });
    }

}
