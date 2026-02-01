package com.danielkim.order.demo.exception;

/** Exception thrown when an order is not found. */
public class OrderNotFoundException extends RuntimeException {

  public OrderNotFoundException(String message) {
    super(message);
  }

  public OrderNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
