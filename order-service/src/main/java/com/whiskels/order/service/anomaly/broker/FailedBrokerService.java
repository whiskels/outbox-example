package com.whiskels.order.service.anomaly.broker;

import com.whiskels.order.SimulationStrategyEnum;
import com.whiskels.order.entity.Order;
import com.whiskels.order.mapper.OrderMapper;
import com.whiskels.order.service.anomaly.AbstractOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
class FailedBrokerService extends AbstractOrderService {
    @Autowired
    public FailedBrokerService(final OrderMapper orderMapper,
                               final CrudRepository<Order, UUID> orderRepository,
                               final CorruptedKafkaTemplate kafkaTemplate,
                               @Value("${producer.topic}") final String topic) {
        super(orderMapper, orderRepository, kafkaTemplate, topic);
    }

    @Override
    public SimulationStrategyEnum strategy() {
        return SimulationStrategyEnum.FAILED_BROKER_ANOMALY;
    }
}
