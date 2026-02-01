package com.danielkim.order.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Data Transfer Object for creating new orders. Contains validation annotations to ensure data
 * integrity.
 */
public class CreateOrderRequest {

  @NotBlank(message = "Customer name is required")
  private String customerName;

  @NotBlank(message = "Product name is required")
  private String productName;

  @NotNull(message = "Quantity is required")
  @Positive(message = "Quantity must be positive")
  private Integer quantity;

  @NotNull(message = "Price is required")
  @Positive(message = "Price must be positive")
  private BigDecimal price;

  @Email(message = "Customer email must be valid")
  private String customerEmail;

  private String customerPhone;

  // Constructors
  public CreateOrderRequest() {}

  public CreateOrderRequest(
      String customerName, String productName, Integer quantity, BigDecimal price) {
    this.customerName = customerName;
    this.productName = productName;
    this.quantity = quantity;
    this.price = price;
  }

  // Getters and Setters
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
}
