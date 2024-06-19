package com.whiskels.order.service.anomaly.broker;

import com.whiskels.order.SimulationStrategyEnum;
import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.entity.Order;
import com.whiskels.order.mapper.OrderMapper;
import com.whiskels.order.service.SimulatedOrderService;
import com.whiskels.order.util.JsonUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
class FailedBrokerService implements SimulatedOrderService {
    private final OrderMapper orderMapper;
    private final CrudRepository<Order, UUID> orderRepository;
    private final CorruptedKafkaTemplate kafkaTemplate;
    private final String topic;

    public FailedBrokerService(final OrderMapper orderMapper,
                               final CrudRepository<Order, UUID> orderRepository,
                               final CorruptedKafkaTemplate kafkaTemplate,
                               @Value("${producer.topic}") final String topic) {
        this.orderMapper = orderMapper;
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    @Transactional
    public OrderCreatedDto create(final OrderDto order) {
        var orderEntity = orderMapper.toEntity(order);
        log.info("Saved order from user {} with id {}", order.getUserId(), orderEntity.getId());
        orderEntity = orderRepository.save(orderEntity);
        var dto = orderMapper.toDto(orderEntity);
        log.info("Preparing to send order with id to kafka {}", orderEntity.getId());
        kafkaTemplate.send(topic, JsonUtil.toJson(dto))
                .whenComplete(
                        (result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send order with id {} to kafka", dto.getId(), ex);
                            } else if (result != null) {
                                log.info("Sent order with id {} to kafka", dto.getId());
                            }
                        });
        return dto;
    }

    @Override
    public SimulationStrategyEnum strategy() {
        return SimulationStrategyEnum.FAILED_BROKER_ANOMALY;
    }
}
