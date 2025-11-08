package com.couponpop.couponservice.domain.coupon.command.service;

import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.coupon.common.entity.Coupon;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.common.exception.CouponErrorCode;
import com.couponpop.couponservice.domain.coupon.common.repository.db.CouponRepository;
import com.couponpop.couponservice.domain.coupon.common.repository.redis.TemporaryCouponCodeRepository;
import com.couponpop.couponservice.domain.coupon.event.model.CouponUsedEvent;
import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;
import com.couponpop.couponservice.domain.couponevent.enums.CouponEventStatus;
import com.couponpop.couponservice.domain.couponevent.exception.CouponEventErrorCode;
import com.couponpop.couponservice.domain.couponevent.repository.CouponEventRepository;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CouponCommandServiceTest {

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
    private CouponCommandService couponCommandService;

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

//    @Nested
//    @DisplayName("쿠폰 발급 (issueCoupon)")
//    class IssueCouponTests {
//
//        @Test
//        @DisplayName("쿠폰 발급 성공")
//        void issuedCoupon_success() {
//            // given
//            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
//            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));
//            given(couponRepository.existsByMemberIdAndCouponEventId(anyLong(), anyLong())).willReturn(false);
//            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));
//
//            // when
//            couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime);
//
//            // then
//            assertThat(couponEvent.getIssuedCount()).isEqualTo(1);
//            then(couponRepository).should().save(any(Coupon.class));
//        }
//
//        @Test
//        @DisplayName("쿠폰 발급 실패 - 이벤트 없음")
//        void issueCoupon_eventNotFound() {
//            // given
//            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
//            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.empty());
//
//            // when & then
//            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
//                    .isInstanceOf(GlobalException.class)
//                    .hasMessage(CouponEventErrorCode.EVENT_NOT_FOUND.getMessage());
//        }
//
//        @Test
//        @DisplayName("쿠폰 발급 실패 - 이벤트가 아직 시작되지 않았습니다.")
//        void issueCoupon_eventNotStarted() {
//            // given
//            couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
//                    "id", 1L,
//                    "name", "이벤트 시간 벗어남",
//                    "eventStartAt", issuedTime.plusHours(2),
//                    "eventEndAt", issuedTime.plusDays(1),
//                    "totalCount", 10,
//                    "couponEventStatus", CouponEventStatus.IN_PROGRESS,
//                    "store", store
//            ));
//            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
//            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));
//
//            // when & then
//            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
//                    .isInstanceOf(GlobalException.class)
//                    .hasMessage(CouponEventErrorCode.EVENT_NOT_STARTED.getMessage());
//        }
//
//        @Test
//        @DisplayName("쿠폰 발급 실패 - 이벤트가 종료되었습니다.")
//        void issueCoupon_eventEnded() {
//            // given
//            couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
//                    "id", 1L,
//                    "name", "이벤트 시간 벗어남",
//                    "eventStartAt", issuedTime.minusHours(2),
//                    "eventEndAt", issuedTime.minusHours(1),
//                    "totalCount", 10,
//                    "couponEventStatus", CouponEventStatus.IN_PROGRESS,
//                    "store", store
//            ));
//            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
//            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));
//
//            // when & then
//            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
//                    .isInstanceOf(GlobalException.class)
//                    .hasMessage(CouponEventErrorCode.EVENT_ENDED.getMessage());
//        }
//
//        @Test
//        @DisplayName("쿠폰 발급 실패 - 쿠폰 모두 소진 (발급 수량과 총 수량 동일하게 설정)")
//        void issueCoupon_eventCouponSoldOut() {
//            // given
//            couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
//                    "id", 1L,
//                    "name", "이벤트 시간 벗어남",
//                    "eventStartAt", issuedTime.minusHours(2),
//                    "eventEndAt", issuedTime.plusDays(1),
//                    "totalCount", 10,
//                    "issuedCount", 10,
//                    "couponEventStatus", CouponEventStatus.IN_PROGRESS,
//                    "store", store
//            ));
//
//            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
//            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));
//
//            // when & then
//            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
//                    .isInstanceOf(GlobalException.class)
//                    .hasMessage(CouponEventErrorCode.EVENT_COUPON_SOLD_OUT.getMessage());
//        }
//
//        @Test
//        @DisplayName("쿠폰 발급 실패 - 이미 발급된 쿠폰")
//        void issueCoupon_alreadyIssued() {
//            // given
//            given(storeRepository.findById(anyLong())).willReturn(Optional.of(store));
//            given(couponEventRepository.findEventForUpdate(anyLong())).willReturn(Optional.of(couponEvent));
//            given(couponRepository.existsByMemberIdAndCouponEventId(anyLong(), anyLong())).willReturn(true);
//
//            // when & then
//            assertThatThrownBy(() -> couponService.issueEventCoupon(member.getId(), store.getId(), couponEvent.getId(), issuedTime))
//                    .isInstanceOf(GlobalException.class)
//                    .hasMessage(CouponErrorCode.COUPON_ALREADY_ISSUED.getMessage());
//        }
//    }

    @Nested
    @DisplayName("쿠폰 사용 (useCoupon)")
    class UseCouponTests {

        @Test
        @DisplayName("쿠폰 사용 - 성공")
        void useCoupon_Success() {
            // given
            given(temporaryCouponCodeRepository.validateAndDeleteTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));

            LocalDateTime usedAt = eventStartAt.plusHours(10);
            String qrCode = "QR123";

            // when
            couponCommandService.useCoupon(storeId, coupon.getId(), qrCode, customerId, usedAt);

            // then
            assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.USED);
            assertThat(coupon.getUsedAt()).isEqualTo(usedAt);

            verify(eventPublisher, times(1))
                    .publishEvent(any(CouponUsedEvent.class));
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 임시 코드 없을 때 예외치")
        void useCoupon_InvalidQRCode() {
            // given
            given(temporaryCouponCodeRepository.validateAndDeleteTemporaryCoupon(anyLong(), anyString())).willReturn(false);

            LocalDateTime usedAt = eventStartAt.plusHours(10);
            String qrCode = "QR123";

            // when & then
            assertThatThrownBy(() -> couponCommandService.useCoupon(storeId, coupon.getId(), qrCode, customerId, usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_INVALID_TEMP_CODE.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 이벤트 시작 전")
        void useCoupon_EventNotStarted() {
            // given
            given(temporaryCouponCodeRepository.validateAndDeleteTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));

            LocalDateTime usedAt = eventStartAt.minusMinutes(1);
            String qrCode = "QR123";

            // when & then
            assertThatThrownBy(() -> couponCommandService.useCoupon(storeId, coupon.getId(), qrCode, customerId, usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_NOT_STARTED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 이벤트 종료 후")
        void useCoupon_EventEnded() {
            // given
            given(temporaryCouponCodeRepository.validateAndDeleteTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));

            LocalDateTime usedAt = eventEndAt.plusMinutes(1);
            String qrCode = "QR123";

            // when & then
            assertThatThrownBy(() -> couponCommandService.useCoupon(storeId, coupon.getId(), qrCode, customerId, usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_ENDED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 쿠폰 소유자 불일치")
        void useCoupon_AccessDenied() {
            // given
            given(temporaryCouponCodeRepository.validateAndDeleteTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));

            LocalDateTime usedAt = eventStartAt.plusHours(10);
            String qrCode = "QR123";

            // when & then
            assertThatThrownBy(() -> couponCommandService.useCoupon(storeId, coupon.getId(), qrCode, 999L, usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_ACCESS_DENIED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 쿠폰 이미 사용됨 (usedAt != null)")
        void useCoupon_AlreadyUsed1() {
            // given
            LocalDateTime usedAt = eventStartAt.plusHours(10);
            coupon = TestUtils.createEntity(Coupon.class, Map.of(
                    "id", 1L,
                    "couponCode", "CPN-9515BD7FE9CD",
                    "receivedAt", couponIssuedAt,
                    "expireAt", eventEndAt,
                    "usedAt", usedAt,
                    "couponStatus", CouponStatus.USED,
                    "couponEvent", couponEvent,
                    "memberId", customerId,
                    "storeId", storeId
            ));

            given(temporaryCouponCodeRepository.validateAndDeleteTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));

            // when & then
            assertThatThrownBy(() -> couponCommandService.useCoupon(storeId, coupon.getId(), "QR123", customerId, usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_ALREADY_USED.getMessage());
        }

        @Test
        @DisplayName("쿠폰 사용 실패 - 쿠폰 이미 사용됨 (isAvailable == false)")
        void useCoupon_AlreadyUsed2() {
            // given
            LocalDateTime usedAt = eventStartAt.plusHours(10);
            coupon = TestUtils.createEntity(Coupon.class, Map.of(
                    "id", 1L,
                    "couponCode", "CPN-9515BD7FE9CD",
                    "receivedAt", couponIssuedAt,
                    "expireAt", eventEndAt,
                    "couponStatus", CouponStatus.USED,
                    "couponEvent", couponEvent,
                    "memberId", customerId,
                    "storeId", storeId
            ));

            given(temporaryCouponCodeRepository.validateAndDeleteTemporaryCoupon(anyLong(), anyString())).willReturn(true);
            given(couponRepository.findByIdWithCouponEvent(anyLong())).willReturn(Optional.of(coupon));

            // when & then
            assertThatThrownBy(() -> couponCommandService.useCoupon(storeId, coupon.getId(), "QR123", customerId, usedAt))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponErrorCode.COUPON_NOT_AVAILABLE.getMessage());
        }
    }

}