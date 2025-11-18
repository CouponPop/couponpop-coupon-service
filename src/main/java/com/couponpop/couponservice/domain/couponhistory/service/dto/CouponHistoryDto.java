package com.couponpop.couponservice.domain.couponhistory.service.dto;


import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;

public record CouponHistoryDto(
        Long couponId,
        Long memberId,
        Long storeId,
        Long eventId,
        CouponStatus couponStatus
) {
}
