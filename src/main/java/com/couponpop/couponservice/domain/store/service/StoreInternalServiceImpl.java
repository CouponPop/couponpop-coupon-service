package com.couponpop.couponservice.domain.store.service;

import com.couponpop.couponpopcoremodule.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.couponpopcoremodule.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
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
        return storeClient.checkOwnership(storeId, memberId);
    }

    @Override
    public List<StoreResponse> findStoresByOwner(Long memberId, StoreCouponEventsStatisticsCursor cursor, int pageSize) {
        return storeClient.findStoresByOwner(memberId, cursor.lastStoreId(), pageSize);
    }

    @Override
    public StoreResponse findByIdOrElseThrow(Long storeId) {
        return storeClient.findByIdOrElseThrow(storeId);
    }

    @Override
    public List<StoreResponse> findAllByIds(List<Long> storeIds) {
        return storeClient.findAllByIds(storeIds);
    }
}
