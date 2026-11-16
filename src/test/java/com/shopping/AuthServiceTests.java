package com.shopping;

import com.shopping.dto.request.auth.LoginRequest;
import com.shopping.dto.request.auth.RefreshTokenRequest;
import com.shopping.dto.request.auth.RegisterRequest;
import com.shopping.dto.response.auth.AuthResponse;
import com.shopping.dto.response.auth.TokenRefreshResponse;
import com.shopping.dto.response.auth.UserSummaryResponse;
import com.shopping.entity.RefreshToken;
import com.shopping.entity.Role;
import com.shopping.entity.User;
import com.shopping.enums.RoleType;
import com.shopping.enums.UserStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.DuplicateResourceException;
import com.shopping.exception.UnauthorizedException;
import com.shopping.mapper.UserMapper;
import com.shopping.repository.RefreshTokenRepository;
import com.shopping.repository.RoleRepository;
import com.shopping.repository.UserRepository;
import com.shopping.security.JwtTokenProvider;
import com.shopping.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role customerRole;
    private User testUser;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder()
                .roleId(1)
                .roleCode(RoleType.ROLE_CUSTOMER)
                .roleName("Khách hàng")
                .build();

        testUser = User.builder()
                .userId(1L)
                .fullName("Nguyễn Văn A")
                .email("test@example.com")
                .phone("0912345678")
                .passwordHash("encoded_password")
                .status(UserStatus.ACTIVE)
                .allowPersonalization(true)
                .roles(Set.of(customerRole))
                .build();
    }

    @Test
    @DisplayName("TC-01: Đăng ký thất bại khi email đã tồn tại")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Nguyễn Văn A")
                .email("test@example.com")
                .phone("0912345678")
                .password("Password123")
                .confirmPassword("Password123")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-02: Đăng ký thất bại khi mật khẩu xác nhận không khớp")
    void testRegisterPasswordMismatch() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Nguyễn Văn A")
                .email("test@example.com")
                .phone("0912345678")
                .password("Password123")
                .confirmPassword("Mismatch123")
                .build();

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký thành công và phát hành token")
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Nguyễn Văn A")
                .email("test@example.com")
                .phone("0912345678")
                .password("Password123")
                .confirmPassword("Password123")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("0912345678")).thenReturn(false);
        when(roleRepository.findByRoleCode(RoleType.ROLE_CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("Password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("mock_access_token");
        when(jwtTokenProvider.generateRefreshTokenString()).thenReturn("mock_raw_refresh_token");
        when(jwtTokenProvider.hashToken("mock_raw_refresh_token")).thenReturn("mock_hash");
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(userMapper.toUserSummaryResponse(any(User.class))).thenReturn(
                UserSummaryResponse.builder().userId(1L).email("test@example.com").build()
        );

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock_access_token", response.getAccessToken());
        assertEquals("mock_raw_refresh_token", response.getRefreshToken());
        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("TC-03: Làm mới token thất bại khi Refresh Token bị thu hồi (Revoked)")
    void testRefreshTokenRevoked() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("revoked_refresh_token")
                .build();

        RefreshToken revokedToken = RefreshToken.builder()
                .tokenId(1L)
                .user(testUser)
                .tokenHash("hash_revoked")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(jwtTokenProvider.hashToken("revoked_refresh_token")).thenReturn("hash_revoked");
        when(refreshTokenRepository.findByTokenHash("hash_revoked")).thenReturn(Optional.of(revokedToken));

        assertThrows(UnauthorizedException.class, () -> authService.refreshToken(request));
    }

    @Test
    @DisplayName("Làm mới token thành công khi Refresh Token hợp lệ")
    void testRefreshTokenSuccess() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("valid_refresh_token")
                .build();

        RefreshToken validToken = RefreshToken.builder()
                .tokenId(1L)
                .user(testUser)
                .tokenHash("hash_valid")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(jwtTokenProvider.hashToken("valid_refresh_token")).thenReturn("hash_valid");
        when(refreshTokenRepository.findByTokenHash("hash_valid")).thenReturn(Optional.of(validToken));
        when(jwtTokenProvider.generateAccessToken(any())).thenReturn("new_access_token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);

        TokenRefreshResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new_access_token", response.getAccessToken());
    }

    @Test
    @DisplayName("Đăng xuất thu hồi Refresh Token trên máy chủ")
    void testLogoutRevokesToken() {
        String rawToken = "raw_token_to_logout";
        RefreshToken activeToken = RefreshToken.builder()
                .tokenId(1L)
                .tokenHash("hash_logout")
                .revoked(false)
                .build();

        when(jwtTokenProvider.hashToken(rawToken)).thenReturn("hash_logout");
        when(refreshTokenRepository.findByTokenHash("hash_logout")).thenReturn(Optional.of(activeToken));

        authService.logout(rawToken);

        assertTrue(activeToken.getRevoked());
        verify(refreshTokenRepository, times(1)).save(activeToken);
    }
}
