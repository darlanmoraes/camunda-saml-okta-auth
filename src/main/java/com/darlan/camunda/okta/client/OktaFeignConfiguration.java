package com.darlan.camunda.okta.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class OktaFeignConfiguration {

    private static final String SUFFIX = "SSWS ";

    @Value("${okta.token}")
    private String token;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> template.header("Authorization", SUFFIX + token);
    }
}