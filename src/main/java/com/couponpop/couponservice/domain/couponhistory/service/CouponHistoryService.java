package com.couponpop.couponservice.domain.couponhistory.service;

import com.couponpop.couponservice.domain.couponhistory.entity.CouponHistory;
import com.couponpop.couponservice.domain.couponhistory.repository.CouponHistoryRepository;
import com.couponpop.couponservice.domain.couponhistory.service.dto.CouponUsedDto;
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
    public void saveCouponHistory(CouponUsedDto couponUsedDto) {
        try {
            CouponHistory couponHistory = CouponHistory.create(
                    couponUsedDto.couponId(),
                    couponUsedDto.memberId(),
                    couponUsedDto.storeId(),
                    couponUsedDto.eventId(),
                    couponUsedDto.couponStatus()
            );
            couponHistoryRepository.save(couponHistory);
        } catch (Exception e) {
            // 독립 트랜잭션이므로 여기서 예외가 발생해도 원본 트랜잭션에는 영향 없음
            log.error("""
                            [쿠폰 사용 통계 적재 실패]
                            - 예외유형 : {}
                            - 메시지 : {}
                            - 쿠폰ID : {}
                            - 회원ID : {}
                            - 매장ID : {}
                            - 이벤트ID : {}
                            - 상태 : {}
                            """,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    couponUsedDto.couponId(),
                    couponUsedDto.memberId(),
                    couponUsedDto.storeId(),
                    couponUsedDto.eventId(),
                    couponUsedDto.couponStatus(),
                    e
            );
        }
    }
}
