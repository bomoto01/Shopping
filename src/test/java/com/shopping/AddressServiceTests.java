package com.shopping;

import com.shopping.dto.request.user.AddressRequest;
import com.shopping.dto.response.user.AddressResponse;
import com.shopping.entity.Address;
import com.shopping.entity.User;
import com.shopping.enums.UserStatus;
import com.shopping.exception.ForbiddenException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.AddressMapper;
import com.shopping.repository.AddressRepository;
import com.shopping.repository.UserRepository;
import com.shopping.security.UserPrincipal;
import com.shopping.service.impl.AddressServiceImpl;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTests {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AddressMapper addressMapper;

    @InjectMocks
    private AddressServiceImpl addressService;

    private User currentUser;
    private User otherUser;
    private Address sampleAddress;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .userId(1L)
                .email("user1@example.com")
                .fullName("User One")
                .status(UserStatus.ACTIVE)
                .build();

        otherUser = User.builder()
                .userId(2L)
                .email("user2@example.com")
                .fullName("User Two")
                .status(UserStatus.ACTIVE)
                .build();

        sampleAddress = Address.builder()
                .addressId(10L)
                .user(currentUser)
                .recipientName("User One")
                .phone("0912345678")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .addressLine("Số 1 Cầu Giấy")
                .isDefault(true)
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
    @DisplayName("Thêm địa chỉ đầu tiên tự động đặt làm mặc định")
    void testCreateFirstAddressAutomaticallyDefault() {
        AddressRequest request = AddressRequest.builder()
                .recipientName("User One")
                .phone("0912345678")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .addressLine("Số 1 Cầu Giấy")
                .isDefault(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(addressRepository.countByUser_UserId(1L)).thenReturn(0L);

        Address mappedAddress = Address.builder().build();
        when(addressMapper.toAddress(request)).thenReturn(mappedAddress);
        when(addressRepository.save(any(Address.class))).thenReturn(sampleAddress);
        when(addressMapper.toAddressResponse(sampleAddress)).thenReturn(
                AddressResponse.builder().addressId(10L).isDefault(true).build()
        );

        AddressResponse response = addressService.createAddress(request);

        assertNotNull(response);
        assertTrue(response.getIsDefault());
        verify(addressRepository, times(1)).save(mappedAddress);
        assertTrue(mappedAddress.getIsDefault());
    }

    @Test
    @DisplayName("Chặn truy cập/thao tác trên địa chỉ của người khác")
    void testAccessDeniedToOtherUsersAddress() {
        Address foreignAddress = Address.builder()
                .addressId(99L)
                .user(otherUser)
                .build();

        when(addressRepository.findById(99L)).thenReturn(Optional.of(foreignAddress));

        assertThrows(ForbiddenException.class, () -> addressService.getAddressById(99L));
    }

    @Test
    @DisplayName("Lấy danh sách địa chỉ của người dùng hiện tại")
    void testGetMyAddresses() {
        when(addressRepository.findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(1L))
                .thenReturn(List.of(sampleAddress));
        when(addressMapper.toAddressResponseList(any())).thenReturn(
                List.of(AddressResponse.builder().addressId(10L).build())
        );

        List<AddressResponse> results = addressService.getMyAddresses();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(10L, results.get(0).getAddressId());
    }
}
