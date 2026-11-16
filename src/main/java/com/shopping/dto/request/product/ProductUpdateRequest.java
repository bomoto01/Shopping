package com.shopping.dto.request.product;

import com.shopping.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
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
public class ProductUpdateRequest {

    @NotNull(message = "Danh mục sản phẩm không được để trống")
    private Long categoryId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 200, message = "Tên sản phẩm tối đa 200 ký tự")
    private String name;

    private String description;

    @Size(max = 100, message = "Tên thương hiệu tối đa 100 ký tự")
    private String brand;

    @NotNull(message = "Giá cơ sở không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá cơ sở phải lớn hơn hoặc bằng 0")
    private BigDecimal basePrice;

    private ProductStatus status;
}
