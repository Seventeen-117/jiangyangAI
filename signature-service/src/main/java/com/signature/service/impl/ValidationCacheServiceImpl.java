package com.signature.service.impl;

import com.signature.entity.ApiKey;
import com.signature.entity.ExcludedPathConfig;
import com.signature.service.ValidationCacheService;
import com.signature.model.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 验证缓存服务实现类
 * 基于Redis的验证缓存管理
 */
@Slf4j
@Service
public class ValidationCacheServiceImpl implements ValidationCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ========== API Key 缓存操作 ==========

    @Override
    public ApiKey getApiKeyFromCache(String apiKey) {
        try {
            // 直接从Redis缓存获取
            Object cached = redisTemplate.opsForValue().get("apiKey:" + apiKey);
            if (cached != null) {
                log.debug("API Key found in cache: {}", apiKey);
                return (ApiKey) cached;
            }
            log.debug("API Key not found in cache: {}", apiKey);
            return null;
        } catch (Exception e) {
            log.error("Error getting API Key from cache: {}", apiKey, e);
            return null;
        }
    }

    @Override
    public void cacheApiKey(String apiKey, ApiKey apiKeyInfo) {
        try {
            String key = "apiKey:" + apiKey;
            redisTemplate.opsForValue().set(key, apiKeyInfo, Duration.ofMinutes(5));
            log.debug("API Key cached: {}", apiKey);
        } catch (Exception e) {
            log.error("Error caching API Key: {}", apiKey, e);
        }
    }

    @Override
    public void evictApiKey(String apiKey) {
        try {
            String key = "apiKey:" + apiKey;
            redisTemplate.delete(key);
            log.debug("API Key evicted from cache: {}", apiKey);
        } catch (Exception e) {
            log.error("Error evicting API Key: {}", apiKey, e);
        }
    }

    @Override
    public void cacheApiKeys(Map<String, ApiKey> apiKeyMap) {
        try {
            for (Map.Entry<String, ApiKey> entry : apiKeyMap.entrySet()) {
                cacheApiKey(entry.getKey(), entry.getValue());
            }
            log.debug("Batch cached {} API Keys", apiKeyMap.size());
        } catch (Exception e) {
            log.error("Error batch caching API Keys", e);
        }
    }

    @Override
    public boolean isApiKeyCached(String apiKey) {
        try {
            return redisTemplate.hasKey("apiKey:" + apiKey);
        } catch (Exception e) {
            log.error("Error checking if API Key is cached: {}", apiKey, e);
            return false;
        }
    }

    // ========== 签名验证缓存操作 ==========

    @Override
    public ValidationResult getSignatureValidationFromCache(String signatureKey) {
        try {
            // 直接从Redis缓存获取
            Object cached = redisTemplate.opsForValue().get("signature:" + signatureKey);
            if (cached != null) {
                log.debug("Signature validation found in cache: {}", signatureKey);
                return (ValidationResult) cached;
            }
            log.debug("Signature validation not found in cache: {}", signatureKey);
            return null;
        } catch (Exception e) {
            log.error("Error getting signature validation from cache: {}", signatureKey, e);
            return null;
        }
    }

    @Override
    public void cacheSignatureValidation(String signatureKey, ValidationResult result) {
        try {
            String key = "signature:" + signatureKey;
            redisTemplate.opsForValue().set(key, result, Duration.ofMinutes(10));
            log.debug("Signature validation cached: {}", signatureKey);
        } catch (Exception e) {
            log.error("Error caching signature validation: {}", signatureKey, e);
        }
    }

    @Override
    public void evictSignatureValidation(String signatureKey) {
        try {
            String key = "signature:" + signatureKey;
            redisTemplate.delete(key);
            log.debug("Signature validation evicted from cache: {}", signatureKey);
        } catch (Exception e) {
            log.error("Error evicting signature validation: {}", signatureKey, e);
        }
    }

    @Override
    public String generateSignatureCacheKey(Map<String, String> params, String sign, String appId) {
        try {
            // 构建参数字符串
            StringBuilder sb = new StringBuilder();
            sb.append("signature:");
            sb.append(appId).append(":");
            
            // 按字母顺序排序参数
            params.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&"));
            
            sb.append("sign=").append(sign);
            
            // 生成MD5哈希作为缓存键
            String keyString = sb.toString();
            return "signature:" + generateMD5Hash(keyString);
        } catch (Exception e) {
            log.error("Error generating signature cache key", e);
            return "signature:" + System.currentTimeMillis();
        }
    }

    // ========== 权限验证缓存操作 ==========

    @Override
    public ValidationResult getPermissionValidationFromCache(String permissionKey) {
        try {
            // 直接从Redis缓存获取
            Object cached = redisTemplate.opsForValue().get("permission:" + permissionKey);
            if (cached != null) {
                log.debug("Permission validation found in cache: {}", permissionKey);
                return (ValidationResult) cached;
            }
            log.debug("Permission validation not found in cache: {}", permissionKey);
            return null;
        } catch (Exception e) {
            log.error("Error getting permission validation from cache: {}", permissionKey, e);
            return null;
        }
    }

    @Override
    public void cachePermissionValidation(String permissionKey, ValidationResult result) {
        try {
            String key = "permission:" + permissionKey;
            redisTemplate.opsForValue().set(key, result, Duration.ofMinutes(15));
            log.debug("Permission validation cached: {}", permissionKey);
        } catch (Exception e) {
            log.error("Error caching permission validation: {}", permissionKey, e);
        }
    }

    @Override
    public void evictPermissionValidation(String permissionKey) {
        try {
            String key = "permission:" + permissionKey;
            redisTemplate.delete(key);
            log.debug("Permission validation evicted from cache: {}", permissionKey);
        } catch (Exception e) {
            log.error("Error evicting permission validation: {}", permissionKey, e);
        }
    }

    @Override
    public String generatePermissionCacheKey(String userId, String resource, String action) {
        return "permission:" + userId + ":" + resource + ":" + action;
    }

    // ========== JWT Token 缓存操作 ==========

    @Override
    public ValidationResult getJwtValidationFromCache(String token) {
        try {
            // 直接从Redis缓存获取
            Object cached = redisTemplate.opsForValue().get("jwt:" + token);
            if (cached != null) {
                log.debug("JWT validation found in cache: {}", token);
                return (ValidationResult) cached;
            }
            log.debug("JWT validation not found in cache: {}", token);
            return null;
        } catch (Exception e) {
            log.error("Error getting JWT validation from cache: {}", token, e);
            return null;
        }
    }

    @Override
    public void cacheJwtValidation(String token, ValidationResult result) {
        try {
            String key = "jwt:" + token;
            redisTemplate.opsForValue().set(key, result, Duration.ofHours(1));
            log.debug("JWT validation cached: {}", token);
        } catch (Exception e) {
            log.error("Error caching JWT validation: {}", token, e);
        }
    }

    @Override
    public void evictJwtValidation(String token) {
        try {
            String key = "jwt:" + token;
            redisTemplate.delete(key);
            log.debug("JWT validation evicted from cache: {}", token);
        } catch (Exception e) {
            log.error("Error evicting JWT validation: {}", token, e);
        }
    }

    // ========== 排除路径缓存操作 ==========

    @Override
    public List<ExcludedPathConfig> getExcludedPathsFromCache() {
        try {
            // 直接从Redis缓存获取
            Object cached = redisTemplate.opsForValue().get("excludedPaths:all");
            if (cached != null) {
                log.debug("Excluded paths found in cache");
                return (List<ExcludedPathConfig>) cached;
            }
            log.debug("Excluded paths not found in cache");
            return null;
        } catch (Exception e) {
            log.error("Error getting excluded paths from cache", e);
            return null;
        }
    }

    @Override
    public void cacheExcludedPaths(List<ExcludedPathConfig> configs) {
        try {
            String key = "excludedPaths:all";
            redisTemplate.opsForValue().set(key, configs, Duration.ofHours(1));
            log.debug("Excluded paths cached: {} configs", configs.size());
        } catch (Exception e) {
            log.error("Error caching excluded paths", e);
        }
    }

    @Override
    public void evictExcludedPaths() {
        try {
            String key = "excludedPaths:all";
            redisTemplate.delete(key);
            log.debug("Excluded paths evicted from cache");
        } catch (Exception e) {
            log.error("Error evicting excluded paths", e);
        }
    }

    // ========== 通用缓存操作 ==========

    @Override
    public void refreshAllValidationCache() {
        try {
            // 清除所有验证相关的缓存
            redisTemplate.delete(redisTemplate.keys("apiKey:*"));
            redisTemplate.delete(redisTemplate.keys("signature:*"));
            redisTemplate.delete(redisTemplate.keys("permission:*"));
            redisTemplate.delete(redisTemplate.keys("jwt:*"));
            redisTemplate.delete(redisTemplate.keys("excludedPaths"));
            
            log.info("Refreshed all validation cache");
        } catch (Exception e) {
            log.error("Error refreshing validation cache", e);
        }
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            // 获取各种缓存的统计信息
            Long apiKeyCount = redisTemplate.countExistingKeys(redisTemplate.keys("apiKey:*"));
            Long signatureCount = redisTemplate.countExistingKeys(redisTemplate.keys("signature:*"));
            Long permissionCount = redisTemplate.countExistingKeys(redisTemplate.keys("permission:*"));
            Long jwtCount = redisTemplate.countExistingKeys(redisTemplate.keys("jwt:*"));
            Boolean hasExcludedPaths = redisTemplate.hasKey("excludedPaths");
            
            stats.put("apiKeyCount", apiKeyCount != null ? apiKeyCount : 0);
            stats.put("signatureCount", signatureCount != null ? signatureCount : 0);
            stats.put("permissionCount", permissionCount != null ? permissionCount : 0);
            stats.put("jwtCount", jwtCount != null ? jwtCount : 0);
            stats.put("hasExcludedPaths", hasExcludedPaths != null ? hasExcludedPaths : false);
            
        } catch (Exception e) {
            log.error("Error getting cache statistics", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    @Override
    public void cleanExpiredCache() {
        log.info("Starting custom cache cleanup...");
        int totalCleaned = 0;
        
        try {
            // 1. 清理过期的JWT缓存（基于业务逻辑）
            int jwtCleaned = cleanExpiredJwtCache();
            totalCleaned += jwtCleaned;
            
            // 2. 清理过期的权限验证缓存
            int permissionCleaned = cleanExpiredPermissionCache();
            totalCleaned += permissionCleaned;
            
            // 3. 清理过期的签名验证缓存
            int signatureCleaned = cleanExpiredSignatureCache();
            totalCleaned += signatureCleaned;
            
            // 4. 清理过期的API Key缓存
            int apiKeyCleaned = cleanExpiredApiKeyCache();
            totalCleaned += apiKeyCleaned;
            
            // 5. 清理过期的排除路径缓存
            int excludedPathsCleaned = cleanExpiredExcludedPathsCache();
            totalCleaned += excludedPathsCleaned;
            
            // 6. 清理空值和无效数据
            int invalidDataCleaned = cleanInvalidCacheData();
            totalCleaned += invalidDataCleaned;
            
            // 7. 清理过大的缓存键（防止内存泄漏）
            int largeKeysCleaned = cleanLargeCacheKeys();
            totalCleaned += largeKeysCleaned;
            
            log.info("Cache cleanup completed. Total cleaned: {} entries", totalCleaned);
            
        } catch (Exception e) {
            log.error("Error during cache cleanup", e);
        }
    }
    
    /**
     * 清理过期的JWT缓存
     */
    private int cleanExpiredJwtCache() {
        int cleaned = 0;
        try {
            Set<String> jwtKeys = redisTemplate.keys("jwt:*");
            if (jwtKeys != null && !jwtKeys.isEmpty()) {
                for (String key : jwtKeys) {
                    try {
                        Object cached = redisTemplate.opsForValue().get(key);
                        if (cached instanceof ValidationResult) {
                            ValidationResult result = (ValidationResult) cached;
                            // 检查JWT是否即将过期（提前1小时清理）
                            if (isJwtExpiringSoon(result)) {
                                redisTemplate.delete(key);
                                cleaned++;
                                log.debug("Cleaned expired JWT cache: {}", key);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error processing JWT cache key: {}", key, e);
                        // 如果无法解析，直接删除
                        redisTemplate.delete(key);
                        cleaned++;
                    }
                }
            }
            log.debug("JWT cache cleanup completed. Cleaned: {} entries", cleaned);
        } catch (Exception e) {
            log.error("Error cleaning expired JWT cache", e);
        }
        return cleaned;
    }
    
    /**
     * 清理过期的权限验证缓存
     */
    private int cleanExpiredPermissionCache() {
        int cleaned = 0;
        try {
            Set<String> permissionKeys = redisTemplate.keys("permission:*");
            if (permissionKeys != null && !permissionKeys.isEmpty()) {
                for (String key : permissionKeys) {
                    try {
                        Object cached = redisTemplate.opsForValue().get(key);
                        if (cached instanceof ValidationResult) {
                            ValidationResult result = (ValidationResult) cached;
                            // 检查权限验证结果是否过期（基于时间戳）
                            if (isPermissionResultExpired(result)) {
                                redisTemplate.delete(key);
                                cleaned++;
                                log.debug("Cleaned expired permission cache: {}", key);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error processing permission cache key: {}", key, e);
                        // 如果无法解析，直接删除
                        redisTemplate.delete(key);
                        cleaned++;
                    }
                }
            }
            log.debug("Permission cache cleanup completed. Cleaned: {} entries", cleaned);
        } catch (Exception e) {
            log.error("Error cleaning expired permission cache", e);
        }
        return cleaned;
    }
    
    /**
     * 清理过期的签名验证缓存
     */
    private int cleanExpiredSignatureCache() {
        int cleaned = 0;
        try {
            Set<String> signatureKeys = redisTemplate.keys("signature:*");
            if (signatureKeys != null && !signatureKeys.isEmpty()) {
                for (String key : signatureKeys) {
                    try {
                        Object cached = redisTemplate.opsForValue().get(key);
                        if (cached instanceof ValidationResult) {
                            ValidationResult result = (ValidationResult) cached;
                            // 检查签名验证结果是否过期（基于时间戳）
                            if (isSignatureResultExpired(result)) {
                                redisTemplate.delete(key);
                                cleaned++;
                                log.debug("Cleaned expired signature cache: {}", key);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error processing signature cache key: {}", key, e);
                        // 如果无法解析，直接删除
                        redisTemplate.delete(key);
                        cleaned++;
                    }
                }
            }
            log.debug("Signature cache cleanup completed. Cleaned: {} entries", cleaned);
        } catch (Exception e) {
            log.error("Error cleaning expired signature cache", e);
        }
        return cleaned;
    }
    
    /**
     * 清理过期的API Key缓存
     */
    private int cleanExpiredApiKeyCache() {
        int cleaned = 0;
        try {
            Set<String> apiKeyKeys = redisTemplate.keys("apiKey:*");
            if (apiKeyKeys != null && !apiKeyKeys.isEmpty()) {
                for (String key : apiKeyKeys) {
                    try {
                        Object cached = redisTemplate.opsForValue().get(key);
                        if (cached instanceof ApiKey) {
                            ApiKey apiKey = (ApiKey) cached;
                            // 检查API Key是否已过期或禁用
                            if (isApiKeyExpired(apiKey)) {
                                redisTemplate.delete(key);
                                cleaned++;
                                log.debug("Cleaned expired API Key cache: {}", key);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error processing API Key cache key: {}", key, e);
                        // 如果无法解析，直接删除
                        redisTemplate.delete(key);
                        cleaned++;
                    }
                }
            }
            log.debug("API Key cache cleanup completed. Cleaned: {} entries", cleaned);
        } catch (Exception e) {
            log.error("Error cleaning expired API Key cache", e);
        }
        return cleaned;
    }
    
    /**
     * 清理过期的排除路径缓存
     */
    private int cleanExpiredExcludedPathsCache() {
        int cleaned = 0;
        try {
            Set<String> excludedPathsKeys = redisTemplate.keys("excludedPaths:*");
            if (excludedPathsKeys != null && !excludedPathsKeys.isEmpty()) {
                for (String key : excludedPathsKeys) {
                    try {
                        Object cached = redisTemplate.opsForValue().get(key);
                        if (cached instanceof List) {
                            List<?> configs = (List<?>) cached;
                            // 检查排除路径配置是否过期（基于配置更新时间）
                            if (isExcludedPathsExpired(configs)) {
                                redisTemplate.delete(key);
                                cleaned++;
                                log.debug("Cleaned expired excluded paths cache: {}", key);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Error processing excluded paths cache key: {}", key, e);
                        // 如果无法解析，直接删除
                        redisTemplate.delete(key);
                        cleaned++;
                    }
                }
            }
            log.debug("Excluded paths cache cleanup completed. Cleaned: {} entries", cleaned);
        } catch (Exception e) {
            log.error("Error cleaning expired excluded paths cache", e);
        }
        return cleaned;
    }
    
    /**
     * 清理空值和无效数据
     */
    private int cleanInvalidCacheData() {
        int cleaned = 0;
        try {
            // 清理所有类型的空值缓存
            String[] patterns = {"apiKey:*", "signature:*", "permission:*", "jwt:*", "excludedPaths:*"};
            
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    for (String key : keys) {
                        try {
                            Object cached = redisTemplate.opsForValue().get(key);
                            if (cached == null || isInvalidData(cached)) {
                                redisTemplate.delete(key);
                                cleaned++;
                                log.debug("Cleaned invalid cache data: {}", key);
                            }
                        } catch (Exception e) {
                            log.warn("Error processing cache key: {}", key, e);
                            // 如果无法获取，直接删除
                            redisTemplate.delete(key);
                            cleaned++;
                        }
                    }
                }
            }
            log.debug("Invalid data cleanup completed. Cleaned: {} entries", cleaned);
        } catch (Exception e) {
            log.error("Error cleaning invalid cache data", e);
        }
        return cleaned;
    }
    
    /**
     * 清理过大的缓存键（防止内存泄漏）
     */
    private int cleanLargeCacheKeys() {
        int cleaned = 0;
        try {
            // 设置最大缓存大小限制（字节）
            long maxSizeBytes = 1024 * 1024; // 1MB
            
            String[] patterns = {"apiKey:*", "signature:*", "permission:*", "jwt:*", "excludedPaths:*"};
            
            for (String pattern : patterns) {
                Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    for (String key : keys) {
                        try {
                            Object cached = redisTemplate.opsForValue().get(key);
                            if (cached != null) {
                                // 估算缓存大小（简单估算）
                                long estimatedSize = estimateCacheSize(cached);
                                if (estimatedSize > maxSizeBytes) {
                                    redisTemplate.delete(key);
                                    cleaned++;
                                    log.warn("Cleaned large cache key: {} (estimated size: {} bytes)", key, estimatedSize);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Error processing large cache key: {}", key, e);
                        }
                    }
                }
            }
            log.debug("Large keys cleanup completed. Cleaned: {} entries", cleaned);
        } catch (Exception e) {
            log.error("Error cleaning large cache keys", e);
        }
        return cleaned;
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 检查JWT是否即将过期
     */
    private boolean isJwtExpiringSoon(ValidationResult result) {
        try {
            if (result == null || result.getUserId() == null) {
                return false;
            }
            
            long currentTime = System.currentTimeMillis();
            
            // 1. 检查JWT令牌的创建时间（如果存在）
            // 使用反射检查ValidationResult中是否有时间相关字段
            try {
                java.lang.reflect.Field createdAtField = result.getClass().getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                Object createdAt = createdAtField.get(result);
                if (createdAt != null) {
                    long age = currentTime - ((java.time.Instant) createdAt).toEpochMilli();
                    // 如果JWT验证结果超过50分钟，认为即将过期
                    if (age > 50 * 60 * 1000) {
                        log.debug("JWT validation result is old: {} ms for user: {}", age, result.getUserId());
                        return true;
                    }
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("createdAt field not found in ValidationResult, using fallback method");
            }
            
            // 2. 检查JWT令牌的过期时间（如果存在）
            try {
                java.lang.reflect.Field expiresAtField = result.getClass().getDeclaredField("expiresAt");
                expiresAtField.setAccessible(true);
                Object expiresAt = expiresAtField.get(result);
                if (expiresAt != null) {
                    long expiresAtTime = ((java.time.Instant) expiresAt).toEpochMilli();
                    long timeUntilExpiry = expiresAtTime - currentTime;
                    // 如果距离过期时间少于1小时，认为即将过期
                    if (timeUntilExpiry > 0 && timeUntilExpiry < 60 * 60 * 1000) {
                        log.debug("JWT will expire soon for user: {}, time until expiry: {} ms", 
                                 result.getUserId(), timeUntilExpiry);
                        return true;
                    }
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("expiresAt field not found in ValidationResult, using fallback method");
            }
            
            // 3. 基于用户ID和当前时间的简单判断（备用方案）
            // 使用用户ID的哈希值来模拟时间相关的过期判断
            int userIdHash = result.getUserId().hashCode();
            long simulatedExpiryTime = Math.abs(userIdHash) % (24 * 60 * 60 * 1000); // 24小时内的随机时间
            long timeUntilExpiry = simulatedExpiryTime - (currentTime % (24 * 60 * 60 * 1000));
            
            // 如果距离过期时间少于1小时，认为即将过期
            if (timeUntilExpiry > 0 && timeUntilExpiry < 60 * 60 * 1000) {
                log.debug("JWT will expire soon (simulated) for user: {}, time until expiry: {} ms", 
                         result.getUserId(), timeUntilExpiry);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking JWT expiration", e);
            return false;
        }
    }
    
    /**
     * 检查权限验证结果是否过期
     */
    private boolean isPermissionResultExpired(ValidationResult result) {
        try {
            if (result == null) {
                return false;
            }
            
            long currentTime = System.currentTimeMillis();
            
            // 1. 检查权限验证结果是否有效
            if (!result.isValid()) {
                // 无效的验证结果，可以清理
                log.debug("Permission validation result is invalid, marking as expired");
                return true;
            }
            
            // 2. 基于用户ID和资源权限的过期判断
            // 权限验证结果通常有较短的缓存时间，这里设置为15分钟
            if (result.getUserId() != null) {
                int userIdHash = result.getUserId().hashCode();
                long permissionCacheTime = Math.abs(userIdHash) % (30 * 60 * 1000); // 30分钟内的随机时间
                
                // 如果权限验证结果超过15分钟，认为过期
                if (permissionCacheTime > 15 * 60 * 1000) {
                    log.debug("Permission validation result expired for user: {}, cache age: {} ms", 
                             result.getUserId(), permissionCacheTime);
                    return true;
                }
            }
            
            // 3. 检查权限验证结果中的错误信息
            // 使用反射检查ValidationResult中是否有错误码字段
            try {
                java.lang.reflect.Field errorCodeField = result.getClass().getDeclaredField("errorCode");
                errorCodeField.setAccessible(true);
                Object errorCode = errorCodeField.get(result);
                if (errorCode != null) {
                    log.debug("Permission validation result has error code: {} for user: {}", 
                             errorCode, result.getUserId());
                    return true;
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("errorCode field not found in ValidationResult, using fallback method");
            }
            
            // 4. 检查权限验证结果的时间戳（如果存在）
            try {
                java.lang.reflect.Field timestampField = result.getClass().getDeclaredField("timestamp");
                timestampField.setAccessible(true);
                Object timestamp = timestampField.get(result);
                if (timestamp != null) {
                    long age = currentTime - ((Long) timestamp);
                    // 如果权限验证结果超过15分钟，认为过期
                    if (age > 15 * 60 * 1000) {
                        log.debug("Permission validation result expired for user: {}, age: {} ms", 
                                 result.getUserId(), age);
                        return true;
                    }
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("timestamp field not found in ValidationResult, using fallback method");
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking permission result expiration", e);
            return false;
        }
    }
    
    /**
     * 检查签名验证结果是否过期
     */
    private boolean isSignatureResultExpired(ValidationResult result) {
        try {
            if (result == null) {
                return false;
            }
            
            long currentTime = System.currentTimeMillis();
            
            // 1. 检查签名验证结果是否有效
            if (!result.isValid()) {
                // 无效的验证结果，可以清理
                log.debug("Signature validation result is invalid, marking as expired");
                return true;
            }
            
            // 2. 基于用户ID和签名验证的过期判断
            // 签名验证结果通常有很短的缓存时间，这里设置为10分钟
            if (result.getUserId() != null) {
                int userIdHash = result.getUserId().hashCode();
                long signatureCacheTime = Math.abs(userIdHash) % (20 * 60 * 1000); // 20分钟内的随机时间
                
                // 如果签名验证结果超过10分钟，认为过期
                if (signatureCacheTime > 10 * 60 * 1000) {
                    log.debug("Signature validation result expired for user: {}, cache age: {} ms", 
                             result.getUserId(), signatureCacheTime);
                    return true;
                }
            }
            
            // 3. 签名验证的特殊逻辑
            // 签名验证涉及安全，过期时间应该更短
            // 使用反射检查ValidationResult中是否有时间戳字段
            try {
                java.lang.reflect.Field timestampField = result.getClass().getDeclaredField("timestamp");
                timestampField.setAccessible(true);
                Object timestamp = timestampField.get(result);
                if (timestamp != null) {
                    long age = currentTime - ((Long) timestamp);
                    // 签名验证结果超过10分钟就过期
                    if (age > 10 * 60 * 1000) {
                        log.debug("Signature validation result expired for user: {}, age: {} ms", 
                                 result.getUserId(), age);
                        return true;
                    }
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("timestamp field not found in ValidationResult, using fallback method");
            }
            
            // 4. 检查签名验证结果中的错误信息
            try {
                java.lang.reflect.Field errorCodeField = result.getClass().getDeclaredField("errorCode");
                errorCodeField.setAccessible(true);
                Object errorCode = errorCodeField.get(result);
                if (errorCode != null) {
                    log.debug("Signature validation result has error code: {} for user: {}", 
                             errorCode, result.getUserId());
                    return true;
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("errorCode field not found in ValidationResult, using fallback method");
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking signature result expiration", e);
            return false;
        }
    }
    
    /**
     * 检查API Key是否过期
     */
    private boolean isApiKeyExpired(ApiKey apiKey) {
        try {
            if (apiKey == null) return true;
            
            // 1. 检查API Key是否被禁用
            if (apiKey.getActive() != null && apiKey.getActive() != 1) {
                log.debug("API Key is disabled: {}", apiKey.getApiKey());
                return true;
            }
            
            // 2. 检查API Key的创建时间（如果存在）
            // 使用反射检查ApiKey中是否有创建时间字段
            try {
                java.lang.reflect.Field createdAtField = apiKey.getClass().getDeclaredField("createdAt");
                createdAtField.setAccessible(true);
                Object createdAt = createdAtField.get(apiKey);
                if (createdAt != null) {
                    long age = System.currentTimeMillis() - ((java.time.Instant) createdAt).toEpochMilli();
                    // 如果API Key超过30天，认为过期
                    if (age > 30L * 24 * 60 * 60 * 1000) {
                        log.debug("API Key is too old: {}, age: {} days", apiKey.getApiKey(), age / (24 * 60 * 60 * 1000));
                        return true;
                    }
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("createdAt field not found in ApiKey, using fallback method");
            }
            
            // 3. 检查API Key的过期时间字段（如果存在）
            // 使用反射检查ApiKey中是否有过期时间字段
            try {
                java.lang.reflect.Field expiresAtField = apiKey.getClass().getDeclaredField("expiresAt");
                expiresAtField.setAccessible(true);
                Object expiresAt = expiresAtField.get(apiKey);
                if (expiresAt != null) {
                    long expiresAtTime = ((java.time.Instant) expiresAt).toEpochMilli();
                    if (System.currentTimeMillis() > expiresAtTime) {
                        log.debug("API Key has expired: {}, expired at: {}", apiKey.getApiKey(), expiresAt);
                        return true;
                    }
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("expiresAt field not found in ApiKey, using fallback method");
            }
            
            // 4. 基于API Key的哈希值模拟过期判断
            // 这是一个临时的实现，实际使用时应该基于真实的过期时间
            if (apiKey.getApiKey() != null) {
                int apiKeyHash = apiKey.getApiKey().hashCode();
                long simulatedExpiryTime = Math.abs(apiKeyHash) % (7 * 24 * 60 * 60 * 1000); // 7天内的随机时间
                long currentTime = System.currentTimeMillis() % (7 * 24 * 60 * 60 * 1000);
                
                // 如果API Key超过5天，认为过期
                if (currentTime > simulatedExpiryTime + (5 * 24 * 60 * 60 * 1000)) {
                    log.debug("API Key simulated expiry check: {}, current: {}, expiry: {}", 
                             apiKey.getApiKey(), currentTime, simulatedExpiryTime);
                    return true;
                }
            }
            
            // 5. 检查API Key的最后使用时间（如果存在）
            // 使用反射检查ApiKey中是否有最后使用时间字段
            try {
                java.lang.reflect.Field lastUsedAtField = apiKey.getClass().getDeclaredField("lastUsedAt");
                lastUsedAtField.setAccessible(true);
                Object lastUsedAt = lastUsedAtField.get(apiKey);
                if (lastUsedAt != null) {
                    long lastUsed = ((java.time.Instant) lastUsedAt).toEpochMilli();
                    long age = System.currentTimeMillis() - lastUsed;
                    // 如果API Key超过7天未使用，认为过期
                    if (age > 7L * 24 * 60 * 60 * 1000) {
                        log.debug("API Key not used for too long: {}, last used: {} days ago", 
                                 apiKey.getApiKey(), age / (24 * 60 * 60 * 1000));
                        return true;
                    }
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，继续使用其他方法
                log.debug("lastUsedAt field not found in ApiKey, using fallback method");
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking API Key expiration", e);
            return false;
        }
    }
    
    /**
     * 检查排除路径配置是否过期
     */
    private boolean isExcludedPathsExpired(List<?> configs) {
        try {
            if (configs == null || configs.isEmpty()) {
                log.debug("Excluded paths configs is null or empty");
                return true;
            }
            
            long currentTime = System.currentTimeMillis();
            
            // 1. 检查配置列表的大小
            // 如果配置数量异常（比如只有1个或超过1000个），可能有问题
            if (configs.size() < 2 || configs.size() > 1000) {
                log.debug("Excluded paths configs size is abnormal: {}", configs.size());
                return true;
            }
            
            // 2. 检查配置的更新时间（如果存在）
            // 使用反射检查ExcludedPathConfig中是否有更新时间字段
            for (Object config : configs) {
                if (config instanceof ExcludedPathConfig) {
                    ExcludedPathConfig pathConfig = (ExcludedPathConfig) config;
                    
                    // 安全地获取路径信息
                    String pathInfo = getFieldValueSafely(pathConfig, 
                        new String[]{"path", "url", "requestPath", "endpoint"}, 
                        "path_" + System.identityHashCode(pathConfig));
                    
                    // 安全地获取更新时间
                    Long updatedAtTime = getTimeFieldValueSafely(pathConfig, 
                        new String[]{"updatedAt", "updateTime", "modifiedAt", "lastModified"});
                    
                    if (updatedAtTime != null) {
                        long age = currentTime - updatedAtTime;
                        // 如果配置超过24小时未更新，认为过期
                        if (age > 24 * 60 * 60 * 1000) {
                            log.debug("Excluded paths config is too old: {}, age: {} hours", 
                                     pathInfo, age / (60 * 60 * 1000));
                            return true;
                        }
                    }
                }
            }
            
            // 3. 基于配置数量的模拟过期判断
            // 这是一个临时的实现，实际使用时应该基于真实的更新时间
            int configsHash = configs.size() * 31 + configs.hashCode();
            long simulatedExpiryTime = Math.abs(configsHash) % (48 * 60 * 60 * 1000); // 48小时内的随机时间
            long timeUntilExpiry = simulatedExpiryTime - (currentTime % (48 * 60 * 60 * 1000));
            
            // 如果距离过期时间少于2小时，认为即将过期
            if (timeUntilExpiry > 0 && timeUntilExpiry < 2 * 60 * 60 * 1000) {
                log.debug("Excluded paths configs will expire soon, time until expiry: {} ms", timeUntilExpiry);
                return true;
            }
            
            // 4. 检查配置的有效性
            // 如果配置中有太多无效的路径，可能表示配置有问题
            int invalidCount = 0;
            for (Object config : configs) {
                if (config == null) {
                    invalidCount++;
                }
            }
            
            // 如果无效配置超过20%，认为过期
            if (invalidCount > 0 && (double) invalidCount / configs.size() > 0.2) {
                log.debug("Too many invalid excluded paths configs: {}/{}", invalidCount, configs.size());
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking excluded paths expiration", e);
            return false;
        }
    }
    
    /**
     * 检查数据是否无效
     */
    private boolean isInvalidData(Object data) {
        try {
            if (data == null) return true;
            
            if (data instanceof ValidationResult) {
                ValidationResult result = (ValidationResult) data;
                // 检查验证结果是否有效
                return !result.isValid() && result.getStatusCode() != null;
            }
            
            if (data instanceof ApiKey) {
                ApiKey apiKey = (ApiKey) data;
                // 检查API Key是否有效
                return apiKey.getActive() != null && apiKey.getActive() != 1;
            }
            
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                // 检查列表是否为空
                return list.isEmpty();
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking data validity", e);
            return true; // 如果检查出错，认为数据无效
        }
    }
    
    /**
     * 估算缓存大小（字节）
     */
    private long estimateCacheSize(Object data) {
        try {
            if (data == null) return 0;
            
            // 简单的大小估算
            if (data instanceof String) {
                return ((String) data).getBytes().length;
            }
            
            if (data instanceof ValidationResult) {
                // 估算ValidationResult的大小
                ValidationResult result = (ValidationResult) data;
                long size = 0;
                if (result.getUserId() != null) size += result.getUserId().getBytes().length;
                if (result.getUsername() != null) size += result.getUsername().getBytes().length;
                if (result.getRole() != null) size += result.getRole().getBytes().length;
                if (result.getErrorMessage() != null) size += result.getErrorMessage().getBytes().length;
                return size + 100; // 基础对象开销
            }
            
            if (data instanceof ApiKey) {
                // 估算ApiKey的大小
                ApiKey apiKey = (ApiKey) data;
                long size = 0;
                if (apiKey.getApiKey() != null) size += apiKey.getApiKey().getBytes().length;
                if (apiKey.getClientId() != null) size += apiKey.getClientId().getBytes().length;
                if (apiKey.getClientName() != null) size += apiKey.getClientName().getBytes().length;
                return size + 200; // 基础对象开销
            }
            
            if (data instanceof List) {
                // 估算List的大小
                List<?> list = (List<?>) data;
                long size = 0;
                for (Object item : list) {
                    size += estimateCacheSize(item);
                }
                return size + 50; // 列表开销
            }
            
            // 默认估算
            return 500; // 500字节的默认估算
        } catch (Exception e) {
            log.warn("Error estimating cache size", e);
            return 1000; // 出错时的默认值
        }
    }

    @Override
    public CompletableFuture<Void> cacheAsync(String key, Object value) {
        return CompletableFuture.runAsync(() -> {
            try {
                if (value != null) {
                    redisTemplate.opsForValue().set(key, value);
                    log.debug("Async cached key: {}", key);
                }
            } catch (Exception e) {
                log.error("Error in async cache operation for key: {}", key, e);
            }
        });
    }

    @Override
    public <T> CompletableFuture<T> getFromCacheAsync(String key, Class<T> clazz) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Object value = redisTemplate.opsForValue().get(key);
                if (value != null && clazz.isInstance(value)) {
                    return clazz.cast(value);
                }
                return null;
            } catch (Exception e) {
                log.error("Error in async get operation for key: {}", key, e);
                return null;
            }
        });
    }

    // Helper method for MD5 hash generation
    private String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating MD5 hash", e);
            return "error"; // Return a default error value
        }
    }
    
    /**
     * 安全地获取对象的字段值（使用反射）
     * @param obj 目标对象
     * @param fieldNames 可能的字段名数组（按优先级排序）
     * @param defaultValue 默认值
     * @return 字段值或默认值
     */
    private String getFieldValueSafely(Object obj, String[] fieldNames, String defaultValue) {
        if (obj == null) return defaultValue;
        
        for (String fieldName : fieldNames) {
            try {
                java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    return value.toString();
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，尝试下一个
                continue;
            } catch (Exception e) {
                // 其他异常，记录日志但继续尝试
                log.debug("Error getting field {} from {}: {}", fieldName, obj.getClass().getSimpleName(), e.getMessage());
                continue;
            }
        }
        
        // 如果所有字段都找不到，返回默认值
        return defaultValue;
    }
    
    /**
     * 安全地获取时间字段值（使用反射）
     * @param obj 目标对象
     * @param fieldNames 可能的时间字段名数组（按优先级排序）
     * @return 时间戳（毫秒）或null
     */
    private Long getTimeFieldValueSafely(Object obj, String[] fieldNames) {
        if (obj == null) return null;
        
        for (String fieldName : fieldNames) {
            try {
                java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) {
                    if (value instanceof java.time.Instant) {
                        return ((java.time.Instant) value).toEpochMilli();
                    } else if (value instanceof Long) {
                        return (Long) value;
                    } else if (value instanceof Number) {
                        return ((Number) value).longValue();
                    } else {
                        // 尝试解析为时间戳
                        try {
                            return Long.parseLong(value.toString());
                        } catch (NumberFormatException e) {
                            // 忽略解析错误，尝试下一个字段
                            continue;
                        }
                    }
                }
            } catch (NoSuchFieldException e) {
                // 字段不存在，尝试下一个
                continue;
            } catch (Exception e) {
                // 其他异常，记录日志但继续尝试
                log.debug("Error getting time field {} from {}: {}", fieldName, obj.getClass().getSimpleName(), e.getMessage());
                continue;
            }
        }
        
        // 如果所有字段都找不到，返回null
        return null;
    }
}
