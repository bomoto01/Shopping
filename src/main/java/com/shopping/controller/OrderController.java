package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.CheckoutRequest;
import com.shopping.dto.response.OrderDetailResponse;
import com.shopping.dto.response.OrderSummaryResponse;
import com.shopping.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Customer", description = "APIs đặt hàng và quản lý đơn hàng của khách hàng")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @Operation(summary = "Đặt hàng từ giỏ hàng (Checkout)")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        OrderDetailResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED.value(), "Đặt hàng thành công", response));
    }

    @GetMapping
    @Operation(summary = "Xem danh sách đơn hàng của tôi")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getMyOrders(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<OrderSummaryResponse> response = orderService.getMyOrders(pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách đơn hàng thành công", response));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Xem chi tiết một đơn hàng của tôi")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getMyOrderDetail(@PathVariable Long orderId) {
        OrderDetailResponse response = orderService.getMyOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy chi tiết đơn hàng thành công", response));
    }

    @PutMapping("/{orderId}/cancel")
    @Operation(summary = "Khách hàng hủy đơn hàng (khi đang ở trạng thái PENDING hoặc CONFIRMED)")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason) {
        OrderDetailResponse response = orderService.cancelOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Hủy đơn hàng thành công", response));
    }
}
