package com.danielkim.order.demo.service;

import com.danielkim.order.demo.dto.*;
import com.danielkim.order.demo.exception.InvalidOrderStatusTransitionException;
import com.danielkim.order.demo.exception.OrderNotFoundException;
import com.danielkim.order.demo.model.Order;
import com.danielkim.order.demo.model.OrderStatus;
import com.danielkim.order.demo.repository.OrderRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for order management operations. Handles business logic, validation, and
 * coordinates with notification service.
 *
 * <p>Key responsibilities: - Order CRUD operations with validation - Status transition management
 * with business rules - Integration with notification system - Transaction management
 */
@Service
@Transactional
public class OrderService {

  private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

  private final OrderRepository orderRepository;
  private final NotificationService notificationService;

  public OrderService(OrderRepository orderRepository, NotificationService notificationService) {
    this.orderRepository = orderRepository;
    this.notificationService = notificationService;
  }

  /** Creates a new order and sends creation notification. */
  public OrderResponse createOrder(CreateOrderRequest request) {
    logger.info("Creating new order for customer: {}", request.getCustomerName());

    Order order = new Order();
    order.setCustomerName(request.getCustomerName());
    order.setProductName(request.getProductName());
    order.setQuantity(request.getQuantity());
    order.setPrice(request.getPrice());
    order.setCustomerEmail(request.getCustomerEmail());
    order.setCustomerPhone(request.getCustomerPhone());
    order.setStatus(OrderStatus.CREATED);

    Order savedOrder = orderRepository.save(order);
    logger.info("Order created successfully with ID: {}", savedOrder.getId());

    // Send creation notification asynchronously
    notificationService.sendOrderCreatedNotification(savedOrder);

    return mapToOrderResponse(savedOrder);
  }

  /** Retrieves an order by ID. */
  @Transactional(readOnly = true)
  public OrderResponse getOrder(Long id) {
    logger.debug("Retrieving order with ID: {}", id);

    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

    return mapToOrderResponse(order);
  }

  /** Updates order status with validation and sends notification. */
  public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
    logger.info("Updating order status for ID: {} to {}", id, request.getStatus());

    Order order =
        orderRepository
            .findById(id)
            .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

    OrderStatus oldStatus = order.getStatus();

    // Validate status transition
    if (!order.canTransitionTo(request.getStatus())) {
      throw new InvalidOrderStatusTransitionException(
          String.format("Cannot transition from %s to %s", oldStatus, request.getStatus()));
    }

    order.setStatus(request.getStatus());
    order.setUpdatedAt(LocalDateTime.now());

    Order savedOrder = orderRepository.save(order);
    logger.info("Order status updated successfully from {} to {}", oldStatus, request.getStatus());

    // Send status change notification asynchronously
    notificationService.sendOrderStatusChangedNotification(savedOrder, oldStatus);

    return mapToOrderResponse(savedOrder);
  }

  /** Searches orders with pagination and filtering. */
  @Transactional(readOnly = true)
  public OrderSearchResponse searchOrders(
      String customerName,
      String productName,
      OrderStatus status,
      String email,
      Pageable pageable) {
    logger.debug(
        "Searching orders with criteria - customerName: {}, productName: {}, status: {}, email: {}",
        customerName,
        productName,
        status,
        email);

    Page<Order> orderPage =
        orderRepository.findOrdersByCriteria(customerName, productName, status, email, pageable);

    var orderResponses = orderPage.getContent().stream().map(this::mapToOrderResponse).toList();

    return new OrderSearchResponse(
        orderResponses,
        orderPage.getTotalElements(),
        orderPage.getTotalPages(),
        orderPage.getNumber(),
        orderPage.getSize());
  }

  /** Maps Order entity to OrderResponse DTO. */
  private OrderResponse mapToOrderResponse(Order order) {
    OrderResponse response = new OrderResponse();
    response.setId(order.getId());
    response.setCustomerName(order.getCustomerName());
    response.setProductName(order.getProductName());
    response.setQuantity(order.getQuantity());
    response.setPrice(order.getPrice());
    response.setTotalValue(order.getTotalValue());
    response.setStatus(order.getStatus());
    response.setCreatedAt(order.getCreatedAt());
    response.setUpdatedAt(order.getUpdatedAt());
    response.setCustomerEmail(order.getCustomerEmail());
    response.setCustomerPhone(order.getCustomerPhone());
    return response;
  }

  /**
   * Compare JPQL vs Native SQL query performance.
   *
   * <p>Demonstrates the difference between: - JPQL: Entity-based, database-independent - Native
   * SQL: Table-based, database-specific, potentially faster
   */
  public QueryComparisonResult compareQueryMethods(
      String customerName,
      String productName,
      OrderStatus status,
      String email,
      Pageable pageable) {

    long jpqlStartTime = System.nanoTime();
    Page<Order> jpqlResults =
        orderRepository.findOrdersByCriteria(customerName, productName, status, email, pageable);
    long jpqlDuration = System.nanoTime() - jpqlStartTime;

    long nativeStartTime = System.nanoTime();
    String statusString = status != null ? status.name() : null;
    Page<Order> nativeResults =
        orderRepository.findOrdersByCriteriaNative(
            customerName, productName, statusString, email, pageable);
    long nativeDuration = System.nanoTime() - nativeStartTime;

    return new QueryComparisonResult(
        jpqlDuration / 1_000_000.0, // Convert to milliseconds
        nativeDuration / 1_000_000.0,
        jpqlResults.getTotalElements(),
        nativeResults.getTotalElements());
  }

  /** Query comparison result holder */
  public static class QueryComparisonResult {
    private final double jpqlTimeMs;
    private final double nativeTimeMs;
    private final long jpqlResultCount;
    private final long nativeResultCount;

    public QueryComparisonResult(
        double jpqlTime, double nativeTime, long jpqlCount, long nativeCount) {
      this.jpqlTimeMs = jpqlTime;
      this.nativeTimeMs = nativeTime;
      this.jpqlResultCount = jpqlCount;
      this.nativeResultCount = nativeCount;
    }

    public double getPerformanceRatio() {
      return jpqlTimeMs / nativeTimeMs;
    }

    @Override
    public String toString() {
      return String.format(
          "Query Performance Comparison:\n"
              + "JPQL Query: %.2f ms (%d results)\n"
              + "Native SQL Query: %.2f ms (%d results)\n"
              + "Performance Ratio: %.1fx (Native SQL is %.1fx %s)",
          jpqlTimeMs,
          jpqlResultCount,
          nativeTimeMs,
          nativeResultCount,
          getPerformanceRatio(),
          Math.abs(getPerformanceRatio() - 1) + 1,
          getPerformanceRatio() > 1 ? "faster" : "slower");
    }
  }
}
