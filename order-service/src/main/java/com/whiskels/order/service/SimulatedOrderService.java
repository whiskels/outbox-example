package com.whiskels.order.service;

import com.whiskels.order.SimulationStrategyEnum;
import com.whiskels.order.dto.OrderCreatedDto;
import com.whiskels.order.dto.OrderDto;

public interface SimulatedOrderService {
    OrderCreatedDto create(OrderDto order);

    SimulationStrategyEnum strategy();
}
