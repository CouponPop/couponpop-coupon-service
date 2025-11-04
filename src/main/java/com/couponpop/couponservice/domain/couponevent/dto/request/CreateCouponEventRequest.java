package com.couponpop.couponservice.domain.couponevent.dto.request;

import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateCouponEventRequest(

        @NotNull(message = "매장 ID는 필수입니다.")
        Long storeId,

        @NotBlank(message = "이벤트명은 필수입니다.")
        String name,

        @NotNull(message = "이벤트 시작 시간은 필수입니다.")
        LocalDateTime eventStartAt,

        @NotNull(message = "이벤트 종료 시간은 필수입니다.")
        LocalDateTime eventEndAt,

        @Min(value = 1, message = "쿠폰 총 발급 수량은 1 이상이어야 합니다.")
        int totalCount
) {
    public CouponEvent toEntity(Long storeId, Long memberId) {
        return CouponEvent.create(name, eventStartAt, eventEndAt, totalCount, storeId, memberId);
    }
}
