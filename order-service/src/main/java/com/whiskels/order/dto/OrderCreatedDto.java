package com.whiskels.order.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OrderCreatedDto {
    private UUID id;
    private UUID userId;
}
