package com.whiskels.order.demo;

import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import com.whiskels.order.api.service.BasicOrderService;
import com.whiskels.order.api.service.OrderService;
import com.whiskels.order.api.service.outbox.OutboxOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class RoutingOrderService implements OrderService {
    private final BasicOrderService basicOrderService;
    private final OutboxOrderService outboxOrderService;
    private final SimulationStrategyProvider simulationStrategyProvider;

    @Override
    @Transactional
    public CreateOrderResponse create(CreateOrderRequest order) {
        var strategy = simulationStrategyProvider.get();
        if (strategy == SimulationStrategyEnum.OUTBOX) {
            return outboxOrderService.create(order);
        }
        var response = basicOrderService.create(order);
        if (strategy == SimulationStrategyEnum.FAILED_COMMIT_ANOMALY) {
            throw new DemoAnomalyException("Simulated failure before commit");
        }
        return response;
    }
}
