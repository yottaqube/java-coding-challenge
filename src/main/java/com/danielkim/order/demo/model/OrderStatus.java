package com.danielkim.order.demo.model;

/**
 * Order status enum defining the controlled lifecycle of orders.
 *
 * <p>State transitions: - Initial state: CREATED - Terminal states: CANCELLED, COMPLETED
 *
 * <p>Business Rules: - Orders can only transition from CREATED to CANCELLED or COMPLETED - Terminal
 * states cannot be changed
 */
public enum OrderStatus {
  /** Initial state when an order is first created */
  CREATED,

  /** Order has been cancelled - terminal state */
  CANCELLED,

  /** Order has been completed successfully - terminal state */
  COMPLETED
}
