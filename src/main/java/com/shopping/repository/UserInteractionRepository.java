package com.shopping.repository;

import com.shopping.entity.UserInteraction;
import com.shopping.enums.InteractionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    Page<UserInteraction> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<UserInteraction> findTop50ByUser_UserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("DELETE FROM UserInteraction u WHERE u.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT u.product.productId, COUNT(u) as cnt FROM UserInteraction u " +
           "WHERE u.interactionType = :type GROUP BY u.product.productId ORDER BY cnt DESC")
    List<Object[]> findTopProductIdsByInteractionType(@Param("type") InteractionType type, Pageable pageable);
}
