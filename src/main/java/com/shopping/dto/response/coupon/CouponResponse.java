package com.shopping.dto.response.coupon;

import com.shopping.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {

    private Long couponId;
    private String code;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderValue;
    private Instant startAt;
    private Instant endAt;
    private Integer usageLimit;
    private Integer usedCount;
    private Boolean isActive;
    private Boolean isExpired;
    private Boolean isAvailable;
}
