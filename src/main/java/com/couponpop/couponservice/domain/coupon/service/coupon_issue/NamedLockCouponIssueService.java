package com.couponpop.couponservice.domain.coupon.service.coupon_issue;

import com.couponpop.couponservice.domain.coupon.repository.db.NamedLockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NamedLockCouponIssueService implements CouponIssueFacade {

    private final NamedLockRepository namedLockRepository;
    private final DefaultCouponIssueService defaultCouponIssueService;

    private static final long LOCK_TIMEOUT_SEC = 3000L; // 3초

    /**
     * 쿠폰 발급
     * Named Lock을 사용하여 동시성 제어
     * 락 획득/해제는 트랜잭션 외부에서 수행
     */
    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) {
        try {
            boolean getLock = namedLockRepository.getLock("namedLock", LOCK_TIMEOUT_SEC);
            log.info("[락 획득] getLock = {}", getLock);
            defaultCouponIssueService.issueCoupon(memberId, eventId, currentDateTime);
        } finally {
            namedLockRepository.releaseLock("namedLock");
            log.info("[락 해제] releaseLock");
        }
    }


}



