package com.couponpop.couponservice.domain.coupon.query.controller.dto.response;

import java.time.LocalDateTime;

public record EventPeriodResponse(LocalDateTime startAt, LocalDateTime endAt) {
}
