package com.couponpop.couponservice.common.datasource;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.function.BiPredicate;

@RequiredArgsConstructor
public enum ReplicationType {

    READ((transactionActive, readOnly) -> transactionActive && readOnly),
    WRITE((transactionActive, readOnly) -> !transactionActive || !readOnly);

    private final BiPredicate<Boolean, Boolean> condition;

    public static ReplicationType from(boolean transactionActive, boolean readOnly) {
        return Arrays.stream(values())
                .filter(replicationType -> replicationType.condition.test(transactionActive, readOnly))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("잘못된 Replication Type 입니다."));
    }
}
