package com.whiskels.order.api.service.outbox;

import com.whiskels.order.BaseIT;
import com.whiskels.order.TestConsumer;
import com.whiskels.order.api.domain.OutboxEvent;
import com.whiskels.order.api.dto.CreateOrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxProducerIT extends BaseIT {
    @Autowired
    private OutboxProducer outboxProducer;

    @Autowired
    private TestConsumer testConsumer;

    @BeforeEach
    void setUp() {
        outboxEventRepository.deleteAll();
    }

    @Test
    void testSend_EventsFoundAndSentSuccessfully() throws Exception {
        var eventDto = new CreateOrderResponse(UUID.randomUUID(), UUID.randomUUID());
        OutboxEvent event = new OutboxEvent();
        event.setTopic(topicName);
        event.setEvent(MAPPER.writeValueAsString(eventDto));
        var persistedEvent = outboxEventRepository.save(event);

        outboxProducer.send();

        await().atMost(10, SECONDS)
                .untilAsserted(() -> assertTrue(outboxEventRepository.findById(persistedEvent.getId())
                        .map(OutboxEvent::getSent)
                        .isPresent() && testConsumer.hasMessageForUser(eventDto.getUserId())));
    }

    @Test
    void testSend_NoEventsFound() {
        outboxProducer.send();

        Iterable<OutboxEvent> events = outboxEventRepository.findAll();
        assertFalse(events.iterator().hasNext());
    }
}