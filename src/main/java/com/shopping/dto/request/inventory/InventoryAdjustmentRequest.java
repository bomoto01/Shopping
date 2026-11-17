package com.shopping.dto.request.inventory;

import com.shopping.enums.InventoryReferenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentRequest {

    @NotNull(message = "Số lượng thay đổi không được để trống")
    private Integer quantityChange;

    @NotBlank(message = "Lý do thay đổi tồn kho không được để trống")
    @Size(max = 255, message = "Lý do thay đổi tối đa 255 ký tự")
    private String reason;

    @NotNull(message = "Loại tham chiếu tồn kho không được để trống")
    private InventoryReferenceType referenceType;

    private Long referenceId;
}
