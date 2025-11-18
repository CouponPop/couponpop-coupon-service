package com.couponpop.couponservice.domain.coupon.command.service.coupon_issue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class RedissonLockCouponIssueService implements CouponIssueFacade {

    private final RedissonClient redissonClient;
    private final CouponIssueService couponIssueService;

    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) throws InterruptedException {
        RLock lock = redissonClient.getLock(String.valueOf(eventId));

        try {
            // waitTime(락 획득 시도 시간) : 15초
            // leaseTime(락 점유 시간) : 1초
            if (!lock.tryLock(15, 1, TimeUnit.SECONDS)) {
                throw new InterruptedException("Redisson 락 획득 실패");
            }
            log.info("[Redisson 락 획득]");
            couponIssueService.issueCoupon(memberId, eventId, currentDateTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (lock != null && lock.isLocked()) {
                log.info("[Redisson 락 해제]");
                lock.unlock();
            }
        }
    }
}


