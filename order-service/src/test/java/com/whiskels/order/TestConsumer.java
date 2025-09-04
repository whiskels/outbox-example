package com.whiskels.order;

import com.whiskels.order.api.dto.CreateOrderResponse;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TestConsumer {
    private final Set<UUID> usersWithExisingOrders = new HashSet<>();

    @KafkaListener(topics = "${producer.topic}")
    public void listen(CreateOrderResponse orderEvent) {
        usersWithExisingOrders.add(orderEvent.getUserId());
    }

    public boolean hasMessageForUser(UUID userId) {
        return usersWithExisingOrders.contains(userId);
    }
}
