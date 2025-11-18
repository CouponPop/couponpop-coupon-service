package com.couponpop.couponservice.domain.coupon.command.service.coupon_issue;

import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.couponevent.common.entity.CouponEvent;
import com.couponpop.couponservice.domain.couponevent.common.exception.CouponEventErrorCode;
import com.couponpop.couponservice.domain.couponevent.common.repository.CouponEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PessimisticLockCouponIssueService implements CouponIssueFacade {

    private final CouponEventRepository couponEventRepository;
    private final CouponIssueService couponIssueService;

    @Transactional
    @Override
    public void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) {
        CouponEvent event = validateEventPeriodAndTime(eventId, currentDateTime);
        couponIssueService.validateCouponDuplication(memberId, eventId);
        couponIssueService.saveCouponIssue(memberId, currentDateTime, event);
    }

    public CouponEvent validateEventPeriodAndTime(Long eventId, LocalDateTime currentDateTime) {
        CouponEvent event = couponEventRepository.findByEventIdForUpdate(eventId)
                .orElseThrow(() -> new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND));

        event.validateIssuable(currentDateTime);
        return event;
    }

}
