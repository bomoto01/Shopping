package com.shopping.repository;

import com.shopping.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    @Query("SELECT c FROM Coupon c " +
            "WHERE c.isActive = true " +
            "AND :now >= c.startAt AND :now <= c.endAt " +
            "AND (c.usageLimit = 0 OR c.usedCount < c.usageLimit) " +
            "ORDER BY c.endAt ASC")
    List<Coupon> findAvailableCoupons(@Param("now") Instant now);

    @Query("SELECT c FROM Coupon c " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:isActive IS NULL OR c.isActive = :isActive)")
    Page<Coupon> searchCoupons(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}
