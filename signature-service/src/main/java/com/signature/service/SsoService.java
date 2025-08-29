package com.signature.service;

import com.signature.model.SsoAuthRequest;
import com.signature.model.SsoAuthResponse;
import com.signature.model.SsoUserInfo;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

/**
 * SSO服务接口
 * 处理单点登录相关的业务逻辑
 */
public interface SsoService {

    /**
     * 构建授权URL
     */
    String buildAuthorizeUrl(String redirectUri, String state, ServerWebExchange exchange);

    /**
     * 处理授权回调
     */
    SsoAuthResponse handleCallback(String code, String state, ServerWebExchange exchange);

    /**
     * 获取访问令牌
     */
    SsoAuthResponse getToken(SsoAuthRequest authRequest);

    /**
     * 刷新访问令牌
     */
    SsoAuthResponse refreshToken(String refreshToken);

    /**
     * 获取用户信息
     */
    SsoUserInfo getUserInfo(String authorization);

    /**
     * 验证令牌
     */
    boolean verifyToken(String accessToken);

    /**
     * 注销登录
     */
    void logout(String accessToken, ServerWebExchange exchange);

    /**
     * 设置认证Cookie
     */
    void setAuthCookies(ServerWebExchange exchange, SsoAuthResponse authResponse);

    /**
     * 获取SSO配置信息
     */
    Map<String, Object> getSsoConfig();

    /**
     * 获取会话状态
     */
    Map<String, Object> getSessionStatus(ServerWebExchange exchange);
} 