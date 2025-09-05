package com.jiangyang.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.JdkClientHttpConnector;
import java.net.http.HttpClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * WebClient配置
 * 用于网关调用signature-service
 */
@Configuration
public class WebClientConfig {

    @Autowired
    private CustomGatewayProperties gatewayProperties;

    @Bean
    public WebClient webClient() {
        int timeout = gatewayProperties.getSignatureService() != null ?
                gatewayProperties.getSignatureService().getTimeout() : 5000;

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build();

        return WebClient.builder()
                .clientConnector(new JdkClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }
}
