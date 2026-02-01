package com.danielkim.order.demo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.danielkim.order.demo.dto.CreateOrderRequest;
import com.danielkim.order.demo.dto.OrderResponse;
import com.danielkim.order.demo.dto.UpdateOrderStatusRequest;
import com.danielkim.order.demo.exception.InvalidOrderStatusTransitionException;
import com.danielkim.order.demo.exception.OrderNotFoundException;
import com.danielkim.order.demo.model.Order;
import com.danielkim.order.demo.model.OrderStatus;
import com.danielkim.order.demo.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for OrderService.
 *
 * <p>Tests cover: - Order creation and validation - Order retrieval - Status transitions and
 * business rules - Search functionality - Exception handling - Notification integration
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock private OrderRepository orderRepository;

  @Mock private NotificationService notificationService;

  @InjectMocks private OrderService orderService;

  @Test
  void createOrder_ValidRequest_ReturnsOrderResponse() {
    // Given
    CreateOrderRequest request = new CreateOrderRequest();
    request.setCustomerName("John Doe");
    request.setProductName("Laptop");
    request.setQuantity(2);
    request.setPrice(BigDecimal.valueOf(999.99));
    request.setCustomerEmail("john@example.com");

    Order savedOrder = createOrder(1L, request);
    when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

    // When
    OrderResponse response = orderService.createOrder(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getCustomerName()).isEqualTo("John Doe");
    assertThat(response.getProductName()).isEqualTo("Laptop");
    assertThat(response.getStatus()).isEqualTo(OrderStatus.CREATED);
    assertThat(response.getTotalValue()).isEqualTo(BigDecimal.valueOf(1999.98));

    verify(orderRepository).save(any(Order.class));
    verify(notificationService).sendOrderCreatedNotification(any(Order.class));
  }

  @Test
  void getOrder_ExistingId_ReturnsOrderResponse() {
    // Given
    Long orderId = 1L;
    Order order = createOrder(orderId, "John Doe", "Laptop");
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    // When
    OrderResponse response = orderService.getOrder(orderId);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(orderId);
    assertThat(response.getCustomerName()).isEqualTo("John Doe");
  }

  @Test
  void getOrder_NonExistentId_ThrowsOrderNotFoundException() {
    // Given
    Long orderId = 999L;
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> orderService.getOrder(orderId))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("Order not found with ID: 999");
  }

  @Test
  void updateOrderStatus_ValidTransition_ReturnsUpdatedOrder() {
    // Given
    Long orderId = 1L;
    Order order = createOrder(orderId, "John Doe", "Laptop");
    order.setStatus(OrderStatus.CREATED);

    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    // When
    OrderResponse response = orderService.updateOrderStatus(orderId, request);

    // Then
    assertThat(response.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    verify(orderRepository).save(order);
    verify(notificationService).sendOrderStatusChangedNotification(order, OrderStatus.CREATED);
  }

  @Test
  void updateOrderStatus_InvalidTransition_ThrowsInvalidOrderStatusTransitionException() {
    // Given
    Long orderId = 1L;
    Order order = createOrder(orderId, "John Doe", "Laptop");
    order.setStatus(OrderStatus.COMPLETED); // Terminal state

    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

    // When & Then
    assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, request))
        .isInstanceOf(InvalidOrderStatusTransitionException.class)
        .hasMessage("Cannot transition from COMPLETED to CANCELLED");
  }

  @Test
  void updateOrderStatus_NonExistentOrder_ThrowsOrderNotFoundException() {
    // Given
    Long orderId = 999L;
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);

    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> orderService.updateOrderStatus(orderId, request))
        .isInstanceOf(OrderNotFoundException.class)
        .hasMessage("Order not found with ID: 999");
  }

  @Test
  void searchOrders_WithFilters_ReturnsFilteredResults() {
    // Given
    String customerName = "John";
    String productName = "Laptop";
    OrderStatus status = OrderStatus.CREATED;
    String email = "john@example.com";
    Pageable pageable = PageRequest.of(0, 10);

    List<Order> orders =
        List.of(createOrder(1L, "John Doe", "Laptop"), createOrder(2L, "John Smith", "Laptop Pro"));
    Page<Order> orderPage = new PageImpl<>(orders, pageable, orders.size());

    when(orderRepository.findOrdersByCriteria(customerName, productName, status, email, pageable))
        .thenReturn(orderPage);

    // When
    var response = orderService.searchOrders(customerName, productName, status, email, pageable);

    // Then
    assertThat(response.getOrders()).hasSize(2);
    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getCurrentPage()).isEqualTo(0);
    assertThat(response.getPageSize()).isEqualTo(10);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  private Order createOrder(Long id, CreateOrderRequest request) {
    Order order = new Order();
    order.setId(id);
    order.setCustomerName(request.getCustomerName());
    order.setProductName(request.getProductName());
    order.setQuantity(request.getQuantity());
    order.setPrice(request.getPrice());
    order.setCustomerEmail(request.getCustomerEmail());
    order.setStatus(OrderStatus.CREATED);
    return order;
  }

  private Order createOrder(Long id, String customerName, String productName) {
    Order order = new Order();
    order.setId(id);
    order.setCustomerName(customerName);
    order.setProductName(productName);
    order.setQuantity(1);
    order.setPrice(BigDecimal.valueOf(999.99));
    order.setStatus(OrderStatus.CREATED);
    return order;
  }
}
