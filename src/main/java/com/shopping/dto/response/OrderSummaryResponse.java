package com.shopping.dto.response;

import com.shopping.enums.OrderStatus;
import com.shopping.enums.PaymentMethod;
import com.shopping.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSummaryResponse {

    private Long orderId;
    private String orderCode;
    private Long userId;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private String addressSnapshot;
    private Instant createdAt;
}
