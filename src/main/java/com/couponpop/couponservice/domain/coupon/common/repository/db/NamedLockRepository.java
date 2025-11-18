package com.couponpop.couponservice.domain.coupon.common.repository.db;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NamedLockRepository {

    private final EntityManager em;

    /**
     * Named Lock 획득
     * @param key 락 이름
     * @param timeoutSec 타임아웃 (초)
     * @return true: 획득 성공, false: 실패
     */
    public boolean getLock(String key, long timeoutSec) {
        Object result = em.createNativeQuery("SELECT GET_LOCK(:lockName, :timeout)")
                .setParameter("lockName", key)
                .setParameter("timeout", timeoutSec)
                .getSingleResult();
        return ((Number) result).intValue() == 1;
    }

    /**
     * Named Lock 해제
     * @param key 락 이름
     */
    public void releaseLock(String key) {
        em.createNativeQuery("SELECT RELEASE_LOCK(:lockName)")
                .setParameter("lockName", key)
                .getSingleResult();
    }
}



