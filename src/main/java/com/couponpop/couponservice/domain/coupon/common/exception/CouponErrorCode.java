package com.couponpop.couponservice.domain.coupon.common.exception;

import com.couponpop.couponservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

    COUPON_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 쿠폰에 접근할 수 없습니다."),

    COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "해당 쿠폰은 이미 발급되었습니다."),
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "존재하지 않은 쿠폰입니다."),
    COUPON_INVALID_TEMP_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 QR 코드입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 쿠폰은 사용할 수 없습니다."),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다."),
    COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "쿠폰이 사용 가능한 상태가 아닙니다.");


    private final HttpStatus httpStatus;
    private final String message;

}
