package com.shopping.repository;

import com.shopping.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    Page<Wishlist> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Wishlist> findByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    boolean existsByUser_UserIdAndProduct_ProductId(Long userId, Long productId);

    void deleteByUser_UserIdAndProduct_ProductId(Long userId, Long productId);
}
