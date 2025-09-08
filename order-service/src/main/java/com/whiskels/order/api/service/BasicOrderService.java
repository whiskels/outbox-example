package com.whiskels.order.api.service;

import com.whiskels.order.api.domain.Order;
import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import com.whiskels.order.api.mapper.OrderMapper;
import com.whiskels.order.util.JsonUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class BasicOrderService implements OrderService {
    private final OrderMapper orderMapper;
    private final JpaRepository<Order, UUID> orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public BasicOrderService(final OrderMapper orderMapper,
                             final JpaRepository<Order, UUID> orderRepository,
                             final KafkaTemplate<String, String> kafkaTemplate,
                             @Value("${producer.topic}") final String topic) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    @Transactional
    public CreateOrderResponse create(final CreateOrderRequest order) {
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
