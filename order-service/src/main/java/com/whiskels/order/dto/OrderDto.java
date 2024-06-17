package com.whiskels.order.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class OrderDto {
    private UUID userId;

    private List<OrderItemDto> items = new ArrayList<>();

    @Data
    public static class OrderItemDto {
        private UUID productId;

        private int quantity;
    }
}
