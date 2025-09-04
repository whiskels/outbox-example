package com.whiskels.order.api.mapper;

import com.whiskels.order.api.domain.Order;
import com.whiskels.order.api.dto.CreateOrderRequest;
import com.whiskels.order.api.dto.CreateOrderResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface OrderMapper {
    Order toEntity(CreateOrderRequest createOrderRequest);

    @AfterMapping
    default void syncRelationships(@MappingTarget Order order) {
        order.getItems().forEach(orderItem -> orderItem.setOrder(order));
    }

    CreateOrderResponse toDto(Order order);
}
