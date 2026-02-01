package com.danielkim.order.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order entity representing an order in the system.
 *
 * <p>Follows a controlled lifecycle: CREATED -> CANCELLED/COMPLETED All status changes trigger
 * notifications to external systems.
 */
@Entity
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Customer name is required")
  @Column(name = "customer_name", nullable = false)
  private String customerName;

  @NotBlank(message = "Product name is required")
  @Column(name = "product_name", nullable = false)
  private String productName;

  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be positive")
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @NotNull(message = "Price is required")
  @Positive(message = "Price must be positive")
  @Column(name = "price", nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status = OrderStatus.CREATED;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "customer_email")
  private String customerEmail;

  @Column(name = "customer_phone")
  private String customerPhone;

  // Constructors
  public Order() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public Order(String customerName, String productName, Integer quantity, BigDecimal price) {
    this();
    this.customerName = customerName;
    this.productName = productName;
    this.quantity = quantity;
    this.price = price;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getProductName() {
    return productName;
  }

  public void setProductName(String productName) {
    this.productName = productName;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
    this.updatedAt = LocalDateTime.now();
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public String getCustomerEmail() {
    return customerEmail;
  }

  public void setCustomerEmail(String customerEmail) {
    this.customerEmail = customerEmail;
  }

  public String getCustomerPhone() {
    return customerPhone;
  }

  public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
  }

  /** Calculates the total value of the order. */
  public BigDecimal getTotalValue() {
    return price.multiply(BigDecimal.valueOf(quantity));
  }

  /** Validates if status transition is allowed. */
  public boolean canTransitionTo(OrderStatus newStatus) {
    return switch (this.status) {
      case CREATED -> newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.COMPLETED;
      case CANCELLED, COMPLETED -> false;
    };
  }
}
