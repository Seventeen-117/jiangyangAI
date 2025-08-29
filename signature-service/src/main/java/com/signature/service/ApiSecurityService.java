package com.signature.service;

import java.util.List;
import java.util.Map;

/**
 * API 安全服务接口
 * 处理API密钥验证、权限管理等安全相关业务逻辑
 */
public interface ApiSecurityService {

    /**
     * 验证API密钥
     */
    boolean verifyApiKey(String apiKey);

    /**
     * 根据API密钥获取客户端ID
     */
    String getClientIdByApiKey(String apiKey);

    /**
     * 获取客户端权限
     */
    Map<String, Object> getClientPermissions(String clientId);

    /**
     * 检查权限
     */
    boolean checkPermission(String apiKey, String permission, String resource);

    /**
     * 获取客户端信息
     */
    Map<String, Object> getClientInfo(String clientId);

    /**
     * 获取所有客户端
     */
    Map<String, Object> getAllClients();

    /**
     * 创建API密钥
     */
    Map<String, Object> createApiKey(String clientId, String description);

    /**
     * 撤销API密钥
     */
    boolean revokeApiKey(String apiKey);

    /**
     * 获取API使用统计
     */
    Map<String, Object> getApiStats(String clientId, Integer days);

    /**
     * 获取限流信息
     */
    Map<String, Object> getRateLimitInfo(String clientId);

    /**
     * 更新客户端权限
     */
    boolean updateClientPermissions(String clientId, Map<String, Object> permissions);
} 