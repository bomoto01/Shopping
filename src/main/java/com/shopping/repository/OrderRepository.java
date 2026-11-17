package com.shopping.repository;

import com.shopping.entity.Order;
import com.shopping.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE " +
           "(:keyword IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(o.addressSnapshot) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR o.orderStatus = :status)")
    Page<Order> findAdminOrders(@Param("keyword") String keyword,
                               @Param("status") OrderStatus status,
                               Pageable pageable);
}
