package com.shopping.service;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.CheckoutRequest;
import com.shopping.dto.request.UpdateOrderStatusRequest;
import com.shopping.dto.response.OrderDetailResponse;
import com.shopping.dto.response.OrderSummaryResponse;
import com.shopping.enums.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    OrderDetailResponse placeOrder(CheckoutRequest request);

    PageResponse<OrderSummaryResponse> getMyOrders(Pageable pageable);

    OrderDetailResponse getMyOrderDetail(Long orderId);

    OrderDetailResponse cancelOrder(Long orderId, String reason);

    PageResponse<OrderSummaryResponse> getAdminOrders(String keyword, OrderStatus status, Pageable pageable);

    OrderDetailResponse getAdminOrderDetail(Long orderId);

    OrderDetailResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}
