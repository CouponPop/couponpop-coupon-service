package com.couponpop.couponservice.domain.couponevent.controller;

import com.couponpop.couponpopcoremodule.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.couponservice.common.response.ApiResponse;
import com.couponpop.couponservice.domain.couponevent.dto.cursor.StoreCouponEventsCursor;
import com.couponpop.couponservice.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.couponpop.couponservice.domain.couponevent.dto.response.CouponEventDetailResponse;
import com.couponpop.couponservice.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.couponpop.couponservice.domain.couponevent.dto.response.StoreCouponEventListResponse;
import com.couponpop.couponservice.domain.couponevent.dto.response.StoreCouponEventStatisticsResponse;
import com.couponpop.couponservice.domain.couponevent.enums.CouponEventStatus;
import com.couponpop.couponservice.domain.couponevent.service.CouponEventService;
import com.couponpop.security.annotation.CurrentMember;
import com.couponpop.security.dto.AuthMember;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CouponEventController {

    private final CouponEventService couponEventService;

    @PostMapping("/owner/coupons/events")
    public ResponseEntity<ApiResponse<CreateCouponEventResponse>> createCouponEvent(
            @RequestBody @Valid CreateCouponEventRequest request,
            @CurrentMember AuthMember authMember // TODO : 인증 세션 구현되면 변경
    ) {
        // TODO : 사장 권한 확인??
        CreateCouponEventResponse response = couponEventService.createCouponEvent(request, authMember.id());
        return ApiResponse.created(response);
    }

    @GetMapping("/owner/coupons/events/{eventId}")
    public ResponseEntity<ApiResponse<CouponEventDetailResponse>> getCouponEvent(@PathVariable Long eventId, @CurrentMember AuthMember authMember) {
        CouponEventDetailResponse response = couponEventService.getCouponEvent(eventId, authMember.id());
        return ApiResponse.success(response);
    }

    @GetMapping("/owner/coupons/events")
    public ResponseEntity<ApiResponse<StoreCouponEventListResponse>> getCouponEventsByStore(
            @RequestParam Long storeId,
            @RequestParam(defaultValue = "IN_PROGRESS", required = false) CouponEventStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastStartAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastEndAt,
            @RequestParam(required = false) Long lastEventId,
            @RequestParam(defaultValue = "10") int size,
            @CurrentMember AuthMember authMember
    ) {
        StoreCouponEventsCursor cursor = StoreCouponEventsCursor.ofNullable(lastStartAt, lastEndAt, lastEventId);
        LocalDateTime now = LocalDateTime.now();
        StoreCouponEventListResponse response = couponEventService.getCouponEventsByStore(authMember.id(), storeId, status, now, cursor, size);
        return ApiResponse.success(response);
    }

    @GetMapping("/owner/coupons/events/statistics")
    public ResponseEntity<ApiResponse<StoreCouponEventStatisticsResponse>> getStoreCouponEventStatistics(
            @RequestParam(required = false) Long lastStoreId,
            @RequestParam(defaultValue = "10") int size,
            @CurrentMember AuthMember authMember
    ) {
        var cursor = StoreCouponEventsStatisticsCursor.ofNullable(lastStoreId);
        StoreCouponEventStatisticsResponse responses = couponEventService.getStoreCouponEventStatistics(authMember.id(), cursor, size);
        return ApiResponse.success(responses);
    }

}
