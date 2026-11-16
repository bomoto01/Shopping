package com.shopping.service.interfaces;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.user.ChangePasswordRequest;
import com.shopping.dto.request.user.UpdatePersonalizationRequest;
import com.shopping.dto.request.user.UpdateProfileRequest;
import com.shopping.dto.request.user.UserStatusUpdateRequest;
import com.shopping.dto.response.user.UserProfileResponse;
import com.shopping.enums.UserStatus;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserProfileResponse getMyProfile();

    UserProfileResponse updateMyProfile(UpdateProfileRequest request);

    void changePassword(ChangePasswordRequest request);

    void updatePersonalization(UpdatePersonalizationRequest request);

    PageResponse<UserProfileResponse> getAllUsers(String keyword, UserStatus status, Pageable pageable);

    UserProfileResponse updateUserStatus(Long userId, UserStatusUpdateRequest request);
}
