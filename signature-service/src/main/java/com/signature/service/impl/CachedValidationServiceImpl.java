package com.signature.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.signature.entity.ApiKey;
import com.signature.entity.ExcludedPathConfig;
import com.signature.mapper.ApiKeyMapper;
import com.signature.mapper.ExcludedPathConfigMapper;
import com.signature.model.ValidationRequest;
import com.signature.model.ValidationResult;
import com.signature.service.ValidationCacheService;
import com.signature.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 带缓存的验证服务实现类
 * 集成Redis缓存，减少数据库访问压力
 */
@Slf4j
@Service
public class CachedValidationServiceImpl implements ValidationService {

    @Autowired
    private ValidationCacheService cacheService;

    @Autowired
    private ApiKeyMapper apiKeyMapper;

    @Autowired
    private ExcludedPathConfigMapper excludedPathConfigMapper;

    @Autowired
    private com.signature.service.SignatureVerificationService signatureVerificationService;

    @Autowired
    private com.signature.service.PermissionService permissionService;

    @Autowired
    private com.signature.service.ApiKeyPermissionService apiKeyPermissionService;

    @Autowired
    private com.signature.service.ApiPermissionService apiPermissionService;

    @Autowired
    private com.signature.service.ClientPermissionService clientPermissionService;

    @Autowired
    private com.signature.utils.JwtUtils jwtUtils;

    // ========== 主要验证方法 ==========

    @Override
    public ValidationResult validateRequest(ValidationRequest request) {
        log.debug("Validating request: {}", request.getPath());

        // 1. 验证API Key
        ValidationResult apiKeyResult = validateApiKey(request.getApiKey());
        if (!apiKeyResult.isValid()) {
            return apiKeyResult;
        }

        // 2. 验证签名
        if (StringUtils.hasText(request.getSignature())) {
            ValidationResult signatureResult = validateSignature(request);
            if (!signatureResult.isValid()) {
                return signatureResult;
            }
        }

        // 3. 验证权限
        if (StringUtils.hasText(request.getUserId())) {
            ValidationResult permissionResult = validatePermission(
                request.getUserId(), request.getPath(), request.getMethod());
            if (!permissionResult.isValid()) {
                return permissionResult;
            }
        }

        return ValidationResult.success();
    }

    @Override
    public CompletableFuture<ValidationResult> validateRequestAsync(ValidationRequest request) {
        return CompletableFuture.supplyAsync(() -> validateRequest(request));
    }

    @Override
    public List<ValidationResult> validateRequests(List<ValidationRequest> requests) {
        return requests.parallelStream()
                .map(this::validateRequest)
                .collect(java.util.stream.Collectors.toList());
    }

    // ========== API Key 验证 ==========

    @Override
    public ValidationResult validateApiKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            return ValidationResult.failure("MISSING_API_KEY", "API Key is required");
        }

        try {
            // 1. 先从缓存获取
            ApiKey cachedApiKey = cacheService.getApiKeyFromCache(apiKey);
            if (cachedApiKey != null) {
                log.debug("API Key found in cache: {}", apiKey);
                return buildApiKeyValidationResult(cachedApiKey);
            }

            // 2. 缓存未命中，从数据库查询
            LambdaQueryWrapper<ApiKey> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApiKey::getApiKey, apiKey);
            ApiKey dbApiKey = apiKeyMapper.selectOne(queryWrapper);
            
            if (dbApiKey != null) {
                // 3. 将结果存储到缓存
                cacheService.cacheApiKey(apiKey, dbApiKey);
                log.debug("API Key cached from database: {}", apiKey);
                return buildApiKeyValidationResult(dbApiKey);
            }

            return ValidationResult.invalidApiKey();

        } catch (Exception e) {
            log.error("Error validating API Key: {}", apiKey, e);
            return ValidationResult.failure("VALIDATION_ERROR", "Error validating API Key");
        }
    }

    private ValidationResult buildApiKeyValidationResult(ApiKey apiKey) {
        if (apiKey == null || apiKey.getActive() != 1) {
            return ValidationResult.invalidApiKey();
        }

        return ValidationResult.success(apiKey.getClientId(), apiKey.getClientName(), "API_USER");
    }

    // ========== 签名验证 ==========

    @Override
    public ValidationResult validateSignature(ValidationRequest request) {
        if (!StringUtils.hasText(request.getSignature()) || !StringUtils.hasText(request.getApiKey())) {
            return ValidationResult.failure("MISSING_SIGNATURE", "Signature and API Key are required");
        }

        try {
            // 1. 生成缓存键
            Map<String, String> params = buildSignatureParams(request);
            String cacheKey = cacheService.generateSignatureCacheKey(params, request.getSignature(), request.getApiKey());

            // 2. 先从缓存获取
            ValidationResult cachedResult = cacheService.getSignatureValidationFromCache(cacheKey);
            if (cachedResult != null) {
                log.debug("Signature validation found in cache: {}", cacheKey);
                return cachedResult;
            }

            // 3. 缓存未命中，执行签名验证
            ValidationResult result = performSignatureValidation(request);
            
            // 4. 将结果存储到缓存
            cacheService.cacheSignatureValidation(cacheKey, result);
            log.debug("Signature validation cached: {}", cacheKey);

            return result;

        } catch (Exception e) {
            log.error("Error validating signature", e);
            return ValidationResult.failure("SIGNATURE_ERROR", "Error validating signature");
        }
    }

    private Map<String, String> buildSignatureParams(ValidationRequest request) {
        Map<String, String> params = new java.util.HashMap<>();
        
        // 添加基本参数
        params.put("path", request.getPath());
        params.put("method", request.getMethod());
        params.put("timestamp", String.valueOf(request.getTimestamp()));
        params.put("apiKey", request.getApiKey());
        
        // 添加应用ID
        if (request.getAppId() != null) {
            params.put("appId", request.getAppId());
        }
        
        // 添加随机数
        if (request.getNonce() != null) {
            params.put("nonce", request.getNonce());
        }
        
        // 添加请求参数
        if (request.getParameters() != null) {
            params.putAll(request.getParameters());
        }
        
        return params;
    }

    private ValidationResult performSignatureValidation(ValidationRequest request) {
        try {
            // 1. 构建签名验证参数
            Map<String, String> params = buildSignatureParams(request);
            
            // 2. 验证时间戳（5分钟有效期）
            String timestamp = request.getTimestamp() != null ? String.valueOf(request.getTimestamp()) : params.get("timestamp");
            if (!signatureVerificationService.validateTimestamp(timestamp, 300)) {
                log.warn("Timestamp validation failed for request: {}", request.getPath());
                return ValidationResult.failure("INVALID_TIMESTAMP", "Request timestamp is invalid or expired");
            }
            
            // 3. 验证nonce防重放攻击（如果提供了nonce）
            String nonce = params.get("nonce");
            if (nonce != null && !signatureVerificationService.validateNonce(nonce, 300)) {
                log.warn("Nonce validation failed for request: {}", request.getPath());
                return ValidationResult.failure("REPLAY_ATTACK", "Nonce already used, possible replay attack");
            }
            
            // 4. 验证签名
            String appId = params.get("appId");
            if (appId == null) {
                log.warn("AppId not found in request parameters");
                return ValidationResult.failure("MISSING_APP_ID", "AppId is required for signature verification");
            }
            
            boolean signatureValid = signatureVerificationService.verifySignature(params, request.getSignature(), appId);
            if (!signatureValid) {
                log.warn("Signature verification failed for appId: {}, path: {}", appId, request.getPath());
                return ValidationResult.invalidSignature();
            }
            
            // 5. 保存nonce到缓存（如果提供了nonce）
            if (nonce != null) {
                signatureVerificationService.saveNonce(nonce, 300);
            }
            
            log.debug("Signature validation successful for appId: {}, path: {}", appId, request.getPath());
            return ValidationResult.success();
            
        } catch (Exception e) {
            log.error("Error during signature validation for request: {}", request.getPath(), e);
            return ValidationResult.failure("SIGNATURE_ERROR", "Error during signature validation: " + e.getMessage());
        }
    }

    // ========== 权限验证 ==========

    @Override
    public ValidationResult validatePermission(String userId, String resource, String action) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(resource) || !StringUtils.hasText(action)) {
            return ValidationResult.failure("MISSING_PERMISSION_PARAMS", "User ID, resource and action are required");
        }

        try {
            // 1. 生成缓存键
            String cacheKey = cacheService.generatePermissionCacheKey(userId, resource, action);

            // 2. 先从缓存获取
            ValidationResult cachedResult = cacheService.getPermissionValidationFromCache(cacheKey);
            if (cachedResult != null) {
                log.debug("Permission validation found in cache: {}", cacheKey);
                return cachedResult;
            }

            // 3. 缓存未命中，从数据库查询权限
            ValidationResult result = performPermissionValidation(userId, resource, action);
            
            // 4. 将结果存储到缓存
            cacheService.cachePermissionValidation(cacheKey, result);
            log.debug("Permission validation cached: {}", cacheKey);

            return result;

        } catch (Exception e) {
            log.error("Error validating permission for user: {}, resource: {}, action: {}", userId, resource, action, e);
            return ValidationResult.failure("PERMISSION_ERROR", "Error validating permission");
        }
    }

    private ValidationResult performPermissionValidation(String userId, String resource, String action) {
        try {
            // 1. 验证参数
            if (!StringUtils.hasText(userId) || !StringUtils.hasText(resource) || !StringUtils.hasText(action)) {
                log.warn("Permission validation failed: missing required parameters");
                return ValidationResult.failure("MISSING_PERMISSION_PARAMS", "User ID, resource and action are required");
            }

            // 2. 构建权限代码
            String permissionCode = buildPermissionCode(resource, action);
            log.debug("Checking permission: userId={}, permissionCode={}", userId, permissionCode);

            // 3. 查找权限
            com.signature.entity.ApiPermission permission = apiPermissionService.findByPermissionCode(permissionCode);
            if (permission == null) {
                log.warn("Permission not found: {}", permissionCode);
                return ValidationResult.failure("PERMISSION_NOT_FOUND", "Permission not found: " + permissionCode);
            }

            // 4. 检查权限状态
            if (permission.getStatus() != 1) {
                log.warn("Permission is disabled: {}", permissionCode);
                return ValidationResult.failure("PERMISSION_DISABLED", "Permission is disabled: " + permissionCode);
            }

            // 5. 尝试从API Key ID验证权限
            Long apiKeyId = parseApiKeyId(userId);
            if (apiKeyId != null) {
                boolean hasPermission = apiKeyPermissionService.hasPermission(apiKeyId, permission.getId());
                if (hasPermission) {
                    log.debug("API Key permission granted: apiKeyId={}, permissionCode={}", apiKeyId, permissionCode);
                    return ValidationResult.success();
                }
            }

            // 6. 尝试从客户端ID验证权限
            boolean hasClientPermission = checkClientPermission(userId, permission.getId());
            if (hasClientPermission) {
                log.debug("Client permission granted: clientId={}, permissionCode={}", userId, permissionCode);
                return ValidationResult.success();
            }

            // 7. 检查通配符权限
            boolean hasWildcardPermission = checkWildcardPermission(userId, resource, action);
            if (hasWildcardPermission) {
                log.debug("Wildcard permission granted: userId={}, resource={}, action={}", userId, resource, action);
                return ValidationResult.success();
            }

            log.warn("Permission denied: userId={}, permissionCode={}", userId, permissionCode);
            return ValidationResult.failure("PERMISSION_DENIED", "Access denied for resource: " + resource + ":" + action);

        } catch (Exception e) {
            log.error("Error during permission validation for userId: {}, resource: {}, action: {}", userId, resource, action, e);
            return ValidationResult.failure("PERMISSION_ERROR", "Error during permission validation: " + e.getMessage());
        }
    }

    /**
     * 构建权限代码
     */
    private String buildPermissionCode(String resource, String action) {
        return resource + ":" + action;
    }

    /**
     * 尝试将userId解析为API Key ID
     */
    private Long parseApiKeyId(String userId) {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 检查客户端权限
     */
    private boolean checkClientPermission(String clientId, Long permissionId) {
        try {
            // 1. 验证参数
            if (!StringUtils.hasText(clientId) || permissionId == null) {
                log.warn("checkClientPermission: Invalid parameters - clientId: {}, permissionId: {}", clientId, permissionId);
                return false;
            }

            // 2. 使用ClientPermissionService检查权限
            boolean hasPermission = clientPermissionService.hasPermission(clientId, permissionId);
            
            log.debug("checkClientPermission: clientId={}, permissionId={}, hasPermission={}", 
                    clientId, permissionId, hasPermission);
            
            return hasPermission;
            
        } catch (Exception e) {
            log.error("Error checking client permission: clientId={}, permissionId={}", clientId, permissionId, e);
            return false;
        }
    }

    /**
     * 检查通配符权限
     */
    private boolean checkWildcardPermission(String userId, String resource, String action) {
        try {
            // 检查是否有 resource:* 权限
            String wildcardPermissionCode = resource + ":*";
            com.signature.entity.ApiPermission wildcardPermission = apiPermissionService.findByPermissionCode(wildcardPermissionCode);
            
            if (wildcardPermission != null && wildcardPermission.getStatus() == 1) {
                Long apiKeyId = parseApiKeyId(userId);
                if (apiKeyId != null) {
                    return apiKeyPermissionService.hasPermission(apiKeyId, wildcardPermission.getId());
                }
            }
            
            return false;
        } catch (Exception e) {
            log.warn("Error checking wildcard permission: userId={}, resource={}, action={}", userId, resource, action, e);
            return false;
        }
    }

    // ========== JWT Token 验证 ==========

    @Override
    public ValidationResult validateJwtToken(String token) {
        if (!StringUtils.hasText(token)) {
            return ValidationResult.failure("MISSING_JWT", "JWT Token is required");
        }

        try {
            // 1. 先从缓存获取
            ValidationResult cachedResult = cacheService.getJwtValidationFromCache(token);
            if (cachedResult != null) {
                log.debug("JWT validation found in cache: {}", token);
                return cachedResult;
            }

            // 2. 缓存未命中，执行JWT验证
            ValidationResult result = performJwtValidation(token);
            
            // 3. 将结果存储到缓存
            cacheService.cacheJwtValidation(token, result);
            log.debug("JWT validation cached: {}", token);

            return result;

        } catch (Exception e) {
            log.error("Error validating JWT token", e);
            return ValidationResult.failure("JWT_ERROR", "Error validating JWT token");
        }
    }

    private ValidationResult performJwtValidation(String token) {
        try {
            // 1. 验证参数
            if (!StringUtils.hasText(token)) {
                log.warn("JWT validation failed: token is empty");
                return ValidationResult.failure("MISSING_JWT", "JWT Token is required");
            }

            // 2. 使用JwtUtils验证JWT令牌
            com.signature.model.SsoUserInfo userInfo = jwtUtils.validateAccessToken(token);
            if (userInfo == null) {
                log.warn("JWT validation failed: invalid token");
                return ValidationResult.failure("INVALID_JWT", "Invalid or expired JWT token");
            }

            // 3. 检查用户状态
            if (userInfo.getEnabled() != null && !userInfo.getEnabled()) {
                log.warn("JWT validation failed: user is disabled, userId: {}", userInfo.getUserId());
                return ValidationResult.failure("USER_DISABLED", "User account is disabled");
            }

            if (userInfo.getLocked() != null && userInfo.getLocked()) {
                log.warn("JWT validation failed: user is locked, userId: {}", userInfo.getUserId());
                return ValidationResult.failure("USER_LOCKED", "User account is locked");
            }

            // 4. 检查账户过期时间
            if (userInfo.getAccountExpiredAt() != null && userInfo.getAccountExpiredAt() < System.currentTimeMillis()) {
                log.warn("JWT validation failed: user account expired, userId: {}", userInfo.getUserId());
                return ValidationResult.failure("ACCOUNT_EXPIRED", "User account has expired");
            }

            // 5. 检查密码过期时间
            if (userInfo.getPasswordExpiredAt() != null && userInfo.getPasswordExpiredAt() < System.currentTimeMillis()) {
                log.warn("JWT validation failed: user password expired, userId: {}", userInfo.getUserId());
                return ValidationResult.failure("PASSWORD_EXPIRED", "User password has expired");
            }

            // 6. 检查令牌是否即将过期（提前5分钟警告）
            if (jwtUtils.isTokenExpiringSoon(token, 300)) {
                log.warn("JWT token will expire soon, userId: {}", userInfo.getUserId());
                // 这里可以选择返回警告信息，但不阻止访问
                // return ValidationResult.failure("TOKEN_EXPIRING_SOON", "JWT token will expire soon");
            }

            log.debug("JWT validation successful for user: {}", userInfo.getUserId());
            return ValidationResult.success(userInfo.getUserId(), userInfo.getUsername(), userInfo.getRole());

        } catch (Exception e) {
            log.error("Error during JWT validation for token: {}", token, e);
            return ValidationResult.failure("JWT_ERROR", "Error during JWT validation: " + e.getMessage());
        }
    }

    // ========== 缓存管理 ==========

    @Override
    public void refreshValidationCache() {
        log.info("Refreshing validation cache...");
        cacheService.refreshAllValidationCache();
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        return cacheService.getCacheStatistics();
    }

    /**
     * 预热缓存
     */
    public void warmUpCache() {
        log.info("Warming up validation cache...");
        
        try {
            // 预热API Key缓存
            LambdaQueryWrapper<ApiKey> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ApiKey::getActive, 1);
            List<ApiKey> apiKeys = apiKeyMapper.selectList(queryWrapper);
            
            Map<String, ApiKey> apiKeyMap = apiKeys.stream()
                    .collect(java.util.stream.Collectors.toMap(ApiKey::getApiKey, apiKey -> apiKey));
            cacheService.cacheApiKeys(apiKeyMap);
            
            // 预热排除路径缓存
            List<ExcludedPathConfig> excludedPaths = excludedPathConfigMapper.selectAllEnabled();
            cacheService.cacheExcludedPaths(excludedPaths);
            
            log.info("Cache warm-up completed. Cached {} API Keys and {} excluded paths", 
                    apiKeyMap.size(), excludedPaths.size());
                    
        } catch (Exception e) {
            log.error("Error warming up cache", e);
        }
    }
}
