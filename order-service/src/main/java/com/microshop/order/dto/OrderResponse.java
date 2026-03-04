package com.microshop.order.dto;

import com.microshop.order.entity.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse(
        String orderNumber,
        BigDecimal totalAmount,
        OrderStatus status
) {}