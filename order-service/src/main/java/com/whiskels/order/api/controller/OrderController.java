package com.whiskels.order.api.controller;

import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import com.whiskels.order.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest order) {
        return orderService.create(order);
    }
}
