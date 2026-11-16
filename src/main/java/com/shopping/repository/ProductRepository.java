package com.shopping.repository;

import com.shopping.entity.Product;
import com.shopping.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySlugAndStatus(String slug, ProductStatus status);

    Optional<Product> findByProductIdAndStatus(Long productId, ProductStatus status);

    boolean existsBySlug(String slug);

    boolean existsByCategory_CategoryId(Long categoryId);

    List<Product> findByCategory_CategoryIdAndProductIdNotAndStatus(
            Long categoryId, Long productId, ProductStatus status, Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Product p " +
            "LEFT JOIN p.variants v " +
            "WHERE (:status IS NULL OR p.status = :status) " +
            "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId OR p.category.parent.categoryId = :categoryId) " +
            "AND (:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) " +
            "AND (:minPrice IS NULL OR p.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.basePrice <= :maxPrice) " +
            "AND (:size IS NULL OR (v.size = :size AND v.isActive = true)) " +
            "AND (:color IS NULL OR (LOWER(v.color) = LOWER(:color) AND v.isActive = true)) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "     LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "     LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> filterProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("size") String size,
            @Param("color") String color,
            @Param("status") ProductStatus status,
            Pageable pageable
    );
}
