package com.couponpop.couponservice.domain.couponevent.command.service;

import com.couponpop.couponpopcoremodule.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.coupon.common.repository.db.CouponRepository;
import com.couponpop.couponservice.domain.couponevent.command.controller.dto.request.CreateCouponEventRequest;
import com.couponpop.couponservice.domain.couponevent.command.controller.dto.response.CreateCouponEventResponse;
import com.couponpop.couponservice.domain.couponevent.common.entity.CouponEvent;
import com.couponpop.couponservice.domain.couponevent.common.enums.CouponEventStatus;
import com.couponpop.couponservice.domain.couponevent.common.exception.CouponEventErrorCode;
import com.couponpop.couponservice.domain.couponevent.common.repository.CouponEventRepository;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import com.couponpop.couponservice.utils.TestUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponEventCommandServiceTest {

    @Mock
    private CouponEventRepository couponEventRepository;

    @Mock
    private StoreInternalService storeInternalService;

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponEventCommandService couponEventCommandService;

    @Nested
    @DisplayName("쿠폰 이벤트 생성")
    class CreateCouponEvent {
        @Test
        @DisplayName("쿠폰 이벤트 생성 - 성공")
        void createCouponEvent_success() {
            // given
            LocalDateTime eventStartAt = LocalDateTime.of(2025, 10, 14, 17, 0);
            LocalDateTime eventEndAt = LocalDateTime.of(2025, 10, 15, 12, 0);

            CreateCouponEventRequest request = CreateCouponEventRequest.builder()
                    .storeId(1L)
                    .name("아이스 아메리카노 1+1")
                    .eventStartAt(eventStartAt)
                    .eventEndAt(eventEndAt)
                    .totalCount(30)
                    .build();

            Long userId = 1L;
            StoreOwnershipResponse storeOwnership = StoreOwnershipResponse.from(true);
            given(storeInternalService.checkOwnership(anyLong(), anyLong())).willReturn(storeOwnership);

            CouponEvent couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                    "id", 1L,
                    "name", "아이스 아메리카노 1+1",
                    "eventStartAt", eventStartAt,
                    "eventEndAt", eventEndAt,
                    "couponEventStatus", CouponEventStatus.SCHEDULED
            ));
            given(couponEventRepository.save(any(CouponEvent.class))).willReturn(couponEvent);

            // when
            CreateCouponEventResponse response = couponEventCommandService.createCouponEvent(request, userId);

            // then
            assertThat(response)
                    .extracting("eventId", "name", "eventStartAt", "eventEndAt")
                    .contains(
                            1L, "아이스 아메리카노 1+1", eventStartAt, eventEndAt
                    );
        }

        @Test
        @DisplayName("이벤트 기간이 48시간을 초과하면 예외 발생")
        void createCouponEvent_fail_eventDurationExceeded() {
            // given
            LocalDateTime eventStartAt = LocalDateTime.of(2025, 10, 14, 12, 0);
            LocalDateTime eventEndAt = eventStartAt.plusHours(49); // 49시간 → 초과

            CreateCouponEventRequest request = CreateCouponEventRequest.builder()
                    .storeId(1L)
                    .name("아이스 아메리카노 1+1")
                    .eventStartAt(eventStartAt)
                    .eventEndAt(eventEndAt)
                    .totalCount(30)
                    .build();

            Long userId = 1L;

            StoreOwnershipResponse storeOwnership = StoreOwnershipResponse.from(true);
            given(storeInternalService.checkOwnership(anyLong(), anyLong())).willReturn(storeOwnership);

            // when & then
            assertThatThrownBy(() -> couponEventCommandService.createCouponEvent(request, userId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_DURATION_EXCEEDED.getMessage());
        }

        @Test
        @DisplayName("이벤트 종료 시간이 시작 시간보다 이전이면 예외 발생")
        void createCouponEvent_fail_eventEndBeforeStart() {
            // given
            LocalDateTime eventStartAt = LocalDateTime.of(2025, 10, 14, 12, 0);
            LocalDateTime eventEndAt = eventStartAt.minusHours(1); // 종료 < 시작

            CreateCouponEventRequest request = CreateCouponEventRequest.builder()
                    .storeId(1L)
                    .name("아이스 아메리카노 1+1")
                    .eventStartAt(eventStartAt)
                    .eventEndAt(eventEndAt)
                    .totalCount(30)
                    .build();

            Long userId = 1L;
            StoreOwnershipResponse storeOwnership = StoreOwnershipResponse.from(true);
            given(storeInternalService.checkOwnership(anyLong(), anyLong())).willReturn(storeOwnership);

            // when & then
            assertThatThrownBy(() -> couponEventCommandService.createCouponEvent(request, userId))
                    .isInstanceOf(GlobalException.class)
                    .hasMessage(CouponEventErrorCode.EVENT_END_BEFORE_START.getMessage());
        }
    }

}