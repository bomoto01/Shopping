package com.shopping.service.impl;

import com.shopping.dto.request.cart.AddToCartRequest;
import com.shopping.dto.request.cart.UpdateCartItemRequest;
import com.shopping.dto.response.cart.CartResponse;
import com.shopping.entity.Cart;
import com.shopping.entity.CartItem;
import com.shopping.entity.ProductVariant;
import com.shopping.entity.User;
import com.shopping.enums.ProductStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ForbiddenException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.CartMapper;
import com.shopping.repository.CartItemRepository;
import com.shopping.repository.CartRepository;
import com.shopping.repository.ProductVariantRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.interfaces.CartService;
import com.shopping.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getMyCart() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Cart cart = getOrCreateCart(currentUserId);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(AddToCartRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Cart cart = getOrCreateCart(currentUserId);

        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Biến thể sản phẩm", "id", request.getVariantId()));

        if (!Boolean.TRUE.equals(variant.getIsActive()) || variant.getProduct().getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Biến thể sản phẩm hiện không còn mở bán");
        }

        int availableStock = variant.getStockQuantity() != null ? variant.getStockQuantity() : 0;
        if (availableStock <= 0) {
            throw new BadRequestException("Sản phẩm đã hết hàng");
        }

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCart_CartIdAndVariant_VariantId(
                cart.getCartId(), variant.getVariantId()
        );

        int targetQuantity = request.getQuantity();
        CartItem cartItem;

        if (existingItemOpt.isPresent()) {
            cartItem = existingItemOpt.get();
            targetQuantity += cartItem.getQuantity();
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(0)
                    .build();
            cart.getItems().add(cartItem);
        }

        // TC-04: Thêm số lượng vượt tồn kho
        if (targetQuantity > availableStock) {
            throw new BadRequestException(String.format(
                    "Số lượng yêu cầu (%d) vượt quá số lượng tồn kho khả dụng (%d)",
                    targetQuantity, availableStock
            ));
        }

        cartItem.setQuantity(targetQuantity);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);

        log.info("Thêm sản phẩm vào giỏ thành công: userId={}, variantId={}, quantity={}",
                currentUserId, variant.getVariantId(), targetQuantity);

        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Cart cart = getOrCreateCart(currentUserId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Dòng giỏ hàng", "id", cartItemId));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new ForbiddenException("Bạn không có quyền thao tác trên dòng giỏ hàng này");
        }

        if (request.getQuantity() <= 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
            log.info("Xóa dòng giỏ hàng do số lượng <= 0: cartItemId={}", cartItemId);
            return cartMapper.toCartResponse(cart);
        }

        int availableStock = cartItem.getVariant().getStockQuantity() != null ? cartItem.getVariant().getStockQuantity() : 0;
        // TC-04: Cập nhật số lượng vượt tồn kho
        if (request.getQuantity() > availableStock) {
            throw new BadRequestException(String.format(
                    "Số lượng yêu cầu (%d) vượt quá số lượng tồn kho khả dụng (%d)",
                    request.getQuantity(), availableStock
            ));
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        log.info("Cập nhật số lượng dòng giỏ hàng: cartItemId={}, quantity={}", cartItemId, request.getQuantity());
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Long cartItemId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Cart cart = getOrCreateCart(currentUserId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Dòng giỏ hàng", "id", cartItemId));

        if (!cartItem.getCart().getCartId().equals(cart.getCartId())) {
            throw new ForbiddenException("Bạn không có quyền thao tác trên dòng giỏ hàng này");
        }

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        log.info("Xóa dòng giỏ hàng: cartItemId={}, userId={}", cartItemId, currentUserId);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public void clearCart() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Cart cart = getOrCreateCart(currentUserId);
        cart.getItems().clear();
        cartItemRepository.deleteAllByCart_CartId(cart.getCartId());
        log.info("Đã làm rỗng giỏ hàng: cartId={}, userId={}", cart.getCartId(), currentUserId);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}
