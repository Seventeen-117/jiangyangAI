package com.signature.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 缓存策略配置
 * 用于配置不同验证场景的缓存策略
 */
@Data
@Component
@ConfigurationProperties(prefix = "signature.cache")
public class CacheStrategyConfig {

    /**
     * 是否启用Redis缓存
     */
    private boolean redisEnabled = true;

    /**
     * 是否启用本地缓存
     */
    private boolean localEnabled = true;

    /**
     * 缓存策略配置
     */
    private Strategy strategy = new Strategy();

    /**
     * 缓存统计配置
     */
    private Statistics statistics = new Statistics();

    @Data
    public static class Strategy {
        /**
         * API Key缓存策略
         */
        private CacheConfig apiKey = new CacheConfig(Duration.ofMinutes(5), 1000);

        /**
         * 签名验证缓存策略
         */
        private CacheConfig signature = new CacheConfig(Duration.ofMinutes(10), 2000);

        /**
         * 权限验证缓存策略
         */
        private CacheConfig permission = new CacheConfig(Duration.ofMinutes(15), 1500);

        /**
         * JWT Token缓存策略
         */
        private CacheConfig jwt = new CacheConfig(Duration.ofHours(1), 500);

        /**
         * 用户信息缓存策略
         */
        private CacheConfig user = new CacheConfig(Duration.ofMinutes(30), 800);

        /**
         * 应用配置缓存策略
         */
        private CacheConfig appConfig = new CacheConfig(Duration.ofHours(2), 100);

        /**
         * 排除路径缓存策略
         */
        private CacheConfig excludedPaths = new CacheConfig(Duration.ofHours(1), 50);
    }

    @Data
    public static class CacheConfig {
        /**
         * 缓存过期时间
         */
        private Duration ttl;

        /**
         * 最大缓存条目数（仅本地缓存）
         */
        private int maxSize;

        public CacheConfig() {
        }

        public CacheConfig(Duration ttl, int maxSize) {
            this.ttl = ttl;
            this.maxSize = maxSize;
        }
    }

    @Data
    public static class Statistics {
        /**
         * 是否启用缓存统计
         */
        private boolean enabled = true;

        /**
         * 统计间隔（秒）
         */
        private int intervalSeconds = 60;

        /**
         * 是否记录缓存命中率
         */
        private boolean recordHitRate = true;

        /**
         * 是否记录缓存大小
         */
        private boolean recordSize = true;
    }

    /**
     * 获取指定类型的缓存配置
     */
    public CacheConfig getCacheConfig(String type) {
        switch (type.toLowerCase()) {
            case "apikey":
                return strategy.getApiKey();
            case "signature":
                return strategy.getSignature();
            case "permission":
                return strategy.getPermission();
            case "jwt":
                return strategy.getJwt();
            case "user":
                return strategy.getUser();
            case "appconfig":
                return strategy.getAppConfig();
            case "excludedpaths":
                return strategy.getExcludedPaths();
            default:
                return new CacheConfig(Duration.ofMinutes(30), 1000);
        }
    }
}
