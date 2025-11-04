package com.couponpop.couponservice.domain.couponhistory.entity;

import com.couponpop.couponservice.domain.coupon.enums.CouponStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CouponHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long couponId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long couponEventId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CouponStatus couponStatus;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder(access = AccessLevel.PRIVATE)
    private CouponHistory(Long couponId, Long memberId, Long storeId, Long couponEventId, CouponStatus couponStatus) {
        this.couponId = couponId;
        this.memberId = memberId;
        this.storeId = storeId;
        this.couponEventId = couponEventId;
        this.couponStatus = couponStatus;
    }

    public static CouponHistory create(Long couponId, Long memberId, Long storeId, Long couponEventId, CouponStatus couponStatus) {
        return CouponHistory.builder()
                .couponId(couponId)
                .memberId(memberId)
                .storeId(storeId)
                .couponEventId(couponEventId)
                .couponStatus(couponStatus)
                .build();
    }
}
