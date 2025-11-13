package com.couponpop.couponservice.domain.store.exception;

import com.couponpop.couponservice.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    STORE_NOT_FOUND(HttpStatus.BAD_REQUEST, "매장을 찾을 수 없습니다."),
    STORE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 매장입니다."),
    STORE_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "해당 매장은 로그인한 회원 소유가 아닙니다."),
    STORE_NOT_MATCHED_WITH_COUPON(HttpStatus.BAD_REQUEST, "쿠폰 발급 매장과 사용 매장이 일치하지 않습니다"),
    STORE_ACCESS_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "매장 접근 권한이 없습니다."),
    STORE_UPDATE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "매장 수정 권한이 없습니다."),
    STORE_DELETE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "매장 삭제 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
