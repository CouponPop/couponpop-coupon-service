package com.couponpop.couponservice.domain.coupon.query.service;

import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;
import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.coupon.common.entity.Coupon;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.common.exception.CouponErrorCode;
import com.couponpop.couponservice.domain.coupon.common.repository.db.CouponRepository;
import com.couponpop.couponservice.domain.coupon.common.repository.db.dto.CouponSummaryInfoProjection;
import com.couponpop.couponservice.domain.coupon.common.repository.redis.TemporaryCouponCodeRepository;
import com.couponpop.couponservice.domain.coupon.query.controller.dto.request.MemberIssuedCouponCursor;
import com.couponpop.couponservice.domain.coupon.query.controller.dto.response.CouponDetailResponse;
import com.couponpop.couponservice.domain.coupon.query.controller.dto.response.IssuedCouponListResponse;
import com.couponpop.couponservice.domain.couponevent.common.entity.CouponEvent;
import com.couponpop.couponservice.domain.couponevent.common.enums.CouponEventStatus;
import com.couponpop.couponservice.domain.couponevent.common.repository.CouponEventRepository;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import com.couponpop.couponservice.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponQueryServiceTest {

    @Mock
    private StoreInternalService storeInternalService;

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TemporaryCouponCodeRepository temporaryCouponCodeRepository;

    @InjectMocks
    private CouponQueryService couponQueryService;

    private Long eventCreatorId;
    private Long customerId;
    private Long storeId;
    private CouponEvent couponEvent;
    private Coupon coupon;

    private static final LocalDateTime issuedTime = LocalDateTime.of(2025, 10, 20, 16, 20);
    private static final LocalDateTime eventStartAt = issuedTime.minusHours(1);
    private static final LocalDateTime eventEndAt = issuedTime.plusDays(1);

    private static final LocalDateTime couponIssuedAt = eventStartAt.plusHours(4);

    @BeforeEach
    void setUp() {
        eventCreatorId = 1L;
        customerId = 2L;
        storeId = 1L;

        couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                "id", 1L,
                "name", "이벤트 제목",
                "eventStartAt", eventStartAt,
                "eventEndAt", eventEndAt,
                "totalCount", 10,
                "couponEventStatus", CouponEventStatus.IN_PROGRESS,
                "memberId", eventCreatorId,
                "storeId", storeId
        ));

        coupon = TestUtils.createEntity(Coupon.class, Map.of(
                "id", 1L,
                "couponCode", "CPN-9515BD7FE9CD",
                "receivedAt", couponIssuedAt,
                "expireAt", eventEndAt,
                "couponStatus", CouponStatus.ISSUED,
                "couponEvent", couponEvent,
                "memberId", customerId,
                "storeId", storeId
        ));
    }

    @Nested
    @DisplayName("쿠폰 정보 상세 조회 (getCouponDetail)")
    class GetCouponDetailTests {

        @Test
        @DisplayName("사용 가능한(Available) 쿠폰")
        void getCouponDetail_AvailableCoupon() {
            // given
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));
            given(storeInternalService.findByIdOrElseThrow(storeId))
                    .willReturn(
                            StoreResponse.of(1L, "매장1", StoreCategory.CAFE, 3.14, 3.14, "imageUrl")
                    );

            // when
            CouponDetailResponse response = couponQueryService.getCouponDetail(coupon.getId(), customerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(CouponStatus.ISSUED);
            assertThat(response)
                    .extracting("id", "issuedAt", "expireAt")
                    .containsExactly(1L, couponIssuedAt, eventEndAt);
            assertThat(response.usedAt()).isNull();
            assertThat(response.qrCode()).isNotNull();

            verify(temporaryCouponCodeRepository, times(1))
                    .setTemporaryCoupon(anyLong(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("사용된(Used) 쿠폰")
        void getCouponDetail_UnavailableCoupon() {
            // given
            LocalDateTime usedAt = couponIssuedAt.plusHours(5);
            coupon = TestUtils.createEntity(Coupon.class, Map.of(
                    "id", 1L,
                    "receivedAt", couponIssuedAt,
                    "expireAt", eventEndAt,
                    "usedAt", usedAt,
                    "couponStatus", CouponStatus.USED,
                    "couponEvent", couponEvent,
                    "memberId", customerId,
                    "storeId", storeId
            ));

            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));
            given(storeInternalService.findByIdOrElseThrow(storeId))
                    .willReturn(
                            StoreResponse.of(1L, "매장1", StoreCategory.CAFE, 3.14, 3.14, "imageUrl")
                    );

            // when
            CouponDetailResponse response = couponQueryService.getCouponDetail(coupon.getId(), customerId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(CouponStatus.USED);
            assertThat(response.usedAt()).isNotNull();
            assertThat(response.qrCode()).isNull();

            verify(temporaryCouponCodeRepository, times(0))
                    .setTemporaryCoupon(anyLong(), anyString(), anyString(), anyLong());
        }

        @Test
        @DisplayName("쿠폰 상세 조회 실패 - 쿠폰 없음")
        void getCouponDetail_NotFound() {
            // given
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> couponQueryService.getCouponDetail(coupon.getId(), customerId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("쿠폰 상세 조회 실패 - 접근 권한 없음")
        void getCouponDetail_AccessDenied() {
            // given
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));

            // when & then
            assertThatThrownBy(() -> couponQueryService.getCouponDetail(coupon.getId(), 999L))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_ACCESS_DENIED.getMessage());
        }
    }

    @Nested
    @DisplayName("쿠폰 리스트 조회 - 커서 기반")
    class GetIssuedCoupons {
        private static final LocalDateTime now = LocalDateTime.of(2025, 10, 20, 16, 0);

        private CouponSummaryInfoProjection createMockCoupon(Long id, LocalDateTime eventEndAt, Long storeId) {
            return new CouponSummaryInfoProjection(
                    id,
                    CouponStatus.ISSUED,
                    now.minusDays(1),
                    now.plusDays(5),
                    null,
                    id,
                    "Event " + id,
                    now.minusDays(2),
                    eventEndAt,
                    storeId
            );
        }

        @Test
        @DisplayName("첫 페이지 조회 - hasNext true")
        void getIssuedCoupons_firstPage_hasNextTrue() {
            // given
            var cursor = MemberIssuedCouponCursor.first();
            int pageSize = 2;

            List<CouponSummaryInfoProjection> mockCoupons = List.of(
                    createMockCoupon(1L, now.minusHours(1), 1L),
                    createMockCoupon(2L, now, 1L),
                    createMockCoupon(3L, now.plusHours(1), 2L) // pageSize + 1
            );

            List<StoreResponse> mockStores = List.of(
                    StoreResponse.of(1L, "매장1", StoreCategory.CAFE, 3.14, 3.14, "imageUrl"),
                    StoreResponse.of(2L, "매장2", StoreCategory.CAFE, 3.14, 3.14, "imageUrl")
            );

            given(couponRepository.findAllByMemberIdWithEventAndStore(anyLong(), any(CouponStatus.class), isNull(), isNull(), eq(pageSize + 1)))
                    .willReturn(mockCoupons);
            given(storeInternalService.findAllByIds(anyList())).willReturn(mockStores);

            // when
            IssuedCouponListResponse response = couponQueryService.getIssuedCoupons(
                    customerId, CouponStatus.ISSUED, cursor, pageSize
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.size()).isEqualTo(pageSize); // 리스트 trimming
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextCursor().lastCouponId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("마지막 페이지 조회 - hasNext false")
        void getIssuedCoupons_lastPage_hasNextFalse() {
            // given
            LocalDateTime lastEventEndAt = now.plusHours(1);
            Long lastCouponId = 3L;
            var cursor = new MemberIssuedCouponCursor(lastEventEndAt, lastCouponId);
            int pageSize = 3;

            List<CouponSummaryInfoProjection> mockCoupons = List.of(
                    createMockCoupon(1L, now.minusHours(1), 1L),
                    createMockCoupon(2L, now, 1L),
                    createMockCoupon(lastCouponId, lastEventEndAt, 2L)
            );

            List<StoreResponse> mockStores = List.of(
                    StoreResponse.of(1L, "매장1", StoreCategory.CAFE, 3.14, 3.14, "imageUrl"),
                    StoreResponse.of(2L, "매장2", StoreCategory.CAFE, 3.14, 3.14, "imageUrl")
            );

            given(couponRepository.findAllByMemberIdWithEventAndStore(anyLong(), any(CouponStatus.class), any(LocalDateTime.class), anyLong(), eq(pageSize + 1)))
                    .willReturn(mockCoupons);
            given(storeInternalService.findAllByIds(anyList())).willReturn(mockStores);

            // when
            IssuedCouponListResponse response = couponQueryService.getIssuedCoupons(
                    customerId, CouponStatus.ISSUED, cursor, pageSize
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.size()).isEqualTo(pageSize);
            assertThat(response.hasNext()).isFalse();
            assertThat(response.nextCursor()).isNull();
        }

        @Test
        @DisplayName("커서 이후 조회")
        void getIssuedCoupons_nextCursorPage() {
            // given
            var lastCursor = new MemberIssuedCouponCursor(now.minusHours(1), 1L);
            int pageSize = 2;

            List<CouponSummaryInfoProjection> mockCoupons = List.of(
                    createMockCoupon(2L, now, 1L),
                    createMockCoupon(3L, now.plusHours(1), 1L),
                    createMockCoupon(4L, now.plusHours(2), 1L)
            );
            List<StoreResponse> mockStores = List.of(
                    StoreResponse.of(1L, "매장1", StoreCategory.CAFE, 3.14, 3.14, "imageUrl"),
                    StoreResponse.of(2L, "매장2", StoreCategory.CAFE, 3.14, 3.14, "imageUrl")
            );

            given(couponRepository.findAllByMemberIdWithEventAndStore(anyLong(), any(CouponStatus.class), any(LocalDateTime.class), anyLong(), eq(pageSize + 1)))
                    .willReturn(mockCoupons);
            given(storeInternalService.findAllByIds(anyList())).willReturn(mockStores);

            // when
            IssuedCouponListResponse response = couponQueryService.getIssuedCoupons(
                    customerId, CouponStatus.ISSUED, lastCursor, pageSize
            );

            // then
            assertThat(response).isNotNull();
            assertThat(response.size()).isEqualTo(pageSize); // 리스트 trimming
            assertThat(response.hasNext()).isTrue();
            assertThat(response.nextCursor()).isNotNull();
            assertThat(response.nextCursor().lastCouponId()).isEqualTo(3L);
        }
    }
}