package com.microshop.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NaturalId
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "payment_id")
    private String paymentId;

    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.CREATED;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "PLN";

    @Builder.Default
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Version
    private Long version;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addOrderItem(OrderItem item) {
        if (item == null || item.getPrice() == null || item.getQuantity() == null) {
            throw new IllegalArgumentException("Item, price, and quantity must not be null");
        }
        if (item.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Item price cannot be negative");
        }
        if (item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Item quantity must be positive");
        }

        item.setOrder(this);
        orderItems.add(item);
        recalculateTotal();
    }

    public void updateItemQuantity(Long productId, Integer newQuantity) {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order items can only be updated in CREATED status");
        }

        OrderItem item = orderItems.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item with product ID " + productId + " not found in order"));

        if (newQuantity <= 0) {
            removeOrderItem(item);
        } else {
            item.setQuantity(newQuantity);
            recalculateTotal();
        }
    }

    public void removeOrderItem(OrderItem item) {
        if (orderItems.size() <= 1 && orderItems.contains(item)) {
            throw new IllegalStateException("Order must have at least one item");
        }

        if (orderItems.remove(item)) {
            if (item.getOrder() == this) {
                item.setOrder(null);
            }
            recalculateTotal();
        }
    }

    public void assignPayment(PaymentMethod method, String paymentId) {
        if (this.status != OrderStatus.PLACED) {
            throw new IllegalStateException("Payment can only be assigned to a PLACED order");
        }

        this.paymentMethod = method;
        this.paymentId = paymentId;
    }

    public void place() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order can only be placed from CREATED status");
        }
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Order must contain at least one item");
        }

        this.status = OrderStatus.PLACED;
    }

    public void pay() {
        if (status != OrderStatus.PLACED) {
            throw new IllegalStateException("Order must be PLACED to be paid");
        }
        if (paymentMethod == null) {
            throw new IllegalStateException("Payment method must be selected before paying");
        }
        if (paymentId == null || paymentId.isBlank()) {
            throw new IllegalStateException("Payment must be authorized (paymentId is missing)");
        }

        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order cannot be cancelled in status: " + status);
        }

        this.status = OrderStatus.CANCELLED;
    }

    private void recalculateTotal() {
        this.totalAmount = orderItems.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}