package com.couponpop.couponservice.domain.coupon.query.controller.dto.request;

import java.time.LocalDateTime;

public record MemberIssuedCouponCursor(
        LocalDateTime lastEventEndAt,
        Long lastCouponId
) {
    public static MemberIssuedCouponCursor first() {
        return new MemberIssuedCouponCursor(null, null);
    }

    public static MemberIssuedCouponCursor ofNullable(LocalDateTime lastEventEndAt, Long lastCouponId) {
        return (lastEventEndAt != null && lastCouponId != null) ?
                new MemberIssuedCouponCursor(lastEventEndAt, lastCouponId) :
                MemberIssuedCouponCursor.first();
    }

}
