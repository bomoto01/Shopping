package com.shopping.service.interfaces;

import com.shopping.dto.request.cart.AddToCartRequest;
import com.shopping.dto.request.cart.UpdateCartItemRequest;
import com.shopping.dto.response.cart.CartResponse;

public interface CartService {

    CartResponse getMyCart();

    CartResponse addToCart(AddToCartRequest request);

    CartResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request);

    CartResponse removeCartItem(Long cartItemId);

    void clearCart();
}
