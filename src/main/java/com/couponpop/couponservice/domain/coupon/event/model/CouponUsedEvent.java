package com.couponpop.couponservice.domain.coupon.event.model;


import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.couponhistory.service.dto.CouponUsedDto;

public record CouponUsedEvent(
        Long couponId,
        Long memberId,
        Long storeId,
        Long eventId,
        String eventName,
        CouponStatus couponStatus
) {

    public static CouponUsedEvent of(Long couponId, Long memberId, Long storeId, Long eventId, String eventName) {
        return new CouponUsedEvent(couponId, memberId, storeId, eventId, eventName, CouponStatus.USED);
    }

    public CouponUsedDto toCouponUsedDto() {
        return new CouponUsedDto(couponId, memberId, storeId, eventId, couponStatus);
    }
}
