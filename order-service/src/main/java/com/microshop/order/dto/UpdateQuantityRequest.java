package com.microshop.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateQuantityRequest(
        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be zero or positive")
        Integer quantity
) {}