package com.shopping.repository;

import com.shopping.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderItem_OrderItemId(Long orderItemId);

    Optional<Review> findByOrderItem_OrderItemId(Long orderItemId);

    Page<Review> findByProduct_ProductIdAndIsApprovedTrueOrderByCreatedAtDesc(Long productId, Pageable pageable);

    Page<Review> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.productId = :productId AND r.isApproved = true")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    long countByProduct_ProductIdAndIsApprovedTrue(Long productId);
}
