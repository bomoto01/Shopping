package com.shopping.mapper;

import com.shopping.dto.response.UserInteractionResponse;
import com.shopping.entity.UserInteraction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserInteractionMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    UserInteractionResponse toResponse(UserInteraction interaction);

    List<UserInteractionResponse> toResponseList(List<UserInteraction> interactions);
}
