package com.danielkim.order.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Main Spring Boot Application class for Order Processing System.
 *
 * <p>This application provides a complete order management system with: - REST API endpoints for
 * order CRUD operations - Multi-channel notification system (Email, SMS) - Security with Spring
 * Security - Database persistence with H2 - Comprehensive error handling and retry
 * mechanism @EnableRetry enables Spring Retry functionality for notification reliability
 */
@SpringBootApplication
@EnableRetry
public class OrderProcessingApplication {

  public static void main(String[] args) {
    SpringApplication.run(OrderProcessingApplication.class, args);
  }
}
