package com.couponpop.couponservice.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetrySynchronizationManager;

@Slf4j
@UtilityClass
public final class RetryLogUtils {

    /**
     * 재시도 로그 간략화
     *
     * @param prefix        로그 앞부분
     * @param keyValuePairs key1, value1, key2, value2 ...
     */
    public static void logRetry(String prefix, Object... keyValuePairs) {
        int attempt = RetrySynchronizationManager.getContext() != null
                ? RetrySynchronizationManager.getContext().getRetryCount() + 1
                : 1;

        StringBuilder sb = new StringBuilder(prefix).append(" [attempt=").append(attempt).append("]");

        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            sb.append(" ").append(keyValuePairs[i]).append("=").append(keyValuePairs[i + 1]);
        }

        log.debug(sb.toString());
    }
}
