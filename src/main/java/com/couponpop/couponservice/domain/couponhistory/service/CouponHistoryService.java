package com.couponpop.couponservice.domain.couponhistory.service;

import com.couponpop.couponservice.domain.couponhistory.entity.CouponHistory;
import com.couponpop.couponservice.domain.couponhistory.repository.CouponHistoryRepository;
import com.couponpop.couponservice.domain.couponhistory.service.dto.CouponHistoryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponHistoryService {

    private final CouponHistoryRepository couponHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCouponHistory(CouponHistoryDto couponHistoryDto) {
        CouponHistory couponHistory = CouponHistory.create(
                couponHistoryDto.couponId(),
                couponHistoryDto.memberId(),
                couponHistoryDto.storeId(),
                couponHistoryDto.eventId(),
                couponHistoryDto.couponStatus()
        );
        couponHistoryRepository.save(couponHistory);
    }
}
