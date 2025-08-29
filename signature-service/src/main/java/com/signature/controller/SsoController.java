package com.signature.controller;

import com.signature.model.SsoAuthRequest;
import com.signature.model.SsoAuthResponse;
import com.signature.model.SsoUserInfo;
import com.signature.service.SsoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * SSO 单点登录控制器
 * 处理认证、授权、用户信息等SSO相关功能
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class SsoController {

    @Autowired
    private SsoService ssoService;

    /**
     * 获取授权URL
     */
    @GetMapping("/authorize")
    public Mono<ResponseEntity<Map<String, Object>>> getAuthorizeUrl(
            @RequestParam(value = "redirect_uri", required = false) String redirectUri,
            @RequestParam(value = "state", required = false) String state,
            ServerWebExchange exchange) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            String authorizeUrl = ssoService.buildAuthorizeUrl(redirectUri, state, exchange);
            response.put("success", true);
            response.put("authorize_url", authorizeUrl);
            response.put("state", state);
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Failed to build authorize URL", e);
            response.put("success", false);
            response.put("error", "Failed to build authorize URL");
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
        }
    }

    /**
     * 处理授权回调
     */
    @GetMapping("/callback")
    public Mono<ResponseEntity<Map<String, Object>>> handleCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            ServerWebExchange exchange) {
        
        Map<String, Object> result = new HashMap<>();
        
        if (error != null) {
            log.warn("SSO callback error: {}", error);
            result.put("success", false);
            result.put("error", error);
            return Mono.just(ResponseEntity.badRequest().body(result));
        }

        try {
            SsoAuthResponse authResponse = ssoService.handleCallback(code, state, exchange);
            result.put("success", true);
            result.put("access_token", authResponse.getAccessToken());
            result.put("refresh_token", authResponse.getRefreshToken());
            result.put("expires_in", authResponse.getExpiresIn());
            result.put("token_type", authResponse.getTokenType());
            
            // 设置Cookie
            ssoService.setAuthCookies(exchange, authResponse);
            
            return Mono.just(ResponseEntity.ok(result));
        } catch (Exception e) {
            log.error("Failed to handle SSO callback", e);
            result.put("success", false);
            result.put("error", "Authentication failed");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result));
        }
    }

    /**
     * 获取访问令牌
     */
    @PostMapping("/token")
    public Mono<ResponseEntity<Map<String, Object>>> getToken(@RequestBody SsoAuthRequest authRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            SsoAuthResponse authResponse = ssoService.getToken(authRequest);
            response.put("success", true);
            response.put("access_token", authResponse.getAccessToken());
            response.put("refresh_token", authResponse.getRefreshToken());
            response.put("expires_in", authResponse.getExpiresIn());
            response.put("token_type", authResponse.getTokenType());
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Failed to get token", e);
            response.put("success", false);
            response.put("error", "Failed to get token");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
        }
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/token/refresh")
    public Mono<ResponseEntity<Map<String, Object>>> refreshToken(
            @RequestParam("refresh_token") String refreshToken) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            SsoAuthResponse authResponse = ssoService.refreshToken(refreshToken);
            response.put("success", true);
            response.put("access_token", authResponse.getAccessToken());
            response.put("refresh_token", authResponse.getRefreshToken());
            response.put("expires_in", authResponse.getExpiresIn());
            response.put("token_type", authResponse.getTokenType());
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            response.put("success", false);
            response.put("error", "Failed to refresh token");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user/info")
    public Mono<ResponseEntity<Map<String, Object>>> getUserInfo(
            @RequestHeader("Authorization") String authorization) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            SsoUserInfo userInfo = ssoService.getUserInfo(authorization);
            response.put("success", true);
            response.put("user", userInfo);
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Failed to get user info", e);
            response.put("success", false);
            response.put("error", "Failed to get user info");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response));
        }
    }

    /**
     * 验证令牌
     */
    @PostMapping("/token/verify")
    public Mono<ResponseEntity<Map<String, Object>>> verifyToken(
            @RequestParam("access_token") String accessToken) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = ssoService.verifyToken(accessToken);
            response.put("success", true);
            response.put("valid", isValid);
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Failed to verify token", e);
            response.put("success", false);
            response.put("error", "Token verification failed");
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
        }
    }

    /**
     * 注销登录
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, Object>>> logout(
            @RequestParam(value = "access_token", required = false) String accessToken,
            ServerWebExchange exchange) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            ssoService.logout(accessToken, exchange);
            result.put("success", true);
            result.put("message", "Logout successful");
            return Mono.just(ResponseEntity.ok(result));
        } catch (Exception e) {
            log.error("Failed to logout", e);
            result.put("success", false);
            result.put("error", "Logout failed");
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result));
        }
    }

    /**
     * 获取SSO配置信息
     */
    @GetMapping("/config")
    public Mono<ResponseEntity<Map<String, Object>>> getSsoConfig() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> config = ssoService.getSsoConfig();
            response.put("success", true);
            response.put("config", config);
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Failed to get SSO config", e);
            response.put("success", false);
            response.put("error", "Failed to get SSO config");
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
        }
    }

    /**
     * 检查会话状态
     */
    @GetMapping("/session/status")
    public Mono<ResponseEntity<Map<String, Object>>> getSessionStatus(ServerWebExchange exchange) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> sessionInfo = ssoService.getSessionStatus(exchange);
            response.put("success", true);
            response.put("session", sessionInfo);
            return Mono.just(ResponseEntity.ok(response));
        } catch (Exception e) {
            log.error("Failed to get session status", e);
            response.put("success", false);
            response.put("error", "Failed to get session status");
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
        }
    }
} 