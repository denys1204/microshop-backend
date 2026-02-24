package com.microshop.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 255, message = "Name length must be less than 255 characters long")
        String name,

        @Size(max = 255, message = "Description length must be less than 255 characters long")
        String description,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be strictly positive")
        BigDecimal price,

        @NotBlank(message = "SKU is required")
        @Size(max = 255, message = "SKU length must be less than 255 characters long")
        String sku
) {}