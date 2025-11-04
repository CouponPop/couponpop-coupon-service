package com.couponpop.couponservice.domain.coupon.event;


import com.couponpop.couponservice.domain.coupon.enums.CouponStatus;
import com.couponpop.couponservice.domain.couponhistory.service.dto.CouponUsedDto;

public record CouponUsedEvent(
        Long couponId,
        Long memberId,
        Long storeId,
        Long eventId,
        CouponStatus couponStatus
) {

    public static CouponUsedEvent of(Long couponId, Long memberId, Long storeId, Long eventId) {
        return new CouponUsedEvent(couponId, memberId, storeId, eventId, CouponStatus.USED);
    }

    public CouponUsedDto toCouponUsedDto() {
        return new CouponUsedDto(couponId, memberId, storeId, eventId, couponStatus);
    }
}
