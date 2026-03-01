package com.microshop.order.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "Order must contain at least one item")
        List<OrderItemRequest> items
) {}