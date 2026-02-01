package com.danielkim.order.demo.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.danielkim.order.demo.dto.CreateOrderRequest;
import com.danielkim.order.demo.dto.UpdateOrderStatusRequest;
import com.danielkim.order.demo.model.OrderStatus;
import com.danielkim.order.demo.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the complete Order Processing System.
 *
 * <p>Tests the full stack from HTTP endpoints down to database persistence with real security,
 * transaction management, and database operations.
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:testdb-integration",
      "logging.level.com.orderprocessing=INFO"
    })
@AutoConfigureMockMvc
class OrderProcessingIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private OrderRepository orderRepository;

  @BeforeEach
  void setUp() {
    orderRepository.deleteAll();
  }

  @Test
  @WithMockUser(roles = "USER")
  void completeOrderLifecycle_CreateRetrieveUpdateSearch() throws Exception {
    // 1. Create Order
    CreateOrderRequest createRequest = new CreateOrderRequest();
    createRequest.setCustomerName("Integration Test User");
    createRequest.setProductName("Integration Test Product");
    createRequest.setQuantity(2);
    createRequest.setPrice(BigDecimal.valueOf(150.00));
    createRequest.setCustomerEmail("integration@test.com");

    String createResponse =
        mockMvc
            .perform(
                post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.customerName").value("Integration Test User"))
            .andExpect(jsonPath("$.status").value("CREATED"))
            .andExpect(jsonPath("$.totalValue").value(300.00))
            .andReturn()
            .getResponse()
            .getContentAsString();

    // Extract order ID for subsequent operations
    var orderResponse = objectMapper.readTree(createResponse);
    Long orderId = orderResponse.get("id").asLong();

    // 2. Retrieve Order
    mockMvc
        .perform(get("/api/orders/{id}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId))
        .andExpect(jsonPath("$.customerName").value("Integration Test User"))
        .andExpect(jsonPath("$.status").value("CREATED"));

    // 3. Update Order Status
    UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);

    mockMvc
        .perform(
            put("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId))
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    // 4. Search Orders
    mockMvc
        .perform(
            get("/api/orders/search")
                .param("customerName", "Integration Test")
                .param("status", "COMPLETED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orders").isArray())
        .andExpect(jsonPath("$.orders[0].id").value(orderId))
        .andExpect(jsonPath("$.orders[0].status").value("COMPLETED"))
        .andExpect(jsonPath("$.totalElements").value(1));

    // 5. Verify pagination
    mockMvc
        .perform(
            get("/api/orders/search")
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "customerName")
                .param("sortDir", "asc"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.currentPage").value(0))
        .andExpect(jsonPath("$.pageSize").value(5));
  }

  @Test
  @WithMockUser(roles = "USER")
  void orderStatusTransition_InvalidTransition_ReturnsBadRequest() throws Exception {
    // Given - Create an order and complete it
    CreateOrderRequest createRequest = new CreateOrderRequest();
    createRequest.setCustomerName("Test User");
    createRequest.setProductName("Test Product");
    createRequest.setQuantity(1);
    createRequest.setPrice(BigDecimal.valueOf(100.00));

    String createResponse =
        mockMvc
            .perform(
                post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Long orderId = objectMapper.readTree(createResponse).get("id").asLong();

    // Complete the order first
    UpdateOrderStatusRequest completeRequest = new UpdateOrderStatusRequest(OrderStatus.COMPLETED);
    mockMvc
        .perform(
            put("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeRequest)))
        .andExpect(status().isOk());

    // When - Try to cancel a completed order (invalid transition)
    UpdateOrderStatusRequest cancelRequest = new UpdateOrderStatusRequest(OrderStatus.CANCELLED);

    // Then
    mockMvc
        .perform(
            put("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Invalid Status Transition"))
        .andExpect(jsonPath("$.message").value("Cannot transition from COMPLETED to CANCELLED"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void getOrder_NonExistent_ReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/api/orders/{id}", 999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order Not Found"))
        .andExpect(jsonPath("$.message").value("Order not found with ID: 999"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void createOrder_InvalidData_ReturnsValidationErrors() throws Exception {
    CreateOrderRequest invalidRequest = new CreateOrderRequest();
    invalidRequest.setCustomerName(""); // Invalid - blank
    invalidRequest.setQuantity(-1); // Invalid - negative
    invalidRequest.setCustomerEmail("invalid-email"); // Invalid format

    mockMvc
        .perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Validation Failed"))
        .andExpect(jsonPath("$.fieldErrors").exists())
        .andExpect(jsonPath("$.fieldErrors.customerName").exists())
        .andExpect(jsonPath("$.fieldErrors.quantity").exists());
  }

  @Test
  void unauthorizedAccess_ReturnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/orders/search")).andExpect(status().isUnauthorized());
  }
}
