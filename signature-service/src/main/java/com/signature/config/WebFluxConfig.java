package com.signature.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux配置类
 * 确保应用程序完全使用WebFlux技术栈
 */
@Configuration
@EnableWebFlux
@ConditionalOnProperty(name = "spring.main.web-application-type", havingValue = "reactive")
public class WebFluxConfig implements WebFluxConfigurer {

    /**
     * 配置WebFlux相关设置
     */
    @Bean
    public WebFluxConfigurer webFluxConfigurer() {
        return new WebFluxConfigurer() {
            // 可以在这里添加自定义的WebFlux配置
        };
    }
}
