package com.couponpop.couponservice.domain.coupon.command.service.coupon_issue;

import com.couponpop.couponservice.domain.coupon.common.repository.redis.LettuceLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LettuceLockCouponIssueService implements CouponIssueFacade {

    private static final String LOCK_PREFIX = "coupon_issue";

    private final LettuceLockRepository lettuceLockRepository;
    private final CouponIssueService couponIssueService;

    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) throws InterruptedException {
        while (!lettuceLockRepository.lock(eventId, LOCK_PREFIX)) {
            log.info("[Lettuce Spin Lock 대기]");
            Thread.sleep(100);
        }
        try {
            log.info("[Lettuce 락 획득]");
            couponIssueService.issueCoupon(memberId, eventId, currentDateTime);
        } finally {
            lettuceLockRepository.unlock(eventId, LOCK_PREFIX);
        }
    }

}
