package com.shopping.service.impl;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.response.wishlist.WishlistResponse;
import com.shopping.entity.Product;
import com.shopping.entity.User;
import com.shopping.entity.Wishlist;
import com.shopping.enums.ProductStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.WishlistMapper;
import com.shopping.repository.ProductRepository;
import com.shopping.repository.UserRepository;
import com.shopping.repository.WishlistRepository;
import com.shopping.service.interfaces.WishlistService;
import com.shopping.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<WishlistResponse> getMyWishlist(Pageable pageable) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Page<Wishlist> page = wishlistRepository.findByUser_UserIdOrderByCreatedAtDesc(currentUserId, pageable);
        return PageResponse.from(page.map(wishlistMapper::toResponse));
    }

    @Override
    @Transactional
    public WishlistResponse addToWishlist(Long productId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm", "id", productId));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Sản phẩm hiện không hoạt động hoặc đã ngừng kinh doanh");
        }

        var existingOpt = wishlistRepository.findByUser_UserIdAndProduct_ProductId(currentUserId, productId);
        if (existingOpt.isPresent()) {
            return wishlistMapper.toResponse(existingOpt.get());
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        Wishlist saved = wishlistRepository.save(wishlist);
        log.info("Thêm sản phẩm vào danh sách yêu thích: userId={}, productId={}", currentUserId, productId);
        return wishlistMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long productId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        wishlistRepository.deleteByUser_UserIdAndProduct_ProductId(currentUserId, productId);
        log.info("Xóa sản phẩm khỏi danh sách yêu thích: userId={}, productId={}", currentUserId, productId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isWishlisted(Long productId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return wishlistRepository.existsByUser_UserIdAndProduct_ProductId(currentUserId, productId);
    }
}
