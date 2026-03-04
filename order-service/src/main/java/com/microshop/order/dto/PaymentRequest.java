package com.microshop.order.dto;

import com.microshop.order.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotBlank(message = "Payment ID is required")
        String paymentId,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {}