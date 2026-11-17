package com.shopping.controller;

import com.shopping.apiResponse.ApiResponse;
import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.UpdateOrderStatusRequest;
import com.shopping.dto.response.OrderDetailResponse;
import com.shopping.dto.response.OrderSummaryResponse;
import com.shopping.enums.OrderStatus;
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
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@Tag(name = "Order Admin", description = "APIs quản lý và xử lý đơn hàng cho Admin & Staff")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Danh sách đơn hàng toàn hệ thống (lọc theo keyword mã đơn/tên/sđt và trạng thái)")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<OrderSummaryResponse> response = orderService.getAdminOrders(keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy danh sách đơn hàng thành công", response));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Xem chi tiết đơn hàng")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderDetail(@PathVariable Long orderId) {
        OrderDetailResponse response = orderService.getAdminOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Lấy chi tiết đơn hàng thành công", response));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (CONFIRMED, PACKING, SHIPPING, DELIVERED, CANCELLED, RETURNED)")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderDetailResponse response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK.value(), "Cập nhật trạng thái đơn hàng thành công", response));
    }
}
