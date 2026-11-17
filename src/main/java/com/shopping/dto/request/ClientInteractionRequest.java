package com.shopping.dto.request;

import com.shopping.enums.InteractionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientInteractionRequest {

    @NotNull(message = "ID sản phẩm không được để trống")
    @Schema(description = "ID sản phẩm người dùng tương tác", example = "10")
    private Long productId;

    @NotNull(message = "Loại tương tác không được để trống")
    @Schema(description = "Loại tương tác từ client (CHỈ CHẤP NHẬN 'VIEW' HOẶC 'CLICK' theo TC-11 Anti-Spoofing)", example = "VIEW")
    private InteractionType interactionType;

    @Schema(description = "Thời gian xem (giây) hoặc giá trị tương tác", example = "15.5")
    private Double interactionValue;

    @Schema(description = "ID phiên người dùng (session id)", example = "sess_98234ab1")
    private String sessionId;
}
