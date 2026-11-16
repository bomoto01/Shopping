package com.shopping.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private Boolean allowPersonalization;
    private String status;
    private Set<String> roles;
    private Instant createdAt;
    private Instant updatedAt;
    private List<AddressResponse> addresses;
}
