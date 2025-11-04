package com.couponpop.couponservice.domain.couponevent.dto.response;

import java.time.LocalDateTime;

public record EventPeriod(
        LocalDateTime start,
        LocalDateTime end
) {
    public static EventPeriod of(LocalDateTime start, LocalDateTime end) {
        return new EventPeriod(start, end);
    }
}
