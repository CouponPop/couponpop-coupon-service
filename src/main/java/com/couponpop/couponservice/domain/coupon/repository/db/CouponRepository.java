package com.couponpop.couponservice.domain.coupon.repository.db;

import com.couponpop.couponservice.domain.coupon.entity.Coupon;
import com.couponpop.couponservice.domain.coupon.enums.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long>, CouponQueryRepository {

    @Query("""
            select count(c)
            from Coupon c
            where c.couponEvent.id = :eventId and c.couponStatus = :status
            """)
    int countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") CouponStatus status);

    boolean existsByMemberIdAndCouponEventId(Long memberId, Long eventId);

    @Query("""
            select c
            from Coupon c
                join fetch c.couponEvent ce
            where c.id = :couponId
            """)
    Optional<Coupon> findByIdWithCouponEvent(@Param("couponId") Long couponId);

}
