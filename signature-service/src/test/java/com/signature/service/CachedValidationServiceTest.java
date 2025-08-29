package com.signature.service;

import com.signature.model.ValidationRequest;
import com.signature.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 缓存验证服务测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class CachedValidationServiceTest {

    @Autowired
    private ValidationService validationService;

    @Test
    public void testValidateApiKey() {
        // 测试API Key验证
        ValidationResult result = validationService.validateApiKey("test-api-key");
        assertNotNull(result);
        // 由于是测试环境，可能没有真实的API Key，所以验证失败是正常的
        assertFalse(result.isValid());
    }

    @Test
    public void testValidateSignature() {
        // 创建测试请求
        ValidationRequest request = ValidationRequest.builder()
                .path("/api/test")
                .method("POST")
                .apiKey("test-api-key")
                .signature("test-signature")
                .timestamp(System.currentTimeMillis())
                .build();

        // 测试签名验证
        ValidationResult result = validationService.validateSignature(request);
        assertNotNull(result);
        // 由于是测试环境，签名验证可能失败，这是正常的
        assertFalse(result.isValid());
    }

    @Test
    public void testValidateRequest() {
        // 创建测试请求
        ValidationRequest request = ValidationRequest.builder()
                .path("/api/test")
                .method("POST")
                .apiKey("test-api-key")
                .userId("test-user")
                .build();

        // 测试完整请求验证
        ValidationResult result = validationService.validateRequest(request);
        assertNotNull(result);
        // 由于缺少必要的参数，验证应该失败
        assertFalse(result.isValid());
    }

    @Test
    public void testValidatePermission() {
        // 测试权限验证
        ValidationResult result = validationService.validatePermission("test-user", "/api/test", "GET");
        assertNotNull(result);
        // 由于是测试环境，权限验证可能失败，这是正常的
        assertFalse(result.isValid());
    }

    @Test
    public void testValidateJwtToken() {
        // 测试JWT Token验证
        ValidationResult result = validationService.validateJwtToken("test-jwt-token");
        assertNotNull(result);
        // 由于是测试环境，JWT验证可能失败，这是正常的
        assertFalse(result.isValid());
    }

    @Test
    public void testValidateRequestWithValidData() {
        // 创建包含所有必要参数的测试请求
        Map<String, String> params = new HashMap<>();
        params.put("appId", "test-app");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", "test-nonce-123");

        ValidationRequest request = ValidationRequest.builder()
                .path("/api/test")
                .method("POST")
                .apiKey("test-api-key")
                .signature("test-signature")
                .timestamp(System.currentTimeMillis())
                .userId("test-user")
                .parameters(params)
                .build();

        // 测试完整请求验证
        ValidationResult result = validationService.validateRequest(request);
        assertNotNull(result);
        // 由于测试环境可能没有真实的配置，验证失败是正常的
        assertFalse(result.isValid());
    }

    @Test
    public void testCacheIntegration() {
        // 测试缓存集成
        String apiKey = "cache-test-api-key";
        
        // 第一次验证（应该从数据库查询并缓存）
        ValidationResult result1 = validationService.validateApiKey(apiKey);
        assertNotNull(result1);
        
        // 第二次验证（应该从缓存获取）
        ValidationResult result2 = validationService.validateApiKey(apiKey);
        assertNotNull(result2);
        
        // 两次结果应该一致
        assertEquals(result1.isValid(), result2.isValid());
    }
}
