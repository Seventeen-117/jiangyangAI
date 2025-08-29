package com.signature.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiangyang.base.datasource.annotation.DataSource;
import com.signature.model.ValidationRequest;
import com.signature.model.ValidationResult;
import com.signature.service.AsyncValidationService;
import com.signature.service.DynamicConfigService;
import com.signature.service.ExcludedPathService;
import com.signature.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一安全过滤器（WebFlux）
 * 整合API Key验证、JWT认证、签名验证、权限验证等功能
 * 支持同步和异步验证模式
 */
@Slf4j
@Component
@DataSource("master")
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // 在CORS过滤器之后执行
public class UnifiedSecurityFilter implements WebFilter, Ordered {

    @Autowired
    @Qualifier("cachedValidationServiceImpl")
    private ValidationService validationService;

    @Autowired
    private AsyncValidationService asyncValidationService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Autowired
    private ExcludedPathService excludedPathService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        log.debug("UnifiedSecurityFilter processing: {} {}", method, path);
        
        // 预检OPTIONS请求直接通过
        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.debug("OPTIONS request, skipping security validation: {}", path);
            return chain.filter(exchange);
        }

        // 跳过不需要验证的路径
        if (isSkipValidation(path, method)) {
            log.debug("Skipping security validation for path: {} {}", method, path);
            return chain.filter(exchange);
        }

        // 检查是否启用异步验证
        if (dynamicConfigService.isAsyncValidationEnabled()) {
            return performAsyncValidation(exchange, chain);
        } else {
            return performSyncValidation(exchange, chain);
        }
    }

    /**
     * 执行异步验证
     */
    private Mono<Void> performAsyncValidation(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 构建验证请求
        ValidationRequest validationRequest = buildValidationRequest(request);
        
        return asyncValidationService.validateAsyncWithConfig(validationRequest)
            .flatMap(result -> {
                if (result.isAllValid()) {
                    log.debug("Async validation passed for path: {}", path);
                    // 将验证结果添加到请求属性中
                    exchange.getAttributes().put("asyncValidationResult", result);
                    return chain.filter(exchange);
                } else {
                    log.warn("Async validation failed for path: {} - {}", path, result.getCombinedErrorMessage());
                    return sendValidationFailedResponse(exchange.getResponse(), result);
                }
            })
            .onErrorResume(e -> {
                log.error("Async validation error for path: {}", path, e);
                return sendValidationFailedResponse(exchange.getResponse(), 
                    new AsyncValidationService.AsyncValidationResult(
                        false, false, false,
                        "验证服务异常", "验证服务异常", "验证服务异常", 0
                    ));
            });
    }

    /**
     * 执行同步验证
     */
    private Mono<Void> performSyncValidation(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();

        // 1. API Key验证
        if (dynamicConfigService.isApiKeyAuthValidationEnabled()) {
            String apiKey = extractApiKey(request);
            if (!StringUtils.hasText(apiKey)) {
                log.warn("API Key not found in request: {}", path);
                return sendUnauthorizedResponse(response, "API Key is required");
            }
            
            // 检查是否为内部服务API Key
            if (dynamicConfigService.isInternalServiceApiKey(apiKey)) {
                log.debug("Internal service API Key detected: {}", apiKey);
                // 内部服务API Key可以跳过某些验证
                exchange.getAttributes().put("isInternalService", true);
            }
            
            ValidationResult apiKeyResult = validationService.validateApiKey(apiKey);
            if (!apiKeyResult.isValid()) {
                log.warn("API Key validation failed: {}, reason: {}", apiKey, apiKeyResult.getErrorMessage());
                return sendUnauthorizedResponse(response, apiKeyResult.getErrorMessage());
            }
            
            // 将用户信息添加到请求属性中
            exchange.getAttributes().put("userId", apiKeyResult.getUserId());
            exchange.getAttributes().put("username", apiKeyResult.getUsername());
            exchange.getAttributes().put("role", apiKeyResult.getRole());
            exchange.getAttributes().put("clientId", apiKeyResult.getClientId());
        }

        // 2. JWT Token验证
        if (dynamicConfigService.isJwtValidationEnabled()) {
            String jwtToken = extractJwtToken(request);
            if (StringUtils.hasText(jwtToken)) {
                ValidationResult jwtResult = validationService.validateJwtToken(jwtToken);
                if (!jwtResult.isValid()) {
                    log.warn("JWT validation failed for path: {}, reason: {}", path, jwtResult.getErrorMessage());
                    return sendUnauthorizedResponse(response, jwtResult.getErrorMessage());
                }
            }
        }

        // 3. 签名验证
        if (dynamicConfigService.isSignatureValidationEnabled()) {
            String signature = extractSignature(request);
            if (StringUtils.hasText(signature)) {
                ValidationRequest validationRequest = buildValidationRequest(request);
                ValidationResult signatureResult = validationService.validateSignature(validationRequest);
                if (!signatureResult.isValid()) {
                    log.warn("Signature validation failed for path: {}, reason: {}", path, signatureResult.getErrorMessage());
                    return sendUnauthorizedResponse(response, signatureResult.getErrorMessage());
                }
            }
        }

        // 4. 权限验证
        if (dynamicConfigService.isPermissionValidationEnabled()) {
            // 检查是否为内部服务调用，内部服务可以跳过权限验证
            Boolean isInternalService = (Boolean) exchange.getAttributes().get("isInternalService");
            if (isInternalService != null && isInternalService) {
                log.debug("Skipping permission validation for internal service call: {}", path);
            } else {
                String userId = (String) exchange.getAttributes().get("userId");
                if (StringUtils.hasText(userId)) {
                    ValidationResult permissionResult = validationService.validatePermission(userId, path, method);
                    if (!permissionResult.isValid()) {
                        log.warn("Permission validation failed for user: {}, path: {}, reason: {}", 
                                userId, path, permissionResult.getErrorMessage());
                        return sendUnauthorizedResponse(response, permissionResult.getErrorMessage());
                    }
                }
            }
        }

        log.debug("Sync validation passed for path: {}", path);
        return chain.filter(exchange);
    }

    /**
     * 判断是否需要跳过验证
     */
    private boolean isSkipValidation(String path, String method) {
        // 使用数据库配置的排除路径
        boolean isExcluded = excludedPathService.isExcludedPath(path, method);
        if (isExcluded) {
            log.debug("Path excluded by database config: {} {}", method, path);
            return true;
        }

        try {
            // 检查异步验证排除路径
            if (dynamicConfigService.isAsyncValidationEnabled()) {
                boolean isAsyncExcluded = dynamicConfigService.isAsyncValidationExcludedPath(path, method);
                if (isAsyncExcluded) {
                    log.debug("Path excluded from async validation by dynamic config: {} {}", method, path);
                    return true;
                }
            }
            
            // 检查API Key认证排除路径
            if (dynamicConfigService.isApiKeyAuthValidationEnabled()) {
                boolean isApiKeyExcluded = dynamicConfigService.isApiKeyAuthExcludedPath(path, method);
                if (isApiKeyExcluded) {
                    log.debug("Path excluded from API Key auth by dynamic config: {} {}", method, path);
                    return true;
                }
            }
            
            // 检查JWT验证排除路径
            if (dynamicConfigService.isJwtValidationEnabled()) {
                boolean isJwtExcluded = dynamicConfigService.isJwtExcludedPath(path, method);
                if (isJwtExcluded) {
                    log.debug("Path excluded from JWT validation by dynamic config: {} {}", method, path);
                    return true;
                }
            }
            
            // 检查权限验证排除路径
            if (dynamicConfigService.isPermissionValidationEnabled()) {
                boolean isPermissionExcluded = dynamicConfigService.isPermissionExcludedPath(path, method);
                if (isPermissionExcluded) {
                    log.debug("Path excluded from permission validation by dynamic config: {} {}", method, path);
                    return true;
                }
            }
            
            // 通用动态配置排除路径
            boolean isDynamicExcluded = dynamicConfigService.isExcludedPath(path, method);
            if (isDynamicExcluded) {
                log.debug("Path excluded by dynamic config: {} {}", method, path);
                return true;
            }
        } catch (Exception e) {
            log.warn("Error checking dynamic excluded paths: {}", e.getMessage());
        }
        
        return false;
    }

    /**
     * 构建验证请求
     */
    private ValidationRequest buildValidationRequest(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        String apiKey = extractApiKey(request);
        String signature = extractSignature(request);
        String jwtToken = extractJwtToken(request);
        String userId = extractUserId(request);
        
        return ValidationRequest.builder()
            .path(path)
            .method(method)
            .apiKey(apiKey)
            .signature(signature)
            .jwtToken(jwtToken)
            .userId(userId)
            .timestamp(System.currentTimeMillis())
            .clientIp(request.getRemoteAddress() != null ? 
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown")
            .userAgent(request.getHeaders().getFirst("User-Agent"))
            .build();
    }

    /**
     * 提取API Key
     */
    private String extractApiKey(ServerHttpRequest request) {
        String apiKey = request.getHeaders().getFirst("X-API-Key");
        if (StringUtils.hasText(apiKey)) {
            return apiKey;
        }
        
        String authorization = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        
        return request.getQueryParams().getFirst("apiKey");
    }

    /**
     * 提取签名
     */
    private String extractSignature(ServerHttpRequest request) {
        String signature = request.getHeaders().getFirst("X-Signature");
        if (StringUtils.hasText(signature)) {
            return signature;
        }
        
        return request.getQueryParams().getFirst("signature");
    }

    /**
     * 提取JWT Token
     */
    private String extractJwtToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        
        return request.getHeaders().getFirst("X-JWT-Token");
    }

    /**
     * 提取用户ID
     */
    private String extractUserId(ServerHttpRequest request) {
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        
        return request.getQueryParams().getFirst("userId");
    }

    /**
     * 发送验证失败响应
     */
    private Mono<Void> sendValidationFailedResponse(ServerHttpResponse response, 
                                                   AsyncValidationService.AsyncValidationResult result) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", 401);
        responseBody.put("message", result.getCombinedErrorMessage());
        responseBody.put("timestamp", System.currentTimeMillis());
        responseBody.put("validationTime", result.getValidationTime());
        responseBody.put("details", Map.of(
            "signatureValid", result.isSignatureValid(),
            "authValid", result.isAuthValid(),
            "apiKeyValid", result.isApiKeyValid()
        ));
        
        String jsonResponse = com.alibaba.fastjson.JSON.toJSONString(responseBody);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    /**
     * 发送未授权响应
     */
    private Mono<Void> sendUnauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("code", 401);
        responseBody.put("message", message);
        responseBody.put("timestamp", System.currentTimeMillis());
        
        String jsonResponse = com.alibaba.fastjson.JSON.toJSONString(responseBody);
        byte[] bytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
