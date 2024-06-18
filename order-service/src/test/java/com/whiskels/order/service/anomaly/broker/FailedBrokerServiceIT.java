package com.whiskels.order.service.anomaly.broker;

import com.whiskels.order.TestConsumer;
import com.whiskels.order.TestcontainersIT;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.entity.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class FailedBrokerServiceIT extends TestcontainersIT {

    @Autowired
    private CrudRepository<Order, UUID> orderRepository;

    @Autowired
    private FailedBrokerService service;

    @Autowired
    private TestConsumer testConsumer;

    @Test
    void testSend() {
        var dto = new OrderDto();
        dto.setUserId(UUID.randomUUID());
        var result = service.create(dto);

        assertTrue(orderSavedButNotSent(result.getId())); //naive test
    }

    private boolean orderSavedButNotSent(UUID id) {
        return orderRepository.existsById(id) && !testConsumer.messageConsumed();
    }
}