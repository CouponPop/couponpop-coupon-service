package com.couponpop.couponservice.domain.couponhistory.service;

import com.couponpop.couponservice.domain.couponhistory.entity.CouponHistory;
import com.couponpop.couponservice.domain.couponhistory.repository.CouponHistoryRepository;
import com.couponpop.couponservice.domain.couponhistory.service.dto.CouponUsedDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponHistoryService {

    private final CouponHistoryRepository couponHistoryRepository;

    @Transactional
    public void saveCouponHistory(CouponUsedDto couponUsedDto) {
        CouponHistory couponHistory = CouponHistory.create(
                couponUsedDto.couponId(),
                couponUsedDto.memberId(),
                couponUsedDto.storeId(),
                couponUsedDto.eventId(),
                couponUsedDto.couponStatus()
        );
        couponHistoryRepository.save(couponHistory);
    }
}
