package com.shopping.service.impl;

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
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.exception.UnauthorizedException;
import com.shopping.mapper.UserMapper;
import com.shopping.repository.RefreshTokenRepository;
import com.shopping.repository.RoleRepository;
import com.shopping.repository.UserRepository;
import com.shopping.security.JwtTokenProvider;
import com.shopping.security.UserPrincipal;
import com.shopping.service.interfaces.AuthService;
import com.shopping.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu xác nhận không khớp với mật khẩu đã nhập");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new DuplicateResourceException("Email này đã được sử dụng bởi một tài khoản khác");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone().trim())) {
            throw new DuplicateResourceException("Số điện thoại này đã được sử dụng bởi một tài khoản khác");
        }

        Role customerRole = roleRepository.findByRoleCode(RoleType.ROLE_CUSTOMER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleCode(RoleType.ROLE_CUSTOMER)
                        .roleName("Khách hàng")
                        .build()));

        User newUser = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .allowPersonalization(true)
                .roles(new HashSet<>(Set.of(customerRole)))
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Đăng ký tài khoản thành công: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());

        UserPrincipal principal = UserPrincipal.create(savedUser);
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String rawRefreshToken = jwtTokenProvider.generateRefreshTokenString();

        saveRefreshToken(savedUser, rawRefreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .user(userMapper.toUserSummaryResponse(savedUser))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(cleanEmail, request.getPassword())
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            User user = userRepository.findById(principal.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getUserId()));

            if (user.getStatus() == UserStatus.LOCKED) {
                throw new BadRequestException("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }

            String accessToken = jwtTokenProvider.generateAccessToken(principal);
            String rawRefreshToken = jwtTokenProvider.generateRefreshTokenString();

            saveRefreshToken(user, rawRefreshToken);

            log.info("Người dùng đăng nhập thành công: email={}", cleanEmail);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(rawRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                    .user(userMapper.toUserSummaryResponse(user))
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("Đăng nhập thất bại cho email: {}", cleanEmail);
            throw new BadCredentialsException("Email hoặc mật khẩu không chính xác");
        }
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken().trim();
        String tokenHash = jwtTokenProvider.hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token không hợp lệ hoặc không tồn tại"));

        if (!refreshToken.isValid()) {
            throw new UnauthorizedException("Refresh token đã hết hạn hoặc đã bị thu hồi. Vui lòng đăng nhập lại.");
        }

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Tài khoản người dùng đang bị khóa hoặc không hoạt động");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = jwtTokenProvider.generateAccessToken(principal);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshTokenString) {
        if (refreshTokenString != null && !refreshTokenString.isBlank()) {
            String tokenHash = jwtTokenProvider.hashToken(refreshTokenString.trim());
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                log.info("Thu hồi thành công refresh token: tokenId={}", token.getTokenId());
            });
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));
        return userMapper.toUserSummaryResponse(user);
    }

    private void saveRefreshToken(User user, String rawRefreshToken) {
        String tokenHash = jwtTokenProvider.hashToken(rawRefreshToken);
        Instant expiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }
}
