package com.couponpop.couponservice.domain.couponhistory.service.dto;


import com.couponpop.couponservice.domain.coupon.enums.CouponStatus;

public record CouponUsedDto(
        Long couponId,
        Long memberId,
        Long storeId,
        Long eventId,
        CouponStatus couponStatus
) {
}
