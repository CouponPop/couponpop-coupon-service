package com.couponpop.couponservice.domain.store.service;

import com.couponpop.couponpopcoremodule.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.couponpopcoremodule.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.couponservice.common.response.ApiResponse;
import com.couponpop.couponservice.domain.store.client.StoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreInternalServiceImpl implements StoreInternalService {

    private final StoreClient storeClient;

    @Override
    public StoreOwnershipResponse checkOwnership(Long storeId, Long memberId) {
        ApiResponse<StoreOwnershipResponse> response = storeClient.checkOwnership(storeId, memberId);
        return response.getData();
    }

    @Override
    public List<StoreResponse> findStoresByOwner(Long memberId, StoreCouponEventsStatisticsCursor cursor, int pageSize) {
        ApiResponse<List<StoreResponse>> response = storeClient.findStoresByOwner(memberId, cursor.lastStoreId(), pageSize);
        return response.getData();
    }

    @Override
    public StoreResponse findByIdOrElseThrow(Long storeId) {
        ApiResponse<StoreResponse> response = storeClient.findByIdOrElseThrow(storeId);
        return response.getData();
    }

    @Override
    public List<StoreResponse> findAllByIds(List<Long> storeIds) {
        ApiResponse<List<StoreResponse>> response = storeClient.findAllByIds(storeIds);
        return response.getData();
    }
}
