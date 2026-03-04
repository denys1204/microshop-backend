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
import java.util.Optional;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
        requireStatus(OrderStatus.CREATED, "Items can only be added to an order in CREATED status");
        validateNewItem(item);

        Optional<OrderItem> existingItem = orderItems.stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem found = existingItem.get();
            found.setQuantity(found.getQuantity() + item.getQuantity());
        } else {
            item.setOrder(this);
            orderItems.add(item);
        }

        recalculateTotal();
    }

    public void updateItemQuantity(Long productId, Integer newQuantity) {
        requireStatus(OrderStatus.CREATED, "Order items can only be updated in CREATED status");
        if (newQuantity == null || newQuantity < 0) {
            throw new IllegalArgumentException("Quantity must be positive or zero");
        }
        if (newQuantity == 0) {
            removeProduct(productId);
            return;
        }

        OrderItem item = findItemByProductIdOrThrow(productId);
        item.setQuantity(newQuantity);
        recalculateTotal();
    }

    public void removeProduct(Long productId) {
        requireStatus(OrderStatus.CREATED, "Items can only be removed from an order in CREATED status");

        OrderItem itemToRemove = findItemByProductIdOrThrow(productId);

        if (orderItems.remove(itemToRemove)) {
            itemToRemove.setOrder(null);
            recalculateTotal();
        }
    }

    public void assignPayment(PaymentMethod method, String paymentId) {
        requireStatus(OrderStatus.PLACED, "Payment can only be assigned to a PLACED order");

        this.paymentMethod = method;
        this.paymentId = paymentId;
    }

    public void place() {
        requireStatus(OrderStatus.CREATED, "Order can only be placed from CREATED status");
        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Order must contain at least one item");
        }

        this.status = OrderStatus.PLACED;
    }

    public void pay() {
        requireStatus(OrderStatus.PLACED, "Order must be PLACED to be paid");
        if (paymentMethod == null || paymentId == null || paymentId.isBlank()) {
            throw new IllegalStateException("Payment details must be fully provided before paying");
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

    private OrderItem findItemByProductIdOrThrow(Long productId) {
        return orderItems.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item with product ID " + productId + " not found in order"));
    }

    private void requireStatus(OrderStatus expectedStatus, String message) {
        if (this.status != expectedStatus) {
            throw new IllegalStateException(message);
        }
    }

    private void validateNewItem(OrderItem item) {
        if (item == null || item.getProductId() == null || item.getPrice() == null || item.getQuantity() == null) {
            throw new IllegalArgumentException("Item, productId, price, and quantity must not be null");
        }
        if (item.getPrice().signum() < 0 || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Price cannot be negative and quantity must be positive");
        }
    }
}