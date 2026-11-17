package com.shopping.dto.request;

import com.shopping.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotNull(message = "Địa chỉ giao hàng không được để trống")
    @Schema(description = "ID địa chỉ nhận hàng đã lưu trong sổ địa chỉ", example = "1")
    private Long addressId;

    @Schema(description = "Mã giảm giá (nếu có)", example = "SUMMER2026")
    private String couponCode;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Schema(description = "Phương thức thanh toán", example = "COD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Ghi chú giao hàng", example = "Giao giờ hành chính giúp mình")
    private String note;
}
