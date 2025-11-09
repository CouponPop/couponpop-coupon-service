package com.couponpop.couponservice.domain.coupon.event.retry;

import com.couponpop.couponservice.common.utils.RetryLogUtils;
import com.couponpop.couponservice.domain.coupon.event.model.CouponUsedEvent;
import com.couponpop.couponservice.domain.couponhistory.service.CouponHistoryFallbackService;
import com.couponpop.couponservice.domain.couponhistory.service.CouponHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsedRetryableProcessor {

    private final CouponHistoryService couponHistoryService;
    private final CouponHistoryFallbackService fallbackService;

    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000) //재시도 2초 지연
    )
    public void process(CouponUsedEvent event) {
        RetryLogUtils.logRetry("[Retryable] 쿠폰 사용 이력 적재",
                "couponId", event.couponId(),
                "memberId", event.memberId(),
                "storeId", event.storeId());
        couponHistoryService.saveCouponHistory(event.toCouponUsedDto());
    }

    /**
     * 모든 재시도 실패 후 호출 (정합성 보장을 위한 보상 처리)
     */
    @Recover
    public void recover(Exception e, CouponUsedEvent event) {
        log.error("[Recover] 모든 재시도 실패 - 쿠폰 사용 이력 보상 처리 필요! couponId={}, memberId={}", event.couponId(), event.memberId(), e);
        fallbackService.saveToBackupStorage(event.toCouponUsedDto(), e); // 마지막까지 정합성 보장을 위한 fallback 로직
    }
}
