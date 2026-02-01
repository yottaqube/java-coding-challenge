package com.danielkim.order.demo.controller;

import com.danielkim.order.demo.benchmark.QueryPerformanceBenchmark;
import com.danielkim.order.demo.model.OrderStatus;
import com.danielkim.order.demo.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Performance testing controller for query benchmarking.
 *
 * <p>Provides endpoints to test and compare performance between different query approaches.
 */
@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

  private final QueryPerformanceBenchmark benchmark;
  private final OrderService orderService;

  public PerformanceController(QueryPerformanceBenchmark benchmark, OrderService orderService) {
    this.benchmark = benchmark;
    this.orderService = orderService;
  }

  /**
   * Run performance benchmark comparing query approaches.
   *
   * @return Performance comparison results
   */
  @GetMapping("/query-benchmark")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> runQueryBenchmark() {
    QueryPerformanceBenchmark.PerformanceResult result = benchmark.comparePerformance();
    return ResponseEntity.ok(result.toString());
  }

  /**
   * Compare JPQL vs Native SQL query performance directly.
   *
   * @param customerName Optional customer name filter
   * @param productName Optional product name filter
   * @param status Optional status filter
   * @param email Optional email filter
   * @return Performance comparison results
   */
  @GetMapping("/jpql-vs-native")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> compareJpqlVsNative(
      @RequestParam(required = false) String customerName,
      @RequestParam(required = false) String productName,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) String email) {

    OrderService.QueryComparisonResult result =
        orderService.compareQueryMethods(
            customerName, productName, status, email, PageRequest.of(0, 10));

    return ResponseEntity.ok(result.toString());
  }
}
