package com.couponpop.couponservice.domain.coupon.query.controller;

import com.couponpop.couponservice.common.response.ApiResponse;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.query.controller.dto.request.MemberIssuedCouponCursor;
import com.couponpop.couponservice.domain.coupon.query.controller.dto.response.CouponDetailResponse;
import com.couponpop.couponservice.domain.coupon.query.controller.dto.response.IssuedCouponListResponse;
import com.couponpop.couponservice.domain.coupon.query.service.CouponQueryService;
import com.couponpop.security.annotation.CurrentMember;
import com.couponpop.security.dto.AuthMember;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class CouponQueryController {

    private final CouponQueryService couponQueryService;

    @GetMapping("/coupons/{couponId}")
    public ResponseEntity<ApiResponse<CouponDetailResponse>> getCouponDetail(@PathVariable Long couponId, @CurrentMember AuthMember authMember) {
        CouponDetailResponse response = couponQueryService.getCouponDetail(couponId, authMember.id());
        return ApiResponse.success(response);
    }

    @GetMapping("/coupons")
    public ResponseEntity<ApiResponse<IssuedCouponListResponse>> getIssuedCoupons(
            @RequestParam CouponStatus type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastEventEndAt,
            @RequestParam(required = false) Long lastCouponId,
            @RequestParam(defaultValue = "10") int size,
            @CurrentMember AuthMember authMember
    ) {
        var cursor = MemberIssuedCouponCursor.ofNullable(lastEventEndAt, lastCouponId);
        IssuedCouponListResponse response = couponQueryService.getIssuedCoupons(authMember.id(), type, cursor, size);
        return ApiResponse.success(response);
    }
}
