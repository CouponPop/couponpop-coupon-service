package com.couponpop.couponservice.common.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration
public class RedisTestContainersConfig {

    private static final String REDIS_IMAGE = "redis:7.2.4";
    private static final int REDIS_PORT = 6379;

    private static final GenericContainer<?> REDIS_CONTAINER;

    static {
        REDIS_CONTAINER = new GenericContainer<>(REDIS_IMAGE)
                .withExposedPorts(REDIS_PORT)
                .withReuse(true);
        REDIS_CONTAINER.start();

        System.setProperty("spring.data.redis.host", REDIS_CONTAINER.getHost());
        System.setProperty("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(REDIS_PORT).toString());
    }

}



