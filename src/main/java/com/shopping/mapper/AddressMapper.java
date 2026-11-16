package com.shopping.mapper;

import com.shopping.dto.request.user.AddressRequest;
import com.shopping.dto.response.user.AddressResponse;
import com.shopping.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponse toAddressResponse(Address address);

    List<AddressResponse> toAddressResponseList(List<Address> addresses);

    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toAddress(AddressRequest request);

    @Mapping(target = "addressId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAddressFromRequest(AddressRequest request, @MappingTarget Address address);
}
