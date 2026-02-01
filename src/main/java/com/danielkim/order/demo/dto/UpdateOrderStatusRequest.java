package com.danielkim.order.demo.dto;

import com.danielkim.order.demo.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for updating order status. Ensures only valid status transitions are
 * requested.
 */
public class UpdateOrderStatusRequest {

  @NotNull(message = "Status is required")
  private OrderStatus status;

  // Constructors
  public UpdateOrderStatusRequest() {}

  public UpdateOrderStatusRequest(OrderStatus status) {
    this.status = status;
  }

  // Getters and Setters
  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }
}
