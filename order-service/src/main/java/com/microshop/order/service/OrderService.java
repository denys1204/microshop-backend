package com.microshop.order.service;

import com.microshop.order.dto.OrderRequest;
import com.microshop.order.dto.OrderResponse;
import com.microshop.order.dto.UpdateQuantityRequest;
import com.microshop.order.entity.PaymentMethod;

public interface OrderService {
    OrderResponse createOrder(OrderRequest request, String customerId);
    OrderResponse getOrderByNumber(String orderNumber);
    void updateItemQuantity(String orderNumber, Long productId, UpdateQuantityRequest request);
    void removeItemFromOrder(String orderNumber, Long productId);
    void placeOrder(String orderNumber);
    void payOrder(String orderNumber, PaymentMethod paymentMethod, String paymentId);
    void cancelOrder(String orderNumber);
}
