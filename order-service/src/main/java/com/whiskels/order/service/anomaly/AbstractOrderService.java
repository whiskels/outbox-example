package com.whiskels.order.service.anomaly;

import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.entity.Order;
import com.whiskels.order.mapper.OrderMapper;
import com.whiskels.order.service.SimulatedOrderService;
import com.whiskels.order.util.JsonUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

@Slf4j
public abstract class AbstractOrderService implements SimulatedOrderService {
    private final OrderMapper orderMapper;
    private final CrudRepository<Order, UUID> orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public AbstractOrderService(final OrderMapper orderMapper,
                                final CrudRepository<Order, UUID> orderRepository,
                                final KafkaTemplate<String, String> kafkaTemplate,
                                final String topic) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    @Transactional
    public OrderCreatedDto create(final OrderDto order) {
        var orderEntity = orderRepository.save(orderMapper.toEntity(order));
        log.info("Saved order from user {} with id {}", order.getUserId(), orderEntity.getId());
        var dto = orderMapper.toDto(orderEntity);
        log.info("Preparing to send order with id to kafka {}", orderEntity.getId());
        kafkaTemplate.send(topic, JsonUtil.toJson(dto))
                .whenComplete(
                        (result, ex) -> {
                            if (result != null && ex == null) {
                                log.info("Sent order with id {} to kafka", dto.getId());
                            } else {
                                log.error("Failed to send order with id {} to kafka", dto.getId(), ex);
                            }
                        });
        return dto;
    }
}
