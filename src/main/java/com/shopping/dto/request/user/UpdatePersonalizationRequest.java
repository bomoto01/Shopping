package com.shopping.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePersonalizationRequest {

    @NotNull(message = "Lựa chọn cá nhân hóa không được để trống")
    private Boolean allowPersonalization;
}
