package com.whiskels.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateOrderRequest {
    private UUID userId;

    private List<OrderItemDto> items;

    @Data
    @AllArgsConstructor
    public static class OrderItemDto {
        private UUID productId;

        private int quantity;
    }
}
