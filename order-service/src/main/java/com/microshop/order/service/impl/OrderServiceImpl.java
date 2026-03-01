package com.microshop.order.service.impl;

import com.microshop.order.dto.OrderRequest;
import com.microshop.order.dto.OrderResponse;
import com.microshop.order.dto.UpdateQuantityRequest;
import com.microshop.order.entity.Order;
import com.microshop.order.entity.OrderItem;
import com.microshop.order.entity.PaymentMethod;
import com.microshop.order.mapper.OrderMapper;
import com.microshop.order.repository.OrderRepository;
import com.microshop.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository repository;
    private final OrderMapper mapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request, String customerId) {
        String orderNumber = generateOrderNumber();
        Order order = mapper.toEntity(request, customerId, orderNumber);
        Order savedOrder = repository.save(order);

        log.info("Order created with number: {} for customer: {}", orderNumber, customerId);

        return mapper.toResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByNumber(String orderNumber) {
        Order order = getOrderEntityOrThrow(orderNumber);
        return mapper.toResponse(order);
    }

    @Override
    @Transactional
    public void updateItemQuantity(String orderNumber, Long productId, UpdateQuantityRequest request) {
        Order order = getOrderEntityOrThrow(orderNumber);
        order.updateItemQuantity(productId, request.quantity());

        log.info("Quantity updated for product {} in order {}. New quantity: {}, New total: {}", productId, orderNumber, request.quantity(), order.getTotalAmount());
    }

    @Override
    @Transactional
    public void removeItemFromOrder(String orderNumber, Long productId) {
        Order order = getOrderEntityOrThrow(orderNumber);

        OrderItem itemToRemove = order.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item with product ID " + productId + " not found in order"));

        order.removeOrderItem(itemToRemove);

        log.info("Product {} was removed from order {}. New total: {}", productId, orderNumber, order.getTotalAmount());
    }

    @Override
    @Transactional
    public void placeOrder(String orderNumber) {
        Order order = getOrderEntityOrThrow(orderNumber);
        order.place();

        log.info("Order {} was placed successfully", orderNumber);
    }

    @Override
    @Transactional
    public void payOrder(String orderNumber, PaymentMethod paymentMethod, String paymentId) {
        Order order = getOrderEntityOrThrow(orderNumber);
        order.assignPayment(paymentMethod, paymentId);
        order.pay();

        log.info("Order {} was marked as paid. PaymentMethod: {}, PaymentID: {}", orderNumber, paymentMethod, paymentId);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNumber) {
        Order order = getOrderEntityOrThrow(orderNumber);
        order.cancel();
        log.info("Order {} was cancelled", orderNumber);
    }

    private Order getOrderEntityOrThrow(String orderNumber) {
        return repository.findByOrderNumber(orderNumber).orElseThrow(
                () -> new IllegalArgumentException("Order not found: " + orderNumber)
        );
    }

    private String generateOrderNumber() {
        return UUID.randomUUID().toString();
    }
}