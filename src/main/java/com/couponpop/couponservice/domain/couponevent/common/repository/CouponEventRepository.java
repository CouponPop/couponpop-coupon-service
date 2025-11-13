package com.couponpop.couponservice.domain.couponevent.common.repository;

import com.couponpop.couponservice.domain.couponevent.common.entity.CouponEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long>, CouponEventQueryRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CouponEvent e where e.id = :eventId")
    Optional<CouponEvent> findEventForUpdate(@Param("eventId") Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("select e from CouponEvent e where e.id = :eventId")
    Optional<CouponEvent> findByEventIdForUpdate(@Param("eventId") Long eventId);

}
