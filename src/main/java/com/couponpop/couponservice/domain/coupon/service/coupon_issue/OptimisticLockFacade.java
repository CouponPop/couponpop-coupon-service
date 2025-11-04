package com.couponpop.couponservice.domain.coupon.service.coupon_issue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticLockFacade implements CouponIssueFacade {

    private static final int MAX_RETRY_COUNT = 5;
    private static final long RETRY_DELAY_MS = 500L;

    private final OptimisticLockCouponIssueService optimisticLockCouponIssueService;

    /**
     * 낙관적 락 재시도 로직
     */
    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) throws InterruptedException {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                optimisticLockCouponIssueService.issueCoupon(memberId, eventId, currentDateTime);
                return; // 성공 시 즉시 종료
            } catch (Exception e) {
                retryCount++;
                log.warn("[Retry: {}] OptimisticLockException 발생 - memberId={}, eventId={}", retryCount, memberId, eventId);

                if (retryCount >= MAX_RETRY_COUNT) {
                    log.error("최대 재시도 횟수 초과 - 쿠폰 발급 실패");
                    throw new RuntimeException("쿠폰 발급 실패 (재시도 초과)");
                }

                sleep(RETRY_DELAY_MS * retryCount); // 점진적 backoff
            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis); // 점진적 backoff
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("스레드 인터럽트 발생", ie);
        }
    }

}
