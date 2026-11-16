package com.shopping.service.interfaces;

import com.shopping.dto.request.user.AddressRequest;
import com.shopping.dto.response.user.AddressResponse;

import java.util.List;

public interface AddressService {

    List<AddressResponse> getMyAddresses();

    AddressResponse getAddressById(Long addressId);

    AddressResponse createAddress(AddressRequest request);

    AddressResponse updateAddress(Long addressId, AddressRequest request);

    AddressResponse setDefaultAddress(Long addressId);

    void deleteAddress(Long addressId);
}
