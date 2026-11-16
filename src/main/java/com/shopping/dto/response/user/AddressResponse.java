package com.shopping.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private Long addressId;
    private String recipientName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String addressLine;
    private Boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;
}
