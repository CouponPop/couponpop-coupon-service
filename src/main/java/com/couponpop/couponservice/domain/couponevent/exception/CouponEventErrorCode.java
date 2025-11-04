package com.couponpop.couponservice.domain.couponevent.exception;

import com.couponpop.couponservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponEventErrorCode implements ErrorCode {

    EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쿠폰 이벤트입니다."),
    EVENT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "본인 소유의 쿠폰 이벤트가 아닙니다."),
    EVENT_DURATION_EXCEEDED(HttpStatus.BAD_REQUEST, "쿠폰 이벤트는 최대 48시간까지 생성 가능합니다."),
    EVENT_END_BEFORE_START(HttpStatus.BAD_REQUEST, "이벤트 종료 시간은 시작 시간보다 이후여야 합니다."),
    EVENT_NOT_BELONG_TO_STORE(HttpStatus.BAD_REQUEST, "해당 매장에서 진행되지 않는 이벤트입니다."),
    EVENT_NOT_IN_PROGRESS_TIME(HttpStatus.BAD_REQUEST, "이벤트 진행 시간이 아닙니다."),
    EVENT_COUPON_SOLD_OUT(HttpStatus.BAD_REQUEST, "이벤트 쿠폰이 모두 소진되었습니다."),
    EVENT_NOT_STARTED(HttpStatus.BAD_REQUEST, "이벤트가 아직 시작되지 않았습니다."),
    EVENT_ENDED(HttpStatus.BAD_REQUEST, "이벤트가 종료되었습니다."),
    EVENT_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "현재 진행 중인 이벤트가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
