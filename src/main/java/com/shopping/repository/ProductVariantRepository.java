package com.shopping.repository;

import com.shopping.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ProductVariant> findByProduct_ProductId(Long productId);

    List<ProductVariant> findByProduct_ProductIdAndIsActiveTrue(Long productId);

    @Query("SELECT v FROM ProductVariant v " +
            "JOIN v.product p " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR " +
            "       LOWER(v.sku) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "       LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:lowStockOnly = false OR v.stockQuantity <= :threshold)")
    Page<ProductVariant> findStockReport(
            @Param("keyword") String keyword,
            @Param("lowStockOnly") boolean lowStockOnly,
            @Param("threshold") int threshold,
            Pageable pageable
    );
}
