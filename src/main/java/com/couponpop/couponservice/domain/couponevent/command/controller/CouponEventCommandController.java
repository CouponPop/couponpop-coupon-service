package com.couponpop.couponservice.domain.couponevent.command.controller;

import com.couponpop.couponservice.common.response.ApiResponse;
import com.couponpop.couponservice.domain.couponevent.command.controller.dto.request.CreateCouponEventRequest;
import com.couponpop.couponservice.domain.couponevent.command.controller.dto.response.CreateCouponEventResponse;
import com.couponpop.couponservice.domain.couponevent.command.service.CouponEventCommandService;
import com.couponpop.security.annotation.CurrentMember;
import com.couponpop.security.dto.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CouponEventCommandController {

    private final CouponEventCommandService couponEventQueryService;

    @PostMapping("/api/v1/owner/coupons/events")
    public ResponseEntity<ApiResponse<CreateCouponEventResponse>> createCouponEvent(
            @RequestBody @Valid CreateCouponEventRequest request,
            @CurrentMember AuthMember authMember // TODO : 인증 세션 구현되면 변경
    ) {
        // TODO : 사장 권한 확인??
        CreateCouponEventResponse response = couponEventQueryService.createCouponEvent(request, authMember.id());
        return ApiResponse.created(response);
    }

    @PostMapping("/api-test/v1/owner/coupons/events")
    public ResponseEntity<ApiResponse<CreateCouponEventResponse>> createCouponEventForTest(
            @RequestBody @Valid CreateCouponEventRequest request,
            @RequestParam Long memberId
    ) {
        CreateCouponEventResponse response = couponEventQueryService.createCouponEvent(request, memberId);
        return ApiResponse.created(response);
    }
}
