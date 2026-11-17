package com.shopping.dto.request;

import com.shopping.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "Trạng thái mới không được để trống")
    @Schema(description = "Trạng thái đơn hàng cần cập nhật", example = "CONFIRMED")
    private OrderStatus status;

    @Schema(description = "Ghi chú lý do thay đổi trạng thái", example = "Đã xác nhận thanh toán qua ngân hàng")
    private String note;
}
