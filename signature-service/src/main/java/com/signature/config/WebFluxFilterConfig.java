package com.signature.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;

/**
 * WebFlux过滤器配置
 * 确保只使用WebFlux风格的过滤器，排除传统的Servlet过滤器
 */
@Configuration
public class WebFluxFilterConfig {

    /**
     * 排除传统的Servlet过滤器
     * 这些过滤器在WebFlux环境中会导致冲突
     */
    @Bean
    @ConditionalOnProperty(name = "spring.main.web-application-type", havingValue = "reactive")
    public FilterExclusionConfig filterExclusionConfig() {
        return new FilterExclusionConfig();
    }

    /**
     * 过滤器排除配置
     */
    public static class FilterExclusionConfig {
        
        /**
         * 需要排除的Servlet过滤器类名
         */
        private static final String[] EXCLUDED_FILTERS = {
            "com.signature.filter.AuthenticationFilter",
            "com.signature.filter.ApiKeyAuthenticationFilter", 
            "com.signature.filter.JwtAuthenticationFilter",
            "com.signature.filter.PermissionAuthorizationFilter",
            "com.signature.filter.SignatureVerificationFilter",
            "com.signature.filter.AsyncSignatureVerificationFilter"
        };

        public String[] getExcludedFilters() {
            return EXCLUDED_FILTERS;
        }
    }
}
