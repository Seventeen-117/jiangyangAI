package com.signature.service.impl;

import com.signature.model.ValidationRequest;
import com.signature.model.ValidationResult;
import com.signature.service.AsyncValidationService;
import com.signature.service.DynamicConfigService;
import com.signature.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * 异步验证服务实现类
 * 协调签名、认证和API Key的异步验证，统一收集结果
 */
@Slf4j
@Service
public class AsyncValidationServiceImpl implements AsyncValidationService {

    @Autowired
    @Qualifier("cachedValidationServiceImpl")
    private ValidationService validationService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    @Override
    public Mono<AsyncValidationResult> validateAsync(ValidationRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.debug("开始异步验证: {}", request.getPath());

        return Mono.fromCallable(() -> {
            // 获取执行器
            Executor executor = (Executor) Schedulers.boundedElastic();
            
            // 并行执行三种验证
            CompletableFuture<ValidationResult> signatureFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return validationService.validateSignature(request);
                } catch (Exception e) {
                    log.error("签名验证异常", e);
                    return ValidationResult.failure("SIGNATURE_ERROR", "签名验证异常: " + e.getMessage());
                }
            }, executor);

            CompletableFuture<ValidationResult> authFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    // 使用JWT Token验证作为认证验证
                    return validationService.validateJwtToken(request.getJwtToken());
                } catch (Exception e) {
                    log.error("认证验证异常", e);
                    return ValidationResult.failure("AUTH_ERROR", "认证验证异常: " + e.getMessage());
                }
            }, executor);

            CompletableFuture<ValidationResult> apiKeyFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return validationService.validateApiKey(request.getApiKey());
                } catch (Exception e) {
                    log.error("API Key验证异常", e);
                    return ValidationResult.failure("API_KEY_ERROR", "API Key验证异常: " + e.getMessage());
                }
            }, executor);

            // 等待所有验证完成，设置超时时间
            try {
                CompletableFuture.allOf(signatureFuture, authFuture, apiKeyFuture)
                    .get(5, TimeUnit.SECONDS); // 5秒超时
            } catch (Exception e) {
                log.error("验证超时或异常", e);
                // 取消未完成的任务
                signatureFuture.cancel(true);
                authFuture.cancel(true);
                apiKeyFuture.cancel(true);
                
                return new AsyncValidationResult(
                    false, false, false,
                    "验证超时", "验证超时", "验证超时",
                    System.currentTimeMillis() - startTime
                );
            }

            // 收集验证结果
            ValidationResult signatureResult = signatureFuture.get();
            ValidationResult authResult = authFuture.get();
            ValidationResult apiKeyResult = apiKeyFuture.get();

            boolean signatureValid = signatureResult.isValid();
            boolean authValid = authResult.isValid();
            boolean apiKeyValid = apiKeyResult.isValid();

            log.debug("异步验证完成 - 签名: {}, 认证: {}, API Key: {}, 耗时: {}ms",
                signatureValid, authValid, apiKeyValid, System.currentTimeMillis() - startTime);

            return new AsyncValidationResult(
                signatureValid, authValid, apiKeyValid,
                signatureValid ? null : signatureResult.getErrorMessage(),
                authValid ? null : authResult.getErrorMessage(),
                apiKeyValid ? null : apiKeyResult.getErrorMessage(),
                System.currentTimeMillis() - startTime
            );

        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<AsyncValidationResult> validateAsyncWithConfig(ValidationRequest request) {
        return Mono.fromCallable(() -> {
            // 检查各种验证是否启用
            boolean signatureEnabled = dynamicConfigService.isSignatureValidationEnabled();
            boolean authEnabled = dynamicConfigService.isAuthenticationValidationEnabled();
            boolean apiKeyEnabled = dynamicConfigService.isApiKeyAuthValidationEnabled();

            log.debug("验证配置 - 签名: {}, 认证: {}, API Key: {}", 
                signatureEnabled, authEnabled, apiKeyEnabled);

            // 如果所有验证都禁用，直接返回成功
            if (!signatureEnabled && !authEnabled && !apiKeyEnabled) {
                log.debug("所有验证都已禁用，直接返回成功");
                return new AsyncValidationResult(true, true, true, null, null, null, 0);
            }

            return null; // 继续执行验证
        }).flatMap(result -> {
            if (result != null) {
                return Mono.just(result);
            }
            return validateAsync(request);
        });
    }

    @Override
    public Flux<AsyncValidationResult> validateBatchAsync(List<ValidationRequest> requests) {
        return Flux.fromIterable(requests)
            .flatMap(this::validateAsync, 10) // 并发度10
            .doOnNext(result -> 
                log.debug("批量验证结果: 全部通过={}, 耗时={}ms", 
                    result.isAllValid(), result.getValidationTime())
            );
    }
}
