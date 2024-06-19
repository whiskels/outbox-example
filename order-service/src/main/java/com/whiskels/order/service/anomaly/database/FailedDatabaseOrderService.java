package com.whiskels.order.service.anomaly.database;

import com.whiskels.order.SimulationStrategyEnum;
import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import com.whiskels.order.entity.Order;
import com.whiskels.order.mapper.OrderMapper;
import com.whiskels.order.service.anomaly.AbstractOrderService;
import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
class FailedDatabaseOrderService extends AbstractOrderService {
    public FailedDatabaseOrderService(final OrderMapper orderMapper,
                                      final CrudRepository<Order, UUID> orderRepository,
                                      final KafkaTemplate<String, String> kafkaTemplate,
                                      @Value("${producer.topic}") final String topic) {
        super(orderMapper, orderRepository, kafkaTemplate, topic);
    }

    @Override
    @Transactional
    public OrderCreatedDto create(final OrderDto order) {
        super.create(order);
        throw new OptimisticLockException("Simulated database failure");
    }

    @Override
    public SimulationStrategyEnum strategy() {
        return SimulationStrategyEnum.FAILED_COMMIT_ANOMALY;
    }
}
