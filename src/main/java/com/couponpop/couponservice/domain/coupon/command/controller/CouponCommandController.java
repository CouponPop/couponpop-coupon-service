package com.couponpop.couponservice.domain.coupon.command.controller;

import com.couponpop.couponservice.common.response.ApiResponse;
import com.couponpop.couponservice.domain.coupon.command.service.CouponCommandService;
import com.couponpop.couponservice.domain.coupon.command.controller.dto.request.CouponIssueRequest;
import com.couponpop.couponservice.domain.coupon.command.controller.dto.request.UseCouponRequest;
import com.couponpop.couponservice.domain.coupon.command.service.coupon_issue.CouponIssueFacade;
import com.couponpop.security.annotation.CurrentMember;
import com.couponpop.security.dto.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CouponCommandController {

    private final CouponCommandService couponCommandService;
    private final CouponIssueFacade couponIssueFacade;

    @PostMapping("/api/v1/coupons/issue")
    public ResponseEntity<ApiResponse<Void>> issueCoupon(@Valid @RequestBody CouponIssueRequest request, @CurrentMember AuthMember authMember) throws InterruptedException {
        LocalDateTime issuedTime = LocalDateTime.now();
        couponIssueFacade.issueCoupon(authMember.id(), request.eventId(), issuedTime);
        return ApiResponse.noContent();
    }

    @PostMapping("/api/v1/coupons/use")
    public ResponseEntity<ApiResponse<Void>> useCoupon(@Valid @RequestBody UseCouponRequest request, @CurrentMember AuthMember authMember) {
        LocalDateTime usedAt = LocalDateTime.now();
        couponCommandService.useCoupon(request.storeId(), request.couponId(), request.qrCode(), authMember.id(), usedAt);
        return ApiResponse.noContent();
    }

    @PostMapping("/api-test/v1/coupons/issue")
    public ResponseEntity<ApiResponse<Void>> issueCouponTest(@Valid @RequestBody CouponIssueRequest request, @RequestParam Long memberId) throws InterruptedException {
        LocalDateTime issuedTime = LocalDateTime.now();
        couponIssueFacade.issueCoupon(memberId, request.eventId(), issuedTime);
        return ApiResponse.noContent();
    }

}
