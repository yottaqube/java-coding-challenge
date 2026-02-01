package com.danielkim.order.demo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.danielkim.order.demo.config.SecurityConfig;
import com.danielkim.order.demo.dto.CreateOrderRequest;
import com.danielkim.order.demo.dto.OrderResponse;
import com.danielkim.order.demo.dto.OrderSearchResponse;
import com.danielkim.order.demo.dto.UpdateOrderStatusRequest;
import com.danielkim.order.demo.model.OrderStatus;
import com.danielkim.order.demo.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Unit tests for OrderController.
 *
 * <p>Tests cover: - Order creation with validation - Order retrieval - Order status updates -
 * Search functionality - Security integration - Error handling scenarios
 */
@WebMvcTest({OrderController.class})
@Import(SecurityConfig.class) // Security Configuration 포함
class OrderControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private OrderService orderService;

  @Test
  @WithMockUser(roles = "USER")
  void createOrder_ValidRequest_ReturnsCreated() throws Exception {
    // Given
    CreateOrderRequest request = new CreateOrderRequest();
    request.setCustomerName("John Doe");
    request.setProductName("Laptop");
    request.setQuantity(1);
    request.setPrice(BigDecimal.valueOf(999.99));
    request.setCustomerEmail("john@example.com");

    OrderResponse response = createOrderResponse(1L, request);
    when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

    // When & Then
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.customerName").value("John Doe"))
        .andExpect(jsonPath("$.productName").value("Laptop"))
        .andExpect(jsonPath("$.status").value("CREATED"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void createOrder_InvalidRequest_ReturnsBadRequest() throws Exception {
    // Given - Invalid request with missing required fields
    CreateOrderRequest request = new CreateOrderRequest();
    request.setCustomerName(""); // Invalid - blank
    request.setQuantity(-1); // Invalid - negative

    // When & Then
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation Failed"))
        .andExpect(jsonPath("$.fieldErrors").exists());
  }

  @Test
  void createOrder_Unauthenticated_ReturnsUnauthorized() throws Exception {
    // Given
    CreateOrderRequest request = new CreateOrderRequest();
    request.setCustomerName("John Doe");
    request.setProductName("Laptop");
    request.setQuantity(1);
    request.setPrice(BigDecimal.valueOf(999.99));

    // When & Then
    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void getOrder_ValidId_ReturnsOrder() throws Exception {
    // Given
    Long orderId = 1L;
    OrderResponse response = createOrderResponse(orderId, "John Doe", "Laptop");
    when(orderService.getOrder(orderId)).thenReturn(response);

    // When & Then
    mockMvc
        .perform(get("/api/orders/{id}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.customerName").value("John Doe"))
        .andExpect(jsonPath("$.productName").value("Laptop"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void updateOrderStatus_ValidRequest_ReturnsUpdatedOrder() throws Exception {
    // Given
    Long orderId = 1L;
    UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);

    OrderResponse response = createOrderResponse(orderId, "John Doe", "Laptop");
    response.setStatus(OrderStatus.COMPLETED);

    when(orderService.updateOrderStatus(eq(orderId), any(UpdateOrderStatusRequest.class)))
        .thenReturn(response);

    // When & Then
    mockMvc
        .perform(
            put("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void searchOrders_WithFilters_ReturnsFilteredResults() throws Exception {
    // Given
    OrderResponse orderResponse = createOrderResponse(1L, "John Doe", "Laptop");
    OrderSearchResponse searchResponse = new OrderSearchResponse();
    searchResponse.setOrders(List.of(orderResponse));
    searchResponse.setTotalElements(1L);
    searchResponse.setTotalPages(1);
    searchResponse.setCurrentPage(0);
    searchResponse.setPageSize(10);

    when(orderService.searchOrders(
            eq("John"), eq(null), eq(OrderStatus.CREATED), eq(null), any(Pageable.class)))
        .thenReturn(searchResponse);

    // When & Then
    mockMvc
        .perform(
            get("/api/orders/search")
                .param("customerName", "John")
                .param("status", "CREATED")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orders").exists())
        .andExpect(jsonPath("$.orders[0].customerName").value("John Doe"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  void searchOrders_WithBasicAuth_ReturnsResults() throws Exception {
    // Given
    OrderResponse orderResponse = createOrderResponse(1L, "John Doe", "Laptop");
    OrderSearchResponse searchResponse = new OrderSearchResponse();
    searchResponse.setOrders(List.of(orderResponse));
    searchResponse.setTotalElements(1L);
    searchResponse.setTotalPages(1);
    searchResponse.setCurrentPage(0);
    searchResponse.setPageSize(10);

    when(orderService.searchOrders(eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
        .thenReturn(searchResponse);

    // When & Then
    mockMvc
        .perform(get("/api/orders/search").with(httpBasic("user", "password")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orders").exists())
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  private OrderResponse createOrderResponse(Long id, CreateOrderRequest request) {
    OrderResponse response = new OrderResponse();
    response.setId(id);
    response.setCustomerName(request.getCustomerName());
    response.setProductName(request.getProductName());
    response.setQuantity(request.getQuantity());
    response.setPrice(request.getPrice());
    response.setTotalValue(request.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
    response.setStatus(OrderStatus.CREATED);
    response.setCreatedAt(LocalDateTime.now());
    response.setUpdatedAt(LocalDateTime.now());
    response.setCustomerEmail(request.getCustomerEmail());
    return response;
  }

  private OrderResponse createOrderResponse(Long id, String customerName, String productName) {
    OrderResponse response = new OrderResponse();
    response.setId(id);
    response.setCustomerName(customerName);
    response.setProductName(productName);
    response.setQuantity(1);
    response.setPrice(BigDecimal.valueOf(999.99));
    response.setTotalValue(BigDecimal.valueOf(999.99));
    response.setStatus(OrderStatus.CREATED);
    response.setCreatedAt(LocalDateTime.now());
    response.setUpdatedAt(LocalDateTime.now());
    return response;
  }
}
