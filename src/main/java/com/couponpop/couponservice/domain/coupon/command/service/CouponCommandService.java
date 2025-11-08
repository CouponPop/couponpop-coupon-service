package com.couponpop.couponservice.domain.coupon.command.service;


import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.coupon.common.entity.Coupon;
import com.couponpop.couponservice.domain.coupon.common.exception.CouponErrorCode;
import com.couponpop.couponservice.domain.coupon.common.repository.db.CouponRepository;
import com.couponpop.couponservice.domain.coupon.common.repository.redis.TemporaryCouponCodeRepository;
import com.couponpop.couponservice.domain.coupon.event.model.CouponUsedEvent;
import com.couponpop.couponservice.domain.store.exception.StoreErrorCode;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponCommandService {

    // ****** Coupon Domain ****** //
    private final CouponRepository couponRepository;
    private final TemporaryCouponCodeRepository temporaryCouponCodeRepository;

    // ****** Event Publish ****** //
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 쿠폰 사용 처리 메서드.
     *
     * <p>사용자가 QR 코드(임시 코드)를 통해 쿠폰을 사용할 때 호출되며,
     * 다음 과정을 순차적으로 수행한다.</p>
     *
     * <ol>
     *     <li>Redis에 저장된 임시 코드(QR) 유효성 검증</li>
     *     <li>비관적 락 기반 쿠폰 + 이벤트 조회 (중복 사용 방어)</li>
     *     <li>이벤트 진행 상태 검증 (시작/종료 시간 포함)</li>
     *     <li>쿠폰 소유자 검증</li>
     *     <li>쿠폰 사용 가능 상태 검증 (USED, EXPIRED 등 방지)</li>
     *     <li>쿠폰 사용 처리 및 사용 시각 기록</li>
     *     <li>Redis에 저장된 임시 코드 삭제</li>
     * </ol>
     *
     * <p><b>동시성 처리:</b> 현재는 비관적 락을 사용 중이며,
     * 추후 Redis 분산 락(Redisson) 또는 낙관적 락(@Version)을 통한 개선 예정.</p>
     *
     * @param couponId 쿠폰 ID
     * @param qrCode   쿠폰 사용 시 전달된 QR 코드(임시 코드)
     * @param memberId 쿠폰 사용 요청자의 회원 ID
     * @param usedAt   쿠폰 사용 시각
     * @throws GlobalException 유효하지 않은 코드, 이벤트 기간 종료, 소유자 불일치, 이미 사용된 쿠폰 등 예외 발생
     */
    // TODO : 도메인 로직의 핵심성과 비핵심성을 분리
    @Transactional
    public void useCoupon(Long storeId, Long couponId, String qrCode, Long memberId, LocalDateTime usedAt) {

        //  Redis 임시 코드 검증
        if (!temporaryCouponCodeRepository.validateAndDeleteTemporaryCoupon(couponId, qrCode)) {
            throw new GlobalException(CouponErrorCode.COUPON_INVALID_TEMP_CODE);
        }

        /**
         * TODO: [동시성 개선]
         * 현재는 비관적 락(Pessimistic Lock)으로 중복 사용을 방어하고 있음.
         * 추후 Redis 기반 분산 락 또는 낙관적 락(Optimistic Lock)으로 개선 예정.
         * ex) Redisson, @Version 등 활용 가능
         */
        // 쿠폰 + 이벤트 조회
        Coupon coupon = couponRepository.findByIdWithCouponEvent(couponId)
                .orElseThrow(() -> new GlobalException(CouponErrorCode.COUPON_NOT_FOUND));

        // 매장 검증 - 사용하려는 매장이 쿠폰이 발급된 매장이 아니면 Error
        if (!coupon.getStoreId().equals(storeId)) {
            throw new GlobalException(StoreErrorCode.STORE_NOT_MATCHED_WITH_COUPON);
        }
        // 쿠폰 소유자 검증
        if (!coupon.getMemberId().equals(memberId)) {
            throw new GlobalException(CouponErrorCode.COUPON_ACCESS_DENIED);
        }

        // 이벤트 상태 검증
        coupon.getCouponEvent().validateInProgress(usedAt);

        // 쿠폰 사용
        coupon.use(usedAt);

        eventPublisher.publishEvent(CouponUsedEvent.of(
                couponId,
                memberId,
                coupon.getStoreId(),
                coupon.getCouponEvent().getId(),
                coupon.getCouponEvent().getName()
        ));
    }

}
