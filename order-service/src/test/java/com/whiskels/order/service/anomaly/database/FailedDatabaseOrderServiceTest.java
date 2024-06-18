package com.whiskels.order.service.anomaly.database;

import com.whiskels.order.TestConsumer;
import com.whiskels.order.TestcontainersIT;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.entity.Order;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;

import java.time.Duration;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FailedDatabaseOrderServiceTest extends TestcontainersIT {

    @Autowired
    private CrudRepository<Order, UUID> orderRepository;

    @Autowired
    private FailedDatabaseOrderService service;

    @Autowired
    private TestConsumer testConsumer;

    @Test
    void testSend() {
        var dto = new OrderDto();
        dto.setUserId(UUID.randomUUID());
        assertThrows(OptimisticLockException.class, () -> service.create(dto));
        await().pollInterval(Duration.ofSeconds(3))
                .atMost(10, SECONDS)
                .untilAsserted(() -> assertTrue(eventSentWithoutDatabaseEntry()));
    }

    private boolean eventSentWithoutDatabaseEntry() {
        return !orderRepository.findAll().iterator().hasNext()
                && testConsumer.messageConsumed();
    }

}