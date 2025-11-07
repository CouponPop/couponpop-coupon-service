package com.couponpop.couponservice.domain.coupon.entity;

import com.couponpop.couponservice.common.entity.BaseEntity;
import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.coupon.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.exception.CouponErrorCode;
import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "coupons",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_coupons_member_coupon_event", columnNames = {"member_id", "coupon_event_id"}),
                @UniqueConstraint(name = "uk_coupons_coupon_code", columnNames = "coupon_code")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String couponCode;

    private LocalDateTime receivedAt;

    private LocalDateTime expireAt;

    private LocalDateTime usedAt;

    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_event_id", nullable = false)
    private CouponEvent couponEvent;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;


    @Builder(access = AccessLevel.PRIVATE)
    private Coupon(String couponCode, LocalDateTime receivedAt, LocalDateTime expireAt, LocalDateTime usedAt, CouponStatus couponStatus, CouponEvent couponEvent, Long memberId, Long storeId) {
        this.couponCode = couponCode;
        this.receivedAt = receivedAt;
        this.expireAt = expireAt;
        this.usedAt = usedAt;
        this.couponStatus = couponStatus;
        this.couponEvent = couponEvent;
        this.memberId = memberId;
        this.storeId = storeId;
    }

    public static Coupon createIssuedCoupon(Long memberId, Long storeId, CouponEvent couponEvent, LocalDateTime issuedTime) {
        String couponCode = generateCouponCode();
        return Coupon.builder()
                .couponCode(couponCode)
                .receivedAt(issuedTime)
                .expireAt(couponEvent.getEventEndAt())
                .couponStatus(CouponStatus.ISSUED)
                .couponEvent(couponEvent)
                .memberId(memberId)
                .storeId(storeId)
                .build();
    }

    // TODO : 다른 매장에서 쿠폰 사용에 대한 검증이 없으므로 Token 로 매장 ID, 이벤트 ID(매장 주인),
    private static String generateCouponCode() {
        return "CPN-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public boolean isIssued() {
        return CouponStatus.ISSUED.equals(this.couponStatus);
    }

    // 쿠폰이 만료되었는지
    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(this.expireAt);
    }

    public boolean isUsed() {
        return this.usedAt != null && CouponStatus.USED.equals(this.couponStatus);
    }

    public void use(LocalDateTime usedAt) {
        // 이미 사용된 쿠폰 방어 로직
        if (isUsed()) {
            throw new GlobalException(CouponErrorCode.COUPON_ALREADY_USED);
        }
        // 쿠폰 사용 가능 상태 검증 - AVAILABLE 이 아닌 USED, EXPIRED, CANCELED 이면 사용 못하는 쿠폰
        if (!isIssued()) {
            throw new GlobalException(CouponErrorCode.COUPON_NOT_AVAILABLE);
        }
        // 만료 시간 검증
        if (isExpired(usedAt)) {
            throw new GlobalException(CouponErrorCode.COUPON_EXPIRED);
        }

        this.couponStatus = CouponStatus.USED;
        this.usedAt = usedAt;
    }


}
