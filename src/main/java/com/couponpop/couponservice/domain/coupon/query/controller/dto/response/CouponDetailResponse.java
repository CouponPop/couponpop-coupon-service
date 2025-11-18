package com.couponpop.couponservice.domain.coupon.query.controller.dto.response;

import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.couponservice.domain.coupon.common.entity.Coupon;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.common.repository.db.dto.CouponSummaryInfoProjection;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Optional;


public record CouponDetailResponse(
        Long id,
        CouponStatus status,
        LocalDateTime issuedAt,
        LocalDateTime expireAt,
        LocalDateTime usedAt,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        QrCode qrCode,
        EventInfoResponse event,
        StoreInfoResponse store
) {
    public static CouponDetailResponse from(Coupon coupon, Optional<String> tempCode, StoreResponse store) {
        return new CouponDetailResponse(
                coupon.getId(),
                coupon.getCouponStatus(),
                coupon.getReceivedAt(),
                coupon.getExpireAt(),
                coupon.getUsedAt(),
                tempCode.map(code -> new QrCode("https://couponpop.com/q/" + code, "사장님께 QR코드를 보여주세요"))
                        .orElse(null),
                EventInfoResponse.from(coupon.getCouponEvent()),
                StoreInfoResponse.from(store)
        );
    }

    public static CouponDetailResponse from(CouponSummaryInfoProjection coupon, StoreResponse store) {
        return new CouponDetailResponse(
                coupon.id(),
                coupon.status(),
                coupon.issuedAt(),
                coupon.expireAt(),
                coupon.usedAt(),
                null,
                EventInfoResponse.from(coupon.event()),
                StoreInfoResponse.from(store)
        );
    }

    public record QrCode(String url, String message) {
    }

}
