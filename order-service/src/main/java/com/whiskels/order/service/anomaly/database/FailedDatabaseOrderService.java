package com.whiskels.order.service.anomaly.database;

import com.whiskels.order.entity.Order;
import com.whiskels.order.SimulationStrategyEnum;
import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.mapper.OrderMapper;
import com.whiskels.order.service.SimulatedOrderService;
import com.whiskels.order.util.JsonUtil;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
class FailedDatabaseOrderService implements SimulatedOrderService {
    private final OrderMapper orderMapper;
    private final CrudRepository<Order, UUID> orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    public OrderCreatedDto create(final OrderDto order) {
        var orderEntity = orderMapper.toEntity(order);
        log.info("Saved order from user {} with id {}", order.getUserId(), orderEntity.getId());
        orderEntity = orderRepository.save(orderEntity);
        var dto = orderMapper.toDto(orderEntity);
        log.info("Preparing to send order with id to kafka {}", orderEntity.getId());
        kafkaTemplate.send("orders", JsonUtil.toJson(dto))
                .whenComplete(
                        (result, ex) -> {
                            if (ex != null) {
                                log.error("Failed to send order with id {} to kafka", dto.getId(), ex);
                            } else if (result != null) {
                                log.info("Sent order with id {} to kafka", dto.getId());
                            }
                        });
        throw new OptimisticLockException("Simulated database failure");
    }

    @Override
    public SimulationStrategyEnum strategy() {
        return SimulationStrategyEnum.FAILED_COMMIT_ANOMALY;
    }
}
