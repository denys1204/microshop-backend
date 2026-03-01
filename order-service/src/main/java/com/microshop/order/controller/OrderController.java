package com.microshop.order.controller;

import com.microshop.order.dto.OrderRequest;
import com.microshop.order.dto.OrderResponse;
import com.microshop.order.dto.PaymentRequest;
import com.microshop.order.dto.UpdateQuantityRequest;
import com.microshop.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-Customer-Id") String customerId,
            @Valid @RequestBody OrderRequest request
    ) {
        OrderResponse response = service.createOrder(request, customerId);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{orderNumber}")
                .buildAndExpand(response.orderNumber())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse getOrder(@PathVariable String orderNumber) {
        return service.getOrderByNumber(orderNumber);
    }

    @PatchMapping("/{orderNumber}/items/{productId}")
    public void updateItemQuantity(
            @PathVariable String orderNumber,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateQuantityRequest request
    ) {
        service.updateItemQuantity(orderNumber, productId, request);
    }

    @DeleteMapping("/{orderNumber}/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable String orderNumber, @PathVariable Long productId) {
        service.removeItemFromOrder(orderNumber, productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderNumber}/place")
    public void placeOrder(@PathVariable String orderNumber) {
        service.placeOrder(orderNumber);
    }

    @PostMapping("/{orderNumber}/pay")
    public void payOrder(@PathVariable String orderNumber, @Valid @RequestBody PaymentRequest request) {
        service.payOrder(orderNumber, request.paymentMethod(), request.paymentId());
    }

    @DeleteMapping("/{orderNumber}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderNumber) {
        service.cancelOrder(orderNumber);
        return ResponseEntity.noContent().build();
    }
}