package com.shopping.service.impl;

import com.shopping.dto.request.user.AddressRequest;
import com.shopping.dto.response.user.AddressResponse;
import com.shopping.entity.Address;
import com.shopping.entity.User;
import com.shopping.exception.ForbiddenException;
import com.shopping.exception.ResourceNotFoundException;
import com.shopping.mapper.AddressMapper;
import com.shopping.repository.AddressRepository;
import com.shopping.repository.UserRepository;
import com.shopping.service.interfaces.AddressService;
import com.shopping.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<Address> addresses = addressRepository.findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(currentUserId);
        return addressMapper.toAddressResponseList(addresses);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Address address = findAndVerifyOwnership(addressId, currentUserId);
        return addressMapper.toAddressResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse createAddress(AddressRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        long currentCount = addressRepository.countByUser_UserId(currentUserId);
        boolean shouldBeDefault = Boolean.TRUE.equals(request.getIsDefault()) || currentCount == 0;

        if (shouldBeDefault && currentCount > 0) {
            addressRepository.resetDefaultAddressForUser(currentUserId);
        }

        Address address = addressMapper.toAddress(request);
        address.setUser(currentUser);
        address.setIsDefault(shouldBeDefault);

        Address savedAddress = addressRepository.save(address);
        log.info("Tạo địa chỉ mới thành công: addressId={}, userId={}", savedAddress.getAddressId(), currentUserId);

        return addressMapper.toAddressResponse(savedAddress);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Address address = findAndVerifyOwnership(addressId, currentUserId);

        boolean willBeDefault = Boolean.TRUE.equals(request.getIsDefault());
        if (willBeDefault && !Boolean.TRUE.equals(address.getIsDefault())) {
            addressRepository.resetDefaultAddressForUser(currentUserId);
        }

        addressMapper.updateAddressFromRequest(request, address);
        address.setIsDefault(willBeDefault || Boolean.TRUE.equals(address.getIsDefault()));

        Address updatedAddress = addressRepository.save(address);
        log.info("Cập nhật địa chỉ thành công: addressId={}, userId={}", addressId, currentUserId);

        return addressMapper.toAddressResponse(updatedAddress);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Address address = findAndVerifyOwnership(addressId, currentUserId);

        addressRepository.resetDefaultAddressForUser(currentUserId);
        address.setIsDefault(true);
        Address savedAddress = addressRepository.save(address);

        log.info("Đặt địa chỉ mặc định thành công: addressId={}, userId={}", addressId, currentUserId);
        return addressMapper.toAddressResponse(savedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Address address = findAndVerifyOwnership(addressId, currentUserId);

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        addressRepository.delete(address);
        log.info("Xóa địa chỉ thành công: addressId={}, userId={}", addressId, currentUserId);

        if (wasDefault) {
            List<Address> remaining = addressRepository.findByUser_UserIdOrderByIsDefaultDescCreatedAtDesc(currentUserId);
            if (!remaining.isEmpty()) {
                Address newDefault = remaining.get(0);
                newDefault.setIsDefault(true);
                addressRepository.save(newDefault);
                log.info("Tự động gán địa chỉ mặc định mới: addressId={}", newDefault.getAddressId());
            }
        }
    }

    private Address findAndVerifyOwnership(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ", "id", addressId));

        if (!address.getUser().getUserId().equals(userId)) {
            throw new ForbiddenException("Bạn không có quyền thao tác trên địa chỉ này");
        }

        return address;
    }
}
