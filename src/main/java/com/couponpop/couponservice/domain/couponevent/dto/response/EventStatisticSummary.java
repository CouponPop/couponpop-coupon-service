package com.couponpop.couponservice.domain.couponevent.dto.response;

import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;

public record EventStatisticSummary(
        int total, // 총 발급 수량
        int unclaimed, // 미수령 개수
        int issued, // 수령 개수
        int used, // 사용 개수
        int unused // 미사용 개수

) {
    public static EventStatisticSummary of(CouponEvent couponEvent, int usedCouponCount) {
        int total = couponEvent.getTotalCount();
        int issued = couponEvent.getIssuedCount();
        int unused = issued - usedCouponCount;
        int unclaimed = total - issued;

        return new EventStatisticSummary(
                total,
                unclaimed,
                issued,
                usedCouponCount,
                unused
        );
    }

    public static EventStatisticSummary of(int total, int issued, int usedCouponCount) {
        int unused = issued - usedCouponCount;
        int unclaimed = total - issued;

        return new EventStatisticSummary(
                total,
                unclaimed,
                issued,
                usedCouponCount,
                unused
        );
    }
}
