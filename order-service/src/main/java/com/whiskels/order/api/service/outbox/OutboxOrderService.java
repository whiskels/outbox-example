package com.whiskels.order.api.service.outbox;

import com.whiskels.order.api.domain.Order;
import com.whiskels.order.api.domain.OutboxEvent;
import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import com.whiskels.order.api.mapper.OrderMapper;
import com.whiskels.order.api.service.OrderService;
import com.whiskels.order.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class OutboxOrderService implements OrderService {
    private final OrderMapper orderMapper;
    private final CrudRepository<Order, UUID> orderRepository;
    private final CrudRepository<OutboxEvent, UUID> outboxRepository;
    private final String topic;

    public OutboxOrderService(final OrderMapper orderMapper,
                              final CrudRepository<Order, UUID> orderRepository,
                              final CrudRepository<OutboxEvent, UUID> outboxRepository,
                              @Value("${producer.topic}") final String topic) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.topic = topic;
    }

    @Override
    @Transactional
    public CreateOrderResponse create(final CreateOrderRequest order) {
        var orderEntity = orderRepository.save(orderMapper.toEntity(order));
        log.info("Saved order from user {} with id {}", order.getUserId(), orderEntity.getId());
        var dto = orderMapper.toDto(orderEntity);
        var outboxEvent = outboxRepository.save(OutboxEvent.of(topic, JsonUtil.toJson(dto)));
        log.info("Saved order with id {} to outbox {}", dto.getId(), outboxEvent.getId());
        return dto;
    }
}
