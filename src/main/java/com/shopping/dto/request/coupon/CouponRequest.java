package com.shopping.dto.request.coupon;

import com.shopping.enums.DiscountType;
import jakarta.validation.constraints.*;
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
public class CouponRequest {

    @NotBlank(message = "Mã giảm giá không được để trống")
    @Size(max = 40, message = "Mã giảm giá tối đa 40 ký tự")
    private String code;

    @NotNull(message = "Loại giảm giá không được để trống")
    private DiscountType discountType;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu không được âm")
    @Builder.Default
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private Instant startAt;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private Instant endAt;

    @Min(value = 0, message = "Giới hạn lượt sử dụng không được âm")
    @Builder.Default
    private Integer usageLimit = 0;

    @Builder.Default
    private Boolean isActive = true;
}
