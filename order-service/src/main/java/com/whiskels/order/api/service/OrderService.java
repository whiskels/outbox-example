package com.whiskels.order.api.service;

import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;

public interface OrderService {
    CreateOrderResponse create(CreateOrderRequest order);
}
