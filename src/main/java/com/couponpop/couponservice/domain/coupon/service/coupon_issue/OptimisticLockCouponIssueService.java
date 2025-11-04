package com.couponpop.couponservice.domain.coupon.service.coupon_issue;

import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;
import com.couponpop.couponservice.domain.couponevent.exception.CouponEventErrorCode;
import com.couponpop.couponservice.domain.couponevent.repository.CouponEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OptimisticLockCouponIssueService implements CouponIssueFacade {

    private final CouponEventRepository couponEventRepository;
    private final DefaultCouponIssueService defaultCouponIssueService;

    @Transactional
    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) {
        CouponEvent event = validateEventPeriodAndTime(eventId, currentDateTime);
        defaultCouponIssueService.validateCouponDuplication(memberId, eventId);
        defaultCouponIssueService.saveCouponIssue(memberId, currentDateTime, event);
    }

    public CouponEvent validateEventPeriodAndTime(Long eventId, LocalDateTime currentDateTime) {
        CouponEvent event = couponEventRepository.findByEventIdWithOptimisticLock(eventId)
                .orElseThrow(() -> new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND));

        event.validateIssuable(currentDateTime);
        return event;
    }

}
