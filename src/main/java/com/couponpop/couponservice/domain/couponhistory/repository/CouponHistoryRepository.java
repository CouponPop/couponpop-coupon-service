package com.couponpop.couponservice.domain.couponhistory.repository;

import com.couponpop.couponservice.domain.couponhistory.entity.CouponHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponHistoryRepository extends JpaRepository<CouponHistory, Long> {
}
