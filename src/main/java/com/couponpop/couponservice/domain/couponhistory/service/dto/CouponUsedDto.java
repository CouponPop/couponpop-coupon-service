package com.couponpop.couponservice.domain.couponhistory.service.dto;


import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;

public record CouponUsedDto(
        Long couponId,
        Long memberId,
        Long storeId,
        Long eventId,
        CouponStatus couponStatus
) {
}
