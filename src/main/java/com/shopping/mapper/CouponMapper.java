package com.shopping.mapper;

import com.shopping.dto.request.coupon.CouponRequest;
import com.shopping.dto.response.coupon.CouponResponse;
import com.shopping.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    @Mapping(target = "isExpired", expression = "java(coupon.isExpired())")
    @Mapping(target = "isAvailable", expression = "java(coupon.isUsable())")
    CouponResponse toResponse(Coupon coupon);

    List<CouponResponse> toResponseList(List<Coupon> coupons);

    @Mapping(target = "couponId", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    Coupon toEntity(CouponRequest request);

    @Mapping(target = "couponId", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    void updateEntityFromRequest(CouponRequest request, @MappingTarget Coupon coupon);
}
