package com.couponpop.couponservice.common.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients("com.couponpop.couponservice")
public class OpenFeignConfig {
}
