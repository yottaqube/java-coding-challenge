package com.danielkim.order.demo.dto;

import java.util.List;

/**
 * Data Transfer Object for paginated search results. Provides pagination metadata along with search
 * results.
 */
public class OrderSearchResponse {

  private List<OrderResponse> orders;
  private long totalElements;
  private int totalPages;
  private int currentPage;
  private int pageSize;
  private boolean hasNext;
  private boolean hasPrevious;

  // Constructors
  public OrderSearchResponse() {}

  public OrderSearchResponse(
      List<OrderResponse> orders,
      long totalElements,
      int totalPages,
      int currentPage,
      int pageSize) {
    this.orders = orders;
    this.totalElements = totalElements;
    this.totalPages = totalPages;
    this.currentPage = currentPage;
    this.pageSize = pageSize;
    this.hasNext = currentPage < totalPages - 1;
    this.hasPrevious = currentPage > 0;
  }

  // Getters and Setters
  public List<OrderResponse> getOrders() {
    return orders;
  }

  public void setOrders(List<OrderResponse> orders) {
    this.orders = orders;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public void setCurrentPage(int currentPage) {
    this.currentPage = currentPage;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public void setHasNext(boolean hasNext) {
    this.hasNext = hasNext;
  }

  public boolean isHasPrevious() {
    return hasPrevious;
  }

  public void setHasPrevious(boolean hasPrevious) {
    this.hasPrevious = hasPrevious;
  }
}
