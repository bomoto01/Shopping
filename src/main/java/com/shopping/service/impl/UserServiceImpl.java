package com.shopping.service.impl;

import com.shopping.apiResponse.PageResponse;
import com.shopping.dto.request.user.ChangePasswordRequest;
import com.shopping.dto.request.user.UpdatePersonalizationRequest;
import com.shopping.dto.request.user.UpdateProfileRequest;
import com.shopping.dto.request.user.UserStatusUpdateRequest;
import com.shopping.dto.response.user.AddressResponse;
import com.shopping.dto.response.user.UserProfileResponse;
import com.shopping.entity.Address;
import com.shopping.entity.User;
import com.shopping.enums.RoleType;
import com.shopping.enums.UserStatus;
import com.shopping.exception.BadRequestException;
import com.shopping.exception.DuplicateResourceException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.AddressMapper;
import com.shopping.mapper.UserMapper;
import com.shopping.repository.AddressRepository;
import com.shopping.repository.RefreshTokenRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.interfaces.UserService;
import com.shopping.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        return buildProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateMyProfile(UpdateProfileRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String newPhone = request.getPhone().trim();
            if (!newPhone.equals(user.getPhone()) && userRepository.existsByPhone(newPhone)) {
                throw new DuplicateResourceException("Số điện thoại này đã được sử dụng bởi tài khoản khác");
            }
            user.setPhone(newPhone);
        }

        user.setFullName(request.getFullName().trim());
        User savedUser = userRepository.save(user);

        log.info("Cập nhật hồ sơ cá nhân thành công: userId={}", currentUserId);
        return buildProfileResponse(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu hiện tại không chính xác");
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        refreshTokenRepository.revokeAllByUserId(currentUserId);
        log.info("Đổi mật khẩu thành công và thu hồi các phiên đăng nhập cũ: userId={}", currentUserId);
    }

    @Override
    @Transactional
    public void updatePersonalization(UpdatePersonalizationRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        user.setAllowPersonalization(request.getAllowPersonalization());
        userRepository.save(user);

        log.info("Cập nhật cài đặt quyền riêng tư AI cá nhân hóa: userId={}, allowPersonalization={}",
                currentUserId, request.getAllowPersonalization());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserProfileResponse> getAllUsers(String keyword, UserStatus status, Pageable pageable) {
        Page<User> usersPage = userRepository.searchUsers(keyword, status, pageable);
        Page<UserProfileResponse> responsePage = usersPage.map(this::buildProfileResponse);
        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserStatus(Long userId, UserStatusUpdateRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();

        if (currentUserId.equals(userId) && request.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("Không thể tự khóa tài khoản quản trị của chính mình");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        targetUser.setStatus(request.getStatus());
        User updatedUser = userRepository.save(targetUser);

        if (request.getStatus() != UserStatus.ACTIVE) {
            int revokedCount = refreshTokenRepository.revokeAllByUserId(userId);
            log.info("Khóa tài khoản userId={}, thu hồi {} refresh tokens", userId, revokedCount);
        }

        return buildProfileResponse(updatedUser);
    }

    private UserProfileResponse buildProfileResponse(User user) {
        UserProfileResponse response = userMapper.toUserProfileResponse(user);
        List<Address> addresses = addressRepository.findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(user.getUserId());
        List<AddressResponse> addressResponses = addressMapper.toAddressResponseList(addresses);
        response.setAddresses(addressResponses);
        return response;
    }
}
