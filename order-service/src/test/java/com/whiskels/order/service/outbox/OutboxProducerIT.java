package com.whiskels.order.service.outbox;

import com.whiskels.order.TestConsumer;
import com.whiskels.order.TestcontainersIT;
import com.whiskels.order.entity.OutboxEvent;
import com.whiskels.order.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OutboxProducerIT extends TestcontainersIT {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxProducer outboxProducer;

    @Autowired
    private TestConsumer testConsumer;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    void testSend_EventsFoundAndSentSuccessfully() {
        OutboxEvent event = new OutboxEvent();
        event.setTopic(topicName);
        event.setEvent("test-event");
        var persistedEvent = outboxEventRepository.save(event);

        outboxProducer.send();

        await().pollInterval(Duration.ofSeconds(3))
                .atMost(10, SECONDS)
                .untilAsserted(() -> assertTrue(messageSuccess(persistedEvent.getId())));
    }

    private boolean messageSuccess(UUID eventId) {
        return outboxEventRepository.findById(eventId)
                .map(OutboxEvent::getSent)
                .isPresent() && testConsumer.messageConsumed();
    }

    @Test
    void testSend_NoEventsFound() {
        outboxProducer.send();

        Iterable<OutboxEvent> events = outboxEventRepository.findAll();
        assertFalse(events.iterator().hasNext());
    }
}