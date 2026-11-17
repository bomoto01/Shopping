package com.shopping.dto.response;

import com.shopping.enums.OrderStatus;
import com.shopping.enums.PaymentMethod;
import com.shopping.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailResponse {

    private Long orderId;
    private String orderCode;
    private Long userId;

    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;

    private String addressSnapshot;

    private Long couponId;
    private String couponCodeSnapshot;
    private String note;

    private Instant createdAt;
    private Instant updatedAt;

    private List<OrderItemResponse> items;
    private List<OrderStatusHistoryResponse> statusHistories;
}
