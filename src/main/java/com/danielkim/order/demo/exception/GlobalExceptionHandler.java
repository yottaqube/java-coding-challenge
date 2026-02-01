package com.danielkim.order.demo.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for consistent error responses.
 *
 * <p>Provides centralized error handling for: - Business exceptions (OrderNotFoundException,
 * InvalidOrderStatusTransitionException) - Validation errors (MethodArgumentNotValidException) -
 * General runtime exceptions
 *
 * <p>All errors are logged and return consistent JSON error responses with: - Timestamp - HTTP
 * status - Error message - Request path (when available) - Validation details (for validation
 * errors)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** Handles order not found exceptions. */
  @ExceptionHandler(OrderNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleOrderNotFoundException(OrderNotFoundException e) {
    logger.error("Order not found: {}", e.getMessage());

    ErrorResponse errorResponse =
        new ErrorResponse(
            LocalDateTime.now(), HttpStatus.NOT_FOUND.value(), "Order Not Found", e.getMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
  }

  /** Handles invalid order status transition exceptions. */
  @ExceptionHandler(InvalidOrderStatusTransitionException.class)
  public ResponseEntity<ErrorResponse> handleInvalidOrderStatusTransitionException(
      InvalidOrderStatusTransitionException e) {
    logger.error("Invalid order status transition: {}", e.getMessage());

    ErrorResponse errorResponse =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Status Transition",
            e.getMessage());

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /** Handles validation errors from request body validation. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {
    logger.error("Validation error: {}", e.getMessage());

    Map<String, String> fieldErrors = new HashMap<>();
    e.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              fieldErrors.put(fieldName, errorMessage);
            });

    ValidationErrorResponse errorResponse =
        new ValidationErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "Request validation failed",
            fieldErrors);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  /** Handles general runtime exceptions. */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
    logger.error("Unexpected error: ", e);

    ErrorResponse errorResponse =
        new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred");

    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** Standard error response structure. */
  public static class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;

    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
      this.timestamp = timestamp;
      this.status = status;
      this.error = error;
      this.message = message;
    }

    // Getters
    public LocalDateTime getTimestamp() {
      return timestamp;
    }

    public int getStatus() {
      return status;
    }

    public String getError() {
      return error;
    }

    public String getMessage() {
      return message;
    }
  }

  /** Validation error response with field-level details. */
  public static class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> fieldErrors;

    public ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors) {
      super(timestamp, status, error, message);
      this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
      return fieldErrors;
    }
  }
}
