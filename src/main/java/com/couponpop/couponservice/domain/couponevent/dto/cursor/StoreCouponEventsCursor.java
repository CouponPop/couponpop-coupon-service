package com.couponpop.couponservice.domain.couponevent.dto.cursor;

import java.time.LocalDateTime;

public record StoreCouponEventsCursor(
        LocalDateTime lastStartAt,
        LocalDateTime lastEndAt,
        Long lastEventId
) {

    public static StoreCouponEventsCursor first() {
        return new StoreCouponEventsCursor(null, null, null);
    }

    public static StoreCouponEventsCursor ofNullable(LocalDateTime lastStartAt, LocalDateTime lastEndAt, Long lastEventId) {
        return (lastStartAt != null && lastEndAt != null && lastEventId != null) ?
                new StoreCouponEventsCursor(lastStartAt, lastEndAt, lastEventId) :
                StoreCouponEventsCursor.first();
    }
}
