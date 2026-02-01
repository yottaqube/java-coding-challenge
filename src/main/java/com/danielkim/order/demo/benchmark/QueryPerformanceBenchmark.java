package com.danielkim.order.demo.benchmark;

import com.danielkim.order.demo.model.Order;
import com.danielkim.order.demo.model.OrderStatus;
import com.danielkim.order.demo.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Performance benchmark comparing different query approaches.
 *
 * <p>Compares method-name based queries vs custom @Query performance for complex search scenarios.
 */
@Component
public class QueryPerformanceBenchmark {

  @Autowired private OrderRepository orderRepository;

  /**
   * Benchmark: Multiple separate queries (method-name based approach).
   *
   * @return Execution time in milliseconds
   */
  public long benchmarkSeparateQueries(
      String customerName, String productName, OrderStatus status, String email) {
    long startTime = System.currentTimeMillis();

    Pageable pageable = PageRequest.of(0, 10);

    // Multiple database hits - inefficient for complex conditions
    Page<Order> result1 =
        customerName != null
            ? orderRepository.findByCustomerNameContainingIgnoreCase(customerName, pageable)
            : Page.empty();

    Page<Order> result2 =
        productName != null
            ? orderRepository.findByProductNameContainingIgnoreCase(productName, pageable)
            : Page.empty();

    Page<Order> result3 =
        status != null ? orderRepository.findByStatus(status, pageable) : Page.empty();

    Page<Order> result4 =
        email != null
            ? orderRepository.findByCustomerEmailContainingIgnoreCase(email, pageable)
            : Page.empty();

    // Application-level merging (expensive)
    // In real scenario, you'd need complex intersection logic

    long endTime = System.currentTimeMillis();
    return endTime - startTime;
  }

  /**
   * Benchmark: Single optimized query (custom @Query approach).
   *
   * @return Execution time in milliseconds
   */
  public long benchmarkSingleOptimizedQuery(
      String customerName, String productName, OrderStatus status, String email) {
    long startTime = System.currentTimeMillis();

    Pageable pageable = PageRequest.of(0, 10);

    // Single database hit - optimal
    Page<Order> result =
        orderRepository.findOrdersByCriteria(customerName, productName, status, email, pageable);

    long endTime = System.currentTimeMillis();
    return endTime - startTime;
  }

  /**
   * Run performance comparison test.
   *
   * @return Performance comparison results
   */
  public PerformanceResult comparePerformance() {
    String customerName = "test";
    String productName = "product";
    OrderStatus status = OrderStatus.CREATED;
    String email = "test@example.com";

    // Warm up
    for (int i = 0; i < 10; i++) {
      benchmarkSeparateQueries(customerName, productName, status, email);
      benchmarkSingleOptimizedQuery(customerName, productName, status, email);
    }

    // Actual benchmark
    long separateQueriesTime = 0;
    long singleQueryTime = 0;
    int iterations = 100;

    for (int i = 0; i < iterations; i++) {
      separateQueriesTime += benchmarkSeparateQueries(customerName, productName, status, email);
      singleQueryTime += benchmarkSingleOptimizedQuery(customerName, productName, status, email);
    }

    return new PerformanceResult(
        separateQueriesTime / iterations, singleQueryTime / iterations, iterations);
  }

  /** Performance test result holder. */
  public static class PerformanceResult {
    private final long averageSeparateQueriesTime;
    private final long averageSingleQueryTime;
    private final int iterations;

    public PerformanceResult(long separateTime, long singleTime, int iterations) {
      this.averageSeparateQueriesTime = separateTime;
      this.averageSingleQueryTime = singleTime;
      this.iterations = iterations;
    }

    public double getPerformanceImprovement() {
      return (double) averageSeparateQueriesTime / averageSingleQueryTime;
    }

    @Override
    public String toString() {
      return String.format(
          "Performance Benchmark Results (avg over %d iterations):\n"
              + "- Separate Queries: %d ms\n"
              + "- Single Query: %d ms\n"
              + "- Improvement: %.1fx faster",
          iterations,
          averageSeparateQueriesTime,
          averageSingleQueryTime,
          getPerformanceImprovement());
    }
  }
}
