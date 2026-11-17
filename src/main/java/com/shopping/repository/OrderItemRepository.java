package com.shopping.repository;

import com.shopping.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_OrderId(Long orderId);

    @Query("SELECT item.variant.product.productId, item.productNameSnapshot, SUM(item.quantity), SUM(item.lineTotal) " +
           "FROM OrderItem item WHERE item.order.orderStatus = 'DELIVERED' " +
           "GROUP BY item.variant.product.productId, item.productNameSnapshot " +
           "ORDER BY SUM(item.quantity) DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);
}
