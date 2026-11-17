package com.shopping.mapper;

import com.shopping.dto.response.OrderDetailResponse;
import com.shopping.dto.response.OrderItemResponse;
import com.shopping.dto.response.OrderStatusHistoryResponse;
import com.shopping.dto.response.OrderSummaryResponse;
import com.shopping.entity.Order;
import com.shopping.entity.OrderItem;
import com.shopping.entity.OrderStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "status", source = "orderStatus")
    @Mapping(target = "totalItems", expression = "java(order.getItems() != null ? order.getItems().size() : 0)")
    OrderSummaryResponse toSummaryResponse(Order order);

    List<OrderSummaryResponse> toSummaryResponseList(List<Order> orders);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "status", source = "orderStatus")
    @Mapping(target = "couponId", source = "coupon.couponId")
    @Mapping(target = "statusHistories", ignore = true)
    OrderDetailResponse toDetailResponse(Order order);

    @Mapping(target = "variantId", source = "variant.variantId")
    OrderItemResponse toItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> orderItems);

    OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history);

    List<OrderStatusHistoryResponse> toHistoryResponseList(List<OrderStatusHistory> histories);
}
