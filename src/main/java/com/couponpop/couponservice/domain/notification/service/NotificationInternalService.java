package com.couponpop.couponservice.domain.notification.service;

import com.couponpop.couponpopcoremodule.dto.fcmtoken.response.FcmTokensResponse;

public interface NotificationInternalService {

    FcmTokensResponse fetchFcmTokensByMemberId(Long memberId);
}
