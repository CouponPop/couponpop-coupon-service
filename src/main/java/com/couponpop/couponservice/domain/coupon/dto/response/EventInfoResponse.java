package com.couponpop.couponservice.domain.coupon.dto.response;

import com.couponpop.couponservice.domain.coupon.repository.db.dto.CouponSummaryInfoProjection;
import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;

public record EventInfoResponse(
        Long id,
        String name,
        EventPeriodResponse period
) {
    public static EventInfoResponse from(CouponEvent event) {
        return new EventInfoResponse(
                event.getId(),
                event.getName(),
                new EventPeriodResponse(
                        event.getEventStartAt(),
                        event.getEventEndAt()
                )
        );
    }

    public static EventInfoResponse from(CouponSummaryInfoProjection.EventInfo event) {
        return new EventInfoResponse(
                event.id(),
                event.name(),
                new EventPeriodResponse(
                        event.period().start(),
                        event.period().end()
                )
        );
    }
}
