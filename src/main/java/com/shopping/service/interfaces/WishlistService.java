package com.shopping.service.interfaces;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.response.wishlist.WishlistResponse;
import org.springframework.data.domain.Pageable;

public interface WishlistService {

    PageResponse<WishlistResponse> getMyWishlist(Pageable pageable);

    WishlistResponse addToWishlist(Long productId);

    void removeFromWishlist(Long productId);

    boolean isWishlisted(Long productId);
}
