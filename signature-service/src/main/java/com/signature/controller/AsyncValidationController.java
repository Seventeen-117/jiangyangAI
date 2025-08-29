package com.signature.controller;

import com.signature.model.ValidationRequest;
import com.signature.service.AsyncValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 异步验证控制器
 * 提供异步验证相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/async-validation")
public class AsyncValidationController {

    @Autowired
    private AsyncValidationService asyncValidationService;

    /**
     * 执行异步验证
     */
    @PostMapping("/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateAsync(@RequestBody ValidationRequest request) {
        log.info("收到异步验证请求: {}", request.getPath());
        
        return asyncValidationService.validateAsyncWithConfig(request)
            .map(result -> {
                Map<String, Object> response = Map.of(
                    "success", result.isAllValid(),
                    "message", result.isAllValid() ? "验证成功" : result.getCombinedErrorMessage(),
                    "validationTime", result.getValidationTime(),
                    "details", Map.of(
                        "signatureValid", result.isSignatureValid(),
                        "authValid", result.isAuthValid(),
                        "apiKeyValid", result.isApiKeyValid(),
                        "signatureError", result.getSignatureError(),
                        "authError", result.getAuthError(),
                        "apiKeyError", result.getApiKeyError()
                    )
                );
                
                return ResponseEntity.ok(response);
            })
            .onErrorResume(e -> {
                log.error("异步验证异常", e);
                Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "验证服务异常: " + e.getMessage(),
                    "validationTime", 0L
                );
                return Mono.just(ResponseEntity.status(500).body(errorResponse));
            });
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        Map<String, Object> health = Map.of(
            "status", "UP",
            "service", "AsyncValidationService",
            "timestamp", System.currentTimeMillis()
        );
        return Mono.just(ResponseEntity.ok(health));
    }
}
