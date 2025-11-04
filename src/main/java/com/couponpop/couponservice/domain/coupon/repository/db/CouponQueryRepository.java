package com.couponpop.couponservice.domain.coupon.repository.db;

import com.couponpop.couponservice.domain.coupon.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.repository.db.dto.CouponSummaryInfoProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponQueryRepository {

    List<CouponSummaryInfoProjection> findAllByMemberIdWithEventAndStore(Long memberId, CouponStatus status, LocalDateTime lastEventEndAt, Long lastCouponId, int limit);
}
