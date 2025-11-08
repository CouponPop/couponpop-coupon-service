package com.couponpop.couponservice.domain.coupon.command.service.coupon_issue;

import com.couponpop.couponservice.domain.coupon.command.service.CouponIssueConcurrencyTestSupport;
import com.couponpop.couponservice.domain.couponevent.common.entity.CouponEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
class CouponIssueConcurrencyTest extends CouponIssueConcurrencyTestSupport {

    @Autowired
    private CouponIssueService couponIssueService;

    @Test
    void 동시에_100개_요청_실패() throws InterruptedException {
        // given
        final Long eventId = couponEvent.getId();

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final long currentMemberId = i + 1;
            executorService.submit(() -> {
                try {
                    couponIssueService.issueCoupon(currentMemberId, eventId, issuedTime);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // when
        CouponEvent event = couponEventRepository.findById(eventId).orElseThrow();

        // then
        assertThat(event.getIssuedCount()).isNotEqualTo(100);
    }
}