package com.couponpop.couponservice.domain.notification.service;

import com.couponpop.couponpopcoremodule.dto.fcmtoken.response.FcmTokensResponse;
import com.couponpop.couponservice.common.response.ApiResponse;
import com.couponpop.couponservice.domain.notification.client.NotificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationInternalServiceImpl implements NotificationInternalService {

    private final NotificationClient notificationClient;

    @Override
    public FcmTokensResponse fetchFcmTokensByMemberId(Long memberId) {
        ApiResponse<FcmTokensResponse> response = notificationClient.fetchFcmTokensByMemberId(memberId);
        return response.getData();
    }
}
