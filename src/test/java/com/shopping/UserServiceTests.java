package com.shopping;

import com.shopping.dto.request.user.ChangePasswordRequest;
import com.shopping.dto.request.user.UpdatePersonalizationRequest;
import com.shopping.dto.request.user.UpdateProfileRequest;
import com.shopping.dto.request.user.UserStatusUpdateRequest;
import com.shopping.dto.response.user.UserProfileResponse;
import com.shopping.entity.Role;
import com.shopping.entity.User;
import com.shopping.enums.RoleType;
import com.shopping.enums.UserStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.mapper.AddressMapper;
import com.shopping.mapper.UserMapper;
import com.shopping.repository.AddressRepository;
import com.shopping.repository.RefreshTokenRepository;
import com.shopping.repository.UserRepository;
import com.shopping.security.UserPrincipal;
import com.shopping.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressRepository addressRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User currentUser;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
                .roleId(1)
                .roleCode(RoleType.ROLE_ADMIN)
                .roleName("Quản trị viên")
                .build();

        currentUser = User.builder()
                .userId(1L)
                .email("admin@shopping.vn")
                .fullName("Quản Trị Viên")
                .phone("0988888888")
                .passwordHash("old_encoded_hash")
                .status(UserStatus.ACTIVE)
                .allowPersonalization(true)
                .roles(Set.of(adminRole))
                .build();

        UserPrincipal principal = UserPrincipal.create(currentUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("TC-10: Cập nhật cài đặt quyền riêng tư AI (Opt-out)")
    void testUpdatePersonalizationOptOut() {
        UpdatePersonalizationRequest request = UpdatePersonalizationRequest.builder()
                .allowPersonalization(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));

        userService.updatePersonalization(request);

        assertFalse(currentUser.getAllowPersonalization());
        verify(userRepository, times(1)).save(currentUser);
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại khi mật khẩu hiện tại sai")
    void testChangePasswordWrongCurrentPassword() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongPassword")
                .newPassword("NewPassword123")
                .confirmNewPassword("NewPassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.matches("WrongPassword", "old_encoded_hash")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> userService.changePassword(request));
        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    @DisplayName("Đổi mật khẩu thành công và thu hồi tất cả refresh tokens")
    void testChangePasswordSuccess() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("OldPassword123")
                .newPassword("NewPassword123")
                .confirmNewPassword("NewPassword123")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(passwordEncoder.matches("OldPassword123", "old_encoded_hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword123", "old_encoded_hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("new_encoded_hash");

        userService.changePassword(request);

        assertEquals("new_encoded_hash", currentUser.getPasswordHash());
        verify(userRepository, times(1)).save(currentUser);
        verify(refreshTokenRepository, times(1)).revokeAllByUserId(1L);
    }

    @Test
    @DisplayName("Chặn Admin tự khóa tài khoản của chính mình")
    void testAdminCannotLockSelf() {
        UserStatusUpdateRequest request = UserStatusUpdateRequest.builder()
                .status(UserStatus.LOCKED)
                .build();

        assertThrows(BadRequestException.class, () -> userService.updateUserStatus(1L, request));
        verify(userRepository, never()).save(any());
    }
}
