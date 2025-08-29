package com.signature.dubbo;

import com.jiangyang.dubbo.api.auth.AuthService;
import com.jiangyang.dubbo.api.auth.dto.*;
import com.jiangyang.dubbo.api.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 认证服务 Dubbo 实现
 * 
 * @author jiangyang
 */
@Slf4j
@DubboService(
    version = "1.0.0",
    group = "auth",
    timeout = 5000,
    retries = 2,
    loadbalance = "roundrobin",
    cluster = "failover"
)
@Component
@RequiredArgsConstructor
public class AuthDubboServiceImpl implements AuthService {
    
    // 简单的内存存储（生产环境应使用数据库或Redis）
    private final Map<String, UserInfo> apiKeyStore = new ConcurrentHashMap<>();
    private final Map<String, UserInfo> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, String> refreshTokenStore = new ConcurrentHashMap<>();
    
    // 统计计数器
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);
    private final AtomicLong failureRequests = new AtomicLong(0);
    
    // 初始化一些测试数据
    {
        UserInfo testUser = new UserInfo();
        testUser.setUserId("test-user-001");
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setStatus(1); // 1表示启用状态
        testUser.setCreateTime(LocalDateTime.now());
        
        apiKeyStore.put("test-api-key-12345", testUser);
        tokenStore.put("test-token-67890", testUser);
        refreshTokenStore.put("test-refresh-token-abc", "test-token-67890");
    }

    @Override
    public Result<AuthResponse> validateApiKey(String apiKey) {
        totalRequests.incrementAndGet();
        
        try {
            log.info("Dubbo调用 - 验证API密钥: apiKey={}", maskApiKey(apiKey));
            
            if (!StringUtils.hasText(apiKey)) {
                failureRequests.incrementAndGet();
                return Result.failure("API密钥不能为空", "400");
            }
            
            UserInfo userInfo = apiKeyStore.get(apiKey);
            if (userInfo == null) {
                failureRequests.incrementAndGet();
                log.warn("无效的API密钥: {}", maskApiKey(apiKey));
                return Result.failure("无效的API密钥", "401");
            }
            
            if (userInfo.getStatus() == null || userInfo.getStatus() != 1) {
                failureRequests.incrementAndGet();
                log.warn("用户状态非活跃: userId={}, status={}", userInfo.getUserId(), userInfo.getStatus());
                return Result.failure("用户状态非活跃", "403");
            }
            
            // 创建认证响应
            AuthResponse response = new AuthResponse();
            response.setAccessToken(generateAccessToken(userInfo.getUserId()));
            response.setRefreshToken(generateRefreshToken());
            response.setExpiresIn(3600L); // 1小时
            response.setLoginTime(System.currentTimeMillis());
            
            // 创建用户信息并设置到响应中
            UserInfo responseUserInfo = new UserInfo();
            responseUserInfo.setUserId(userInfo.getUserId());
            responseUserInfo.setUsername(userInfo.getUsername());
            responseUserInfo.setEmail(userInfo.getEmail());
            responseUserInfo.setLastLoginTime(userInfo.getLastLoginTime());
            response.setUserInfo(responseUserInfo);
            
            successRequests.incrementAndGet();
            log.info("API密钥验证成功: userId={}", userInfo.getUserId());
            
            return Result.success("API密钥验证成功", response);
            
        } catch (Exception e) {
            failureRequests.incrementAndGet();
            log.error("验证API密钥异常: apiKey={}, error={}", maskApiKey(apiKey), e.getMessage(), e);
            return Result.failure("API密钥验证失败: " + e.getMessage(), "500");
        }
    }

    @Override
    public Result<UserInfo> getUserInfo(String userId) {
        totalRequests.incrementAndGet();
        
        try {
            log.info("Dubbo调用 - 获取用户信息: userId={}", userId);
            
            if (!StringUtils.hasText(userId)) {
                failureRequests.incrementAndGet();
                return Result.failure("用户ID不能为空", "400");
            }
            
            // 从各个存储中查找用户
            UserInfo userInfo = findUserById(userId);
            if (userInfo == null) {
                failureRequests.incrementAndGet();
                log.warn("用户不存在: userId={}", userId);
                return Result.failure("用户不存在", "404");
            }
            
            successRequests.incrementAndGet();
            log.info("获取用户信息成功: userId={}, username={}", userInfo.getUserId(), userInfo.getUsername());
            
            return Result.success("获取用户信息成功", userInfo);
            
        } catch (Exception e) {
            failureRequests.incrementAndGet();
            log.error("获取用户信息异常: userId={}, error={}", userId, e.getMessage(), e);
            return Result.failure("获取用户信息失败: " + e.getMessage(), "500");
        }
    }

    @Override
    public Result<AuthResponse> validateToken(String token) {
        totalRequests.incrementAndGet();
        
        try {
            log.info("Dubbo调用 - 验证Token: token={}", maskToken(token));
            
            if (!StringUtils.hasText(token)) {
                failureRequests.incrementAndGet();
                return Result.failure("Token不能为空", "400");
            }
            
            UserInfo userInfo = tokenStore.get(token);
            if (userInfo == null) {
                failureRequests.incrementAndGet();
                log.warn("无效的Token: {}", maskToken(token));
                return Result.failure("无效的Token", "401");
            }
            
            // 创建认证响应
            AuthResponse response = new AuthResponse();
            response.setAccessToken(token);
            response.setLoginTime(System.currentTimeMillis());
            
            // 创建用户信息并设置到响应中
            UserInfo responseUserInfo = new UserInfo();
            responseUserInfo.setUserId(userInfo.getUserId());
            responseUserInfo.setUsername(userInfo.getUsername());
            response.setUserInfo(responseUserInfo);
            
            successRequests.incrementAndGet();
            log.info("Token验证成功: userId={}", userInfo.getUserId());
            
            return Result.success("Token验证成功", response);
            
        } catch (Exception e) {
            failureRequests.incrementAndGet();
            log.error("验证Token异常: token={}, error={}", maskToken(token), e.getMessage(), e);
            return Result.failure("Token验证失败: " + e.getMessage(), "500");
        }
    }

    @Override
    public Result<AuthResponse> refreshToken(String refreshToken) {
        totalRequests.incrementAndGet();
        
        try {
            log.info("Dubbo调用 - 刷新Token: refreshToken={}", maskToken(refreshToken));
            
            if (!StringUtils.hasText(refreshToken)) {
                failureRequests.incrementAndGet();
                return Result.failure("刷新Token不能为空", "400");
            }
            
            String oldToken = refreshTokenStore.get(refreshToken);
            if (oldToken == null) {
                failureRequests.incrementAndGet();
                log.warn("无效的刷新Token: {}", maskToken(refreshToken));
                return Result.failure("无效的刷新Token", "401");
            }
            
            UserInfo userInfo = tokenStore.get(oldToken);
            if (userInfo == null) {
                failureRequests.incrementAndGet();
                return Result.failure("关联的用户Token无效", "401");
            }
            
            // 生成新的Token
            String newAccessToken = generateAccessToken(userInfo.getUserId());
            String newRefreshToken = generateRefreshToken();
            
            // 更新存储
            tokenStore.remove(oldToken);
            tokenStore.put(newAccessToken, userInfo);
            refreshTokenStore.remove(refreshToken);
            refreshTokenStore.put(newRefreshToken, newAccessToken);
            
            // 创建响应
            AuthResponse response = new AuthResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(3600L);
            response.setLoginTime(System.currentTimeMillis());
            
            // 创建用户信息并设置到响应中
            UserInfo responseUserInfo = new UserInfo();
            responseUserInfo.setUserId(userInfo.getUserId());
            responseUserInfo.setUsername(userInfo.getUsername());
            response.setUserInfo(responseUserInfo);
            
            successRequests.incrementAndGet();
            log.info("Token刷新成功: userId={}", userInfo.getUserId());
            
            return Result.success("Token刷新成功", response);
            
        } catch (Exception e) {
            failureRequests.incrementAndGet();
            log.error("刷新Token异常: refreshToken={}, error={}", maskToken(refreshToken), e.getMessage(), e);
            return Result.failure("Token刷新失败: " + e.getMessage(), "500");
        }
    }

    @Override
    public Result<AuthResponse> login(AuthRequest request) {
        totalRequests.incrementAndGet();
        
        try {
            log.info("Dubbo调用 - 用户登录: username={}", request.getUsername());
            
            if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
                failureRequests.incrementAndGet();
                return Result.failure("用户名和密码不能为空", "400");
            }
            
            // 简单的用户名密码验证（生产环境应该验证哈希密码）
            if (!"testUser".equals(request.getUsername()) || !"password123".equals(request.getPassword())) {
                failureRequests.incrementAndGet();
                log.warn("用户名或密码错误: username={}", request.getUsername());
                return Result.failure("用户名或密码错误", "401");
            }
            
            // 创建用户信息
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(UUID.randomUUID().toString());
            userInfo.setUsername(request.getUsername());
            userInfo.setEmail("test@example.com");
            userInfo.setStatus(1); // 1表示启用状态
            userInfo.setCreateTime(LocalDateTime.now());
            userInfo.setLastLoginTime(LocalDateTime.now());
            
            // 生成Token
            String accessToken = generateAccessToken(userInfo.getUserId());
            String refreshToken = generateRefreshToken();
            
            // 存储Token
            tokenStore.put(accessToken, userInfo);
            refreshTokenStore.put(refreshToken, accessToken);
            
            // 创建响应
            AuthResponse response = new AuthResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(3600L);
            response.setLoginTime(System.currentTimeMillis());
            
            // 创建用户信息并设置到响应中
            UserInfo responseUserInfo = new UserInfo();
            responseUserInfo.setUserId(userInfo.getUserId());
            responseUserInfo.setUsername(userInfo.getUsername());
            response.setUserInfo(responseUserInfo);
            
            successRequests.incrementAndGet();
            log.info("用户登录成功: userId={}, username={}", userInfo.getUserId(), userInfo.getUsername());
            
            return Result.success("登录成功", response);
            
        } catch (Exception e) {
            failureRequests.incrementAndGet();
            log.error("用户登录异常: username={}, error={}", 
                    request != null ? request.getUsername() : "null", e.getMessage(), e);
            return Result.failure("登录失败: " + e.getMessage(), "500");
        }
    }

    @Override
    public Result<Boolean> logout(String token) {
        totalRequests.incrementAndGet();
        
        try {
            log.info("Dubbo调用 - 用户登出: token={}", maskToken(token));
            
            if (!StringUtils.hasText(token)) {
                failureRequests.incrementAndGet();
                return Result.failure("Token不能为空", "400");
            }
            
            UserInfo userInfo = tokenStore.get(token);
            if (userInfo == null) {
                failureRequests.incrementAndGet();
                log.warn("Token不存在或已失效: {}", maskToken(token));
                return Result.success("登出成功", true); // 即使Token不存在也返回成功
            }
            
            // 移除Token和相关的刷新Token
            tokenStore.remove(token);
            refreshTokenStore.entrySet().removeIf(entry -> token.equals(entry.getValue()));
            
            successRequests.incrementAndGet();
            log.info("用户登出成功: userId={}", userInfo.getUserId());
            
            return Result.success("登出成功", true);
            
        } catch (Exception e) {
            failureRequests.incrementAndGet();
            log.error("用户登出异常: token={}, error={}", maskToken(token), e.getMessage(), e);
            return Result.failure("登出失败: " + e.getMessage(), "500");
        }
    }
    
    /**
     * 根据用户ID查找用户
     */
    private UserInfo findUserById(String userId) {
        return apiKeyStore.values().stream()
                .filter(user -> userId.equals(user.getUserId()))
                .findFirst()
                .orElse(tokenStore.values().stream()
                        .filter(user -> userId.equals(user.getUserId()))
                        .findFirst()
                        .orElse(null));
    }
    
    /**
     * 生成访问Token
     */
    private String generateAccessToken(String userId) {
        return "access_token_" + userId + "_" + System.currentTimeMillis();
    }
    
    /**
     * 生成刷新Token
     */
    private String generateRefreshToken() {
        return "refresh_token_" + UUID.randomUUID().toString();
    }
    
    /**
     * 掩码API密钥
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
    
    /**
     * 掩码Token
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 8) {
            return "****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}