package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.constants.PaginationConstants;
import com.shopping.dto.response.wishlist.WishlistResponse;
import com.shopping.service.interfaces.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/wishlist")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Wishlist", description = "Các API quản lý danh sách sản phẩm yêu thích của khách hàng")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Xem danh sách sản phẩm yêu thích", description = "Lấy danh sách các sản phẩm khách hàng đã bấm yêu thích.")
    public ResponseEntity<ApiResponse<PageResponse<WishlistResponse>>> getMyWishlist(
            @RequestParam(name = "page", defaultValue = PaginationConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(name = "size", defaultValue = PaginationConstants.DEFAULT_PAGE_SIZE) int size
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, PaginationConstants.MAX_PAGE_SIZE));
        PageResponse<WishlistResponse> response = wishlistService.getMyWishlist(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Thêm sản phẩm vào danh sách yêu thích", description = "Đánh dấu sản phẩm yêu thích cho tài khoản.")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(@PathVariable("productId") Long productId) {
        WishlistResponse response = wishlistService.addToWishlist(productId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Thêm vào danh sách yêu thích thành công", response));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Bỏ sản phẩm khỏi danh sách yêu thích", description = "Xóa sản phẩm khỏi danh sách yêu thích của tài khoản.")
    public ResponseEntity<ApiResponse<Void>> removeFromWishlist(@PathVariable("productId") Long productId) {
        wishlistService.removeFromWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success(200, "Đã xóa khỏi danh sách yêu thích", null));
    }

    @GetMapping("/{productId}/check")
    @Operation(summary = "Kiểm tra sản phẩm đã yêu thích hay chưa", description = "Trả về boolean xem sản phẩm đã có trong wishlist hay chưa.")
    public ResponseEntity<ApiResponse<Boolean>> isWishlisted(@PathVariable("productId") Long productId) {
        boolean exists = wishlistService.isWishlisted(productId);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}
