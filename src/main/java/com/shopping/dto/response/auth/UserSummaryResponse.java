package com.shopping.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private Boolean allowPersonalization;
    private String status;
    private Set<String> roles;
}
