package com.shopping.repository;

import com.shopping.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProduct_ProductIdOrderBySortOrderAscImageIdAsc(Long productId);

    Optional<ProductImage> findFirstByProduct_ProductIdAndIsPrimaryTrue(Long productId);

    @Modifying
    @Query("UPDATE ProductImage img SET img.isPrimary = false WHERE img.product.productId = :productId")
    void resetPrimaryForProduct(@Param("productId") Long productId);
}
