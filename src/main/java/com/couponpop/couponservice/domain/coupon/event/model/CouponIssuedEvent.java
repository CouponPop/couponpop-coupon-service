package com.couponpop.couponservice.domain.coupon.event.model;


import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.couponhistory.service.dto.CouponHistoryDto;

public record CouponIssuedEvent(
        Long couponId,
        Long memberId,
        Long storeId,
        Long eventId,
        int totalCount,
        int issuedCount,
        String eventName,
        CouponStatus couponStatus
) {

    public static CouponIssuedEvent of(Long couponId, Long memberId, Long storeId, Long eventId, int totalCount, int issuedCount, String eventName) {
        return new CouponIssuedEvent(couponId, memberId, storeId, eventId, totalCount, issuedCount, eventName, CouponStatus.ISSUED);
    }

    public CouponHistoryDto toCouponUsedDto() {
        return new CouponHistoryDto(couponId, memberId, storeId, eventId, couponStatus);
    }

}
