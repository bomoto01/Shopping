package com.shopping.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    @NotNull(message = "ID dòng đơn hàng (orderItemId) không được để trống")
    @Schema(description = "ID dòng sản phẩm trong đơn hàng đã hoàn tất giao hàng", example = "10")
    private Long orderItemId;

    @NotNull(message = "Điểm đánh giá không được để trống")
    @Min(value = 1, message = "Điểm đánh giá tối thiểu là 1 sao")
    @Max(value = 5, message = "Điểm đánh giá tối đa là 5 sao")
    @Schema(description = "Số sao đánh giá từ 1 đến 5", example = "5")
    private Integer rating;

    @Size(max = 1000, message = "Nội dung đánh giá không được vượt quá 1000 ký tự")
    @Schema(description = "Nội dung nhận xét chi tiết về sản phẩm", example = "Chất vải cotton rất mịn và thoáng mát, form áo chuẩn đẹp!")
    private String comment;
}
