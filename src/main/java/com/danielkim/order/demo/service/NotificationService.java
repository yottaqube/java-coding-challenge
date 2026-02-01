package com.danielkim.order.demo.service;

import com.danielkim.order.demo.model.Order;
import com.danielkim.order.demo.model.OrderStatus;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Notification service handling multi-channel notifications.
 *
 * <p>Features: - Supports Email and SMS notifications - Configurable notification channels via
 * application properties - Retry mechanism for handling temporary failures - Asynchronous
 * processing to avoid blocking order operations - Comprehensive error handling and logging
 *
 * <p>The service can be easily extended to support additional notification channels like push
 * notifications, webhooks, etc.
 */
@Service
public class NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

  private final RestTemplate restTemplate;

  @Value("${notification.channels.email.enabled:true}")
  private boolean emailEnabled;

  @Value("${notification.channels.sms.enabled:true}")
  private boolean smsEnabled;

  @Value("${notification.channels.email.url}")
  private String emailServiceUrl;

  @Value("${notification.channels.sms.url}")
  private String smsServiceUrl;

  public NotificationService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * Sends notification when a new order is created. Executes asynchronously to avoid blocking the
   * order creation process.
   */
  @Async
  public void sendOrderCreatedNotification(Order order) {
    logger.info("Sending order created notification for order ID: {}", order.getId());

    Map<String, Object> notificationData =
        createNotificationData(order, "ORDER_CREATED", "Your order has been created successfully");

    sendNotifications(notificationData);
  }

  /**
   * Sends notification when order status changes. Executes asynchronously to avoid blocking the
   * order update process.
   */
  @Async
  public void sendOrderStatusChangedNotification(Order order, OrderStatus oldStatus) {
    logger.info(
        "Sending order status change notification for order ID: {} from {} to {}",
        order.getId(),
        oldStatus,
        order.getStatus());

    String message = createStatusChangeMessage(order.getStatus());
    Map<String, Object> notificationData =
        createNotificationData(order, "ORDER_STATUS_CHANGED", message);
    notificationData.put("oldStatus", oldStatus.toString());

    sendNotifications(notificationData);
  }

  /** Sends notifications through all configured channels. */
  private void sendNotifications(Map<String, Object> notificationData) {
    if (emailEnabled && hasEmailContact(notificationData)) {
      sendEmailNotification(notificationData);
    }

    if (smsEnabled && hasSmsContact(notificationData)) {
      sendSmsNotification(notificationData);
    }
  }

  /**
   * Sends email notification with retry mechanism. Retries up to 3 times with exponential backoff
   * on failure.
   */
  @Retryable(
      retryFor = {ResourceAccessException.class, HttpServerErrorException.class, Exception.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  private void sendEmailNotification(Map<String, Object> notificationData) {
    try {
      logger.debug("Sending email notification to: {}", notificationData.get("customerEmail"));
      restTemplate.postForObject(emailServiceUrl, notificationData, String.class);
      logger.info(
          "Email notification sent successfully for order ID: {}", notificationData.get("orderId"));
    } catch (Exception e) {
      logger.error(
          "Failed to send email notification for order ID: {}", notificationData.get("orderId"), e);
      throw e; // Re-throw to trigger retry
    }
  }

  /**
   * Sends SMS notification with retry mechanism. Retries up to 3 times with exponential backoff on
   * failure.
   */
  @Retryable(
      retryFor = {ResourceAccessException.class, HttpServerErrorException.class, Exception.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  private void sendSmsNotification(Map<String, Object> notificationData) {
    try {
      logger.debug("Sending SMS notification to: {}", notificationData.get("customerPhone"));
      restTemplate.postForObject(smsServiceUrl, notificationData, String.class);
      logger.info(
          "SMS notification sent successfully for order ID: {}", notificationData.get("orderId"));
    } catch (Exception e) {
      logger.error(
          "Failed to send SMS notification for order ID: {}", notificationData.get("orderId"), e);
      throw e; // Re-throw to trigger retry
    }
  }

  /** Creates notification data payload with order information. */
  private Map<String, Object> createNotificationData(
      Order order, String eventType, String message) {
    Map<String, Object> data = new HashMap<>();
    data.put("orderId", order.getId());
    data.put("customerName", order.getCustomerName());
    data.put("customerEmail", order.getCustomerEmail());
    data.put("customerPhone", order.getCustomerPhone());
    data.put("productName", order.getProductName());
    data.put("quantity", order.getQuantity());
    data.put("price", order.getPrice());
    data.put("totalValue", order.getTotalValue());
    data.put("status", order.getStatus().toString());
    data.put("eventType", eventType);
    data.put("message", message);
    data.put("timestamp", order.getUpdatedAt());
    return data;
  }

  /** Creates human-readable message for status changes. */
  private String createStatusChangeMessage(OrderStatus status) {
    return switch (status) {
      case COMPLETED -> "Your order has been completed successfully";
      case CANCELLED -> "Your order has been cancelled";
      default -> "Your order status has been updated to " + status;
    };
  }

  /** Checks if customer has email contact information. */
  private boolean hasEmailContact(Map<String, Object> data) {
    String email = (String) data.get("customerEmail");
    return email != null && !email.trim().isEmpty();
  }

  /** Checks if customer has phone contact information. */
  private boolean hasSmsContact(Map<String, Object> data) {
    String phone = (String) data.get("customerPhone");
    return phone != null && !phone.trim().isEmpty();
  }
}
