package com.shopping.mapper;

import com.shopping.dto.response.ReviewResponse;
import com.shopping.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "orderItemId", source = "orderItem.orderItemId")
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userFullName", source = "user.fullName")
    ReviewResponse toResponse(Review review);

    List<ReviewResponse> toResponseList(List<Review> reviews);
}
