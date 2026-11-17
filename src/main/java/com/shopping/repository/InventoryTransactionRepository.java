package com.shopping.repository;

import com.shopping.entity.InventoryTransaction;
import com.shopping.enums.InventoryReferenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

    @Query("SELECT t FROM InventoryTransaction t " +
            "WHERE (:variantId IS NULL OR t.variant.variantId = :variantId) " +
            "AND (:referenceType IS NULL OR t.referenceType = :referenceType)")
    Page<InventoryTransaction> findFilteredTransactions(
            @Param("variantId") Long variantId,
            @Param("referenceType") InventoryReferenceType referenceType,
            Pageable pageable
    );
}
