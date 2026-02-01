package com.danielkim.order.demo.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

/**
 * Application configuration for beans and async processing.
 *
 * <p>Configures: - RestTemplate for external HTTP calls (notifications) - Async task executor for
 * non-blocking notification processing
 */
@Configuration
@EnableAsync
public class AppConfig {

  /**
   * RestTemplate bean for making HTTP requests to external services. Used by NotificationService to
   * send notifications to email/SMS services.
   */
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  /**
   * Custom task executor for async processing. Used for sending notifications without blocking
   * order operations.
   *
   * <p>Configuration: - Core pool size: 2 threads - Max pool size: 5 threads - Queue capacity: 100
   * tasks - Thread name prefix for easier debugging
   */
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("OrderProcessing-");
    executor.initialize();
    return executor;
  }
}
