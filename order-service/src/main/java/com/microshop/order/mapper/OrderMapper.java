package com.microshop.order.mapper;

import com.microshop.order.dto.OrderItemRequest;
import com.microshop.order.dto.OrderRequest;
import com.microshop.order.dto.OrderResponse;
import com.microshop.order.entity.Order;
import com.microshop.order.entity.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "paymentId", ignore = true)
    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "orderNumber", source = "orderNumber")
    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderRequest request, String customerId, String orderNumber);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderItem toOrderItemEntity(OrderItemRequest request);

    OrderResponse toResponse(Order order);

    @AfterMapping
    default void linkOrderItems(OrderRequest request, @MappingTarget Order order) {
        if (request.items() != null) {
            request.items().forEach(itemRequest -> order.addOrderItem(toOrderItemEntity(itemRequest)));
        }
    }
}