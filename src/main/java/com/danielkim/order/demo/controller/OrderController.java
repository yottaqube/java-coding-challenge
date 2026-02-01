package com.danielkim.order.demo.controller;

import com.danielkim.order.demo.dto.*;
import com.danielkim.order.demo.model.OrderStatus;
import com.danielkim.order.demo.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Order Management API.
 *
 * <p>Provides endpoints for: - POST /api/orders - Create new order - GET /api/orders/{id} -
 * Retrieve order details - PUT /api/orders/{id}/status - Update order status - GET
 * /api/orders/search - Search orders with pagination and filtering
 *
 * <p>All endpoints are secured and require authentication. Comprehensive logging and error handling
 * is implemented.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

  private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  /**
   * Creates a new order.
   *
   * @param request Order creation request with validation
   * @return Created order details with HTTP 201 status
   */
  @PostMapping
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    logger.info("Received create order request for customer: {}", request.getCustomerName());

    OrderResponse response = orderService.createOrder(request);

    logger.info("Order created successfully with ID: {}", response.getId());
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  /**
   * Retrieves order details by ID.
   *
   * @param id Order ID
   * @return Order details with HTTP 200 status
   */
  @GetMapping("/{id}")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
    logger.debug("Received get order request for ID: {}", id);

    OrderResponse response = orderService.getOrder(id);

    logger.debug("Order retrieved successfully for ID: {}", id);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates order status.
   *
   * @param id Order ID
   * @param request Status update request with validation
   * @return Updated order details with HTTP 200 status
   */
  @PutMapping("/{id}/status")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
    logger.info(
        "Received update order status request for ID: {} to status: {}", id, request.getStatus());

    OrderResponse response = orderService.updateOrderStatus(id, request);

    logger.info("Order status updated successfully for ID: {}", id);
    return ResponseEntity.ok(response);
  }

  /**
   * Searches orders with pagination and filtering.
   *
   * @param customerName Customer name filter (optional)
   * @param productName Product name filter (optional)
   * @param status Order status filter (optional)
   * @param email Customer email filter (optional)
   * @param page Page number (default: 0)
   * @param size Page size (default: 10, max: 100)
   * @param sortBy Sort field (default: createdAt)
   * @param sortDir Sort direction (default: desc)
   * @return Paginated search results with HTTP 200 status
   */
  @GetMapping("/search")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<OrderSearchResponse> searchOrders(
      @RequestParam(required = false) String customerName,
      @RequestParam(required = false) String productName,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) String email,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortDir) {

    logger.debug(
        "Received search orders request with filters - customerName: {}, productName: {}, status: {}, email: {}",
        customerName,
        productName,
        status,
        email);

    // Validate and limit page size
    size = Math.min(size, 100);

    // Create sort object
    Sort.Direction direction =
        sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    OrderSearchResponse response =
        orderService.searchOrders(customerName, productName, status, email, pageable);

    logger.debug("Search completed successfully. Found {} orders", response.getTotalElements());
    return ResponseEntity.ok(response);
  }
}
