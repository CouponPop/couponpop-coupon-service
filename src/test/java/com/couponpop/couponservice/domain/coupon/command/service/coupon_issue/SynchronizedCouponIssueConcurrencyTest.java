package com.couponpop.couponservice.domain.coupon.command.service.coupon_issue;

import com.couponpop.couponservice.domain.coupon.command.service.CouponIssueConcurrencyTestSupport;
import com.couponpop.couponservice.domain.couponevent.common.entity.CouponEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Disabled
class SynchronizedCouponIssueConcurrencyTest extends CouponIssueConcurrencyTestSupport {

    @Autowired
    private SynchronizedCouponIssueService couponIssueFacade;

    @Test
    void 동시에_100개_요청() throws InterruptedException {
        // given
        final Long eventId = couponEvent.getId();

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final long currentMemberId = i + 1;
            executorService.submit(() -> {
                try {
                    couponIssueFacade.issueCoupon(currentMemberId, eventId, issuedTime);
                } catch (Exception e) {
                    log.error("에러", e);
                    throw e;
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // when
        CouponEvent event = couponEventRepository.findById(eventId).orElseThrow();

        // then
        assertThat(event.getIssuedCount()).isEqualTo(100);
    }

}