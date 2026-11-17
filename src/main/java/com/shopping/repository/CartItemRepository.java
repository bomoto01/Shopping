package com.shopping.repository;

import com.shopping.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCart_CartIdAndVariant_VariantId(Long cartId, Long variantId);

    List<CartItem> findByCart_CartId(Long cartId);

    void deleteAllByCart_CartId(Long cartId);
}
