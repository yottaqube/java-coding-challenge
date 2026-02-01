package com.danielkim.order.demo.repository;

import com.danielkim.order.demo.model.Order;
import com.danielkim.order.demo.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Order entity. Provides custom queries for searching orders with various
 * criteria.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

  /** Find orders by customer name (case-insensitive partial matching). */
  Page<Order> findByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);

  /** Find orders by product name (case-insensitive partial matching). */
  Page<Order> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

  /** Find orders by status. */
  Page<Order> findByStatus(OrderStatus status, Pageable pageable);

  /** Find orders by customer email. */
  Page<Order> findByCustomerEmailContainingIgnoreCase(String email, Pageable pageable);

  /**
   * Advanced search query supporting multiple criteria. Searches across customer name, product
   * name, and email with status filtering.
   */
  @Query(
      "SELECT o FROM Order o WHERE "
          + "(:customerName IS NULL OR LOWER(o.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND "
          + "(:productName IS NULL OR LOWER(o.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND "
          + "(:status IS NULL OR o.status = :status) AND "
          + "(:email IS NULL OR LOWER(o.customerEmail) LIKE LOWER(CONCAT('%', :email, '%')))")
  Page<Order> findOrdersByCriteria(
      @Param("customerName") String customerName,
      @Param("productName") String productName,
      @Param("status") OrderStatus status,
      @Param("email") String email,
      Pageable pageable);

  /**
   * Optimized version using native SQL for better performance on large datasets.
   *
   * <p>Benefits: - Database-specific optimizations - Better index utilization - Reduced JPA
   * overhead - Direct SQL execution without JPQL translation
   *
   * <p>Note: Uses actual table/column names instead of entity field names
   */
  @Query(
      nativeQuery = true,
      value =
          "SELECT * FROM orders o WHERE "
              + "(:customerName IS NULL OR LOWER(o.customer_name) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND "
              + "(:productName IS NULL OR LOWER(o.product_name) LIKE LOWER(CONCAT('%', :productName, '%'))) AND "
              + "(:status IS NULL OR o.status = CAST(:status AS VARCHAR)) AND "
              + "(:email IS NULL OR LOWER(o.customer_email) LIKE LOWER(CONCAT('%', :email, '%'))) "
              + "ORDER BY o.created_at DESC")
  Page<Order> findOrdersByCriteriaNative(
      @Param("customerName") String customerName,
      @Param("productName") String productName,
      @Param("status") String status, // Enum은 String으로 전달해야 함
      @Param("email") String email,
      Pageable pageable);
}
