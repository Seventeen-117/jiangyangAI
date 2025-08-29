package com.signature.service;

import com.signature.entity.ApiKey;
import com.signature.entity.ExcludedPathConfig;
import com.signature.model.ValidationResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 验证缓存服务接口
 * 负责管理签名验证、API Key验证、权限验证等相关的缓存操作
 */
public interface ValidationCacheService {

    // ========== API Key 缓存操作 ==========
    
    /**
     * 从缓存获取API Key信息
     * @param apiKey API Key
     * @return API Key信息，如果缓存不存在则返回null
     */
    ApiKey getApiKeyFromCache(String apiKey);
    
    /**
     * 将API Key信息存储到缓存
     * @param apiKey API Key
     * @param apiKeyInfo API Key信息
     */
    void cacheApiKey(String apiKey, ApiKey apiKeyInfo);
    
    /**
     * 从缓存中删除API Key
     * @param apiKey API Key
     */
    void evictApiKey(String apiKey);
    
    /**
     * 批量缓存API Key信息
     * @param apiKeyMap API Key映射
     */
    void cacheApiKeys(Map<String, ApiKey> apiKeyMap);
    
    /**
     * 检查API Key是否在缓存中
     * @param apiKey API Key
     * @return 是否在缓存中
     */
    boolean isApiKeyCached(String apiKey);

    // ========== 签名验证缓存操作 ==========
    
    /**
     * 从缓存获取签名验证结果
     * @param signatureKey 签名缓存键
     * @return 验证结果，如果缓存不存在则返回null
     */
    ValidationResult getSignatureValidationFromCache(String signatureKey);
    
    /**
     * 将签名验证结果存储到缓存
     * @param signatureKey 签名缓存键
     * @param result 验证结果
     */
    void cacheSignatureValidation(String signatureKey, ValidationResult result);
    
    /**
     * 从缓存中删除签名验证结果
     * @param signatureKey 签名缓存键
     */
    void evictSignatureValidation(String signatureKey);
    
    /**
     * 生成签名验证缓存键
     * @param params 参数
     * @param sign 签名
     * @param appId 应用ID
     * @return 缓存键
     */
    String generateSignatureCacheKey(Map<String, String> params, String sign, String appId);

    // ========== 权限验证缓存操作 ==========
    
    /**
     * 从缓存获取权限验证结果
     * @param permissionKey 权限缓存键
     * @return 验证结果，如果缓存不存在则返回null
     */
    ValidationResult getPermissionValidationFromCache(String permissionKey);
    
    /**
     * 将权限验证结果存储到缓存
     * @param permissionKey 权限缓存键
     * @param result 验证结果
     */
    void cachePermissionValidation(String permissionKey, ValidationResult result);
    
    /**
     * 从缓存中删除权限验证结果
     * @param permissionKey 权限缓存键
     */
    void evictPermissionValidation(String permissionKey);
    
    /**
     * 生成权限验证缓存键
     * @param userId 用户ID
     * @param resource 资源
     * @param action 操作
     * @return 缓存键
     */
    String generatePermissionCacheKey(String userId, String resource, String action);

    // ========== JWT Token 缓存操作 ==========
    
    /**
     * 从缓存获取JWT验证结果
     * @param token JWT Token
     * @return 验证结果，如果缓存不存在则返回null
     */
    ValidationResult getJwtValidationFromCache(String token);
    
    /**
     * 将JWT验证结果存储到缓存
     * @param token JWT Token
     * @param result 验证结果
     */
    void cacheJwtValidation(String token, ValidationResult result);
    
    /**
     * 从缓存中删除JWT验证结果
     * @param token JWT Token
     */
    void evictJwtValidation(String token);

    // ========== 排除路径缓存操作 ==========
    
    /**
     * 从缓存获取排除路径配置
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> getExcludedPathsFromCache();
    
    /**
     * 将排除路径配置存储到缓存
     * @param configs 配置列表
     */
    void cacheExcludedPaths(List<ExcludedPathConfig> configs);
    
    /**
     * 从缓存中删除排除路径配置
     */
    void evictExcludedPaths();

    // ========== 通用缓存操作 ==========
    
    /**
     * 刷新所有验证缓存
     */
    void refreshAllValidationCache();
    
    /**
     * 获取缓存统计信息
     * @return 统计信息
     */
    Map<String, Object> getCacheStatistics();
    
    /**
     * 清理过期缓存
     */
    void cleanExpiredCache();
    
    /**
     * 异步缓存操作
     * @param key 缓存键
     * @param value 缓存值
     * @return 异步结果
     */
    CompletableFuture<Void> cacheAsync(String key, Object value);
    
    /**
     * 异步获取缓存
     * @param key 缓存键
     * @param clazz 类型
     * @return 异步结果
     */
    <T> CompletableFuture<T> getFromCacheAsync(String key, Class<T> clazz);
}
