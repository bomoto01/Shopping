package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.dto.request.cart.AddToCartRequest;
import com.shopping.dto.request.cart.UpdateCartItemRequest;
import com.shopping.dto.response.cart.CartResponse;
import com.shopping.service.interfaces.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
@Tag(name = "Cart", description = "Các API quản lý giỏ hàng của khách hàng (Yêu cầu đăng nhập - Cart Policy)")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Xem giỏ hàng hiện tại", description = "Lấy toàn bộ sản phẩm trong giỏ hàng cùng số lượng và tổng tiền tạm tính.")
    public ResponseEntity<ApiResponse<CartResponse>> getMyCart() {
        CartResponse response = cartService.getMyCart();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/items")
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng", description = "Thêm biến thể SKU và số lượng vào giỏ (kiểm tra tồn kho khả dụng).")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Thêm vào giỏ hàng thành công", response));
    }

    @PatchMapping("/items/{id}")
    @Operation(summary = "Cập nhật số lượng dòng giỏ hàng", description = "Thay đổi số lượng của một dòng hàng (kiểm tra tồn kho, xóa dòng nếu số lượng <= 0).")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse response = cartService.updateCartItem(id, request);
        return ResponseEntity.ok(ApiResponse.success(200, "Cập nhật số lượng thành công", response));
    }

    @DeleteMapping("/items/{id}")
    @Operation(summary = "Xóa một dòng sản phẩm khỏi giỏ hàng", description = "Xóa biến thể đã chọn ra khỏi giỏ.")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(@PathVariable("id") Long id) {
        CartResponse response = cartService.removeCartItem(id);
        return ResponseEntity.ok(ApiResponse.success(200, "Đã xóa sản phẩm khỏi giỏ hàng", response));
    }

    @DeleteMapping("/items")
    @Operation(summary = "Làm rỗng giỏ hàng", description = "Xóa toàn bộ sản phẩm có trong giỏ hàng hiện tại.")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(ApiResponse.success(200, "Đã làm rỗng giỏ hàng", null));
    }
}
