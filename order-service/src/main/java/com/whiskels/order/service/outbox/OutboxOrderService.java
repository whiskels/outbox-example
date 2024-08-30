package com.whiskels.order.service.outbox;

import com.whiskels.order.SimulationStrategyEnum;
import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.entity.Order;
import com.whiskels.order.entity.OutboxEvent;
import com.whiskels.order.mapper.OrderMapper;
import com.whiskels.order.service.SimulatedOrderService;
import com.whiskels.order.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
class OutboxOrderService implements SimulatedOrderService {
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
    public OrderCreatedDto create(final OrderDto order) {
        var orderEntity = orderRepository.save(orderMapper.toEntity(order));
        log.info("Saved order from user {} with id {}", order.getUserId(), orderEntity.getId());
        var dto = orderMapper.toDto(orderEntity);
        var outboxEvent = outboxRepository.save(OutboxEvent.of(topic, JsonUtil.toJson(dto)));
        log.info("Saved order with id {} to outbox {}", dto.getId(), outboxEvent.getId());
        return dto;
    }

    @Override
    public SimulationStrategyEnum strategy() {
        return SimulationStrategyEnum.OUTBOX;
    }
}
