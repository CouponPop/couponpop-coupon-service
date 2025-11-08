package com.couponpop.couponservice.domain.coupon.command.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UseCouponRequest(

        @NotBlank(message = "QR 코드는 필수입니다.")
        String qrCode,

        @NotNull(message = "사용하려는 쿠폰 ID는 필수입니다.")
        Long couponId,

        @NotNull(message = "쿠폰 사용 매장 ID는 필수입니다.")
        Long storeId
) {
}
