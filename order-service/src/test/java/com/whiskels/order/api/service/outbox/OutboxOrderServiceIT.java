package com.whiskels.order.api.service.outbox;

import com.whiskels.order.BaseIT;
import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxOrderServiceIT extends BaseIT {
    @Autowired
    private OutboxOrderService outboxOrderService;

    @Test
    void shouldCreateOrderAndOutboxEntry() {
        CreateOrderRequest order = new CreateOrderRequest(UUID.randomUUID(), List.of());

        // when
        CreateOrderResponse createOrderResponse = outboxOrderService.create(order);

        assertEquals(order.getUserId(), createOrderResponse.getUserId());
        assertTrue(outboxEventRepository.findAll().iterator().hasNext());
        assertTrue(orderRepository.findAll().iterator().hasNext());
    }
}