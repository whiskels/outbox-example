package com.whiskels.order.controller;

import com.whiskels.order.service.SimulatedOrderService;
import com.whiskels.order.SimulationStrategyEnum;
import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@RestController
class OrderController {
    private final Map<SimulationStrategyEnum, SimulatedOrderService> services;

    public OrderController(final List<SimulatedOrderService> services) {
        this.services = services.stream().collect(toMap(SimulatedOrderService::strategy, Function.identity()));
    }

    @PostMapping("/orders")
    public OrderCreatedDto createOrder(
            @RequestBody OrderDto order,
            @RequestParam(required = false, defaultValue = "OUTBOX") SimulationStrategyEnum simulationStrategy
    ) {
        return services.get(simulationStrategy)
                .create(order);
    }
}
