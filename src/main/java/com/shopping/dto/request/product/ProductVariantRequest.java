package com.shopping.dto.request.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {

    @NotBlank(message = "Mã SKU không được để trống")
    @Size(max = 60, message = "Mã SKU tối đa 60 ký tự")
    private String sku;

    @NotBlank(message = "Kích cỡ không được để trống")
    @Size(max = 20, message = "Kích cỡ tối đa 20 ký tự")
    private String size;

    @NotBlank(message = "Màu sắc không được để trống")
    @Size(max = 50, message = "Màu sắc tối đa 50 ký tự")
    private String color;

    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @NotNull(message = "Số lượng tồn kho không được để trống")
    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Builder.Default
    private Boolean isActive = true;
}
