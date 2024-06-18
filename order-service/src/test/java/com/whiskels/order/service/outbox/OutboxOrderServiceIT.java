package com.whiskels.order.service.outbox;

import com.whiskels.order.TestcontainersIT;
import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.entity.Order;
import com.whiskels.order.entity.OutboxEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class OutboxOrderServiceIT extends TestcontainersIT {
    @Autowired
    private CrudRepository<Order, UUID> orderRepository;

    @Autowired
    private CrudRepository<OutboxEvent, UUID> outboxEventRepository;

    @Autowired
    private OutboxOrderService outboxOrderService;

    @BeforeEach
    @AfterEach
    void cleanUp() {
        outboxEventRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void testCreate() {
        OrderDto order = new OrderDto();
        order.setUserId(UUID.randomUUID());

        // when
        OrderCreatedDto orderCreatedDto = outboxOrderService.create(order);

        assertEquals(order.getUserId(), orderCreatedDto.getUserId());
        assertTrue(outboxEventRepository.findAll().iterator().hasNext());
        assertTrue(orderRepository.findAll().iterator().hasNext());
    }
}