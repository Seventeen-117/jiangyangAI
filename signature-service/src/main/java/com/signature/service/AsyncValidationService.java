package com.signature.service;

import com.signature.model.ValidationRequest;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 异步验证服务接口
 * 协调签名、认证和API Key的异步验证，统一收集结果
 */
public interface AsyncValidationService {

    /**
     * 异步验证结果
     */
    class AsyncValidationResult {
        private final boolean signatureValid;
        private final boolean authValid;
        private final boolean apiKeyValid;
        private final String signatureError;
        private final String authError;
        private final String apiKeyError;
        private final long validationTime;

        public AsyncValidationResult(boolean signatureValid, boolean authValid, boolean apiKeyValid,
                                   String signatureError, String authError, String apiKeyError, long validationTime) {
            this.signatureValid = signatureValid;
            this.authValid = authValid;
            this.apiKeyValid = apiKeyValid;
            this.signatureError = signatureError;
            this.authError = authError;
            this.apiKeyError = apiKeyError;
            this.validationTime = validationTime;
        }

        public boolean isAllValid() {
            return signatureValid && authValid && apiKeyValid;
        }

        public String getCombinedErrorMessage() {
            StringBuilder sb = new StringBuilder();
            if (!signatureValid && signatureError != null) {
                sb.append("Signature: ").append(signatureError).append("; ");
            }
            if (!authValid && authError != null) {
                sb.append("Auth: ").append(authError).append("; ");
            }
            if (!apiKeyValid && apiKeyError != null) {
                sb.append("API Key: ").append(apiKeyError).append("; ");
            }
            return sb.length() > 0 ? sb.toString() : "Validation failed";
        }

        // Getters
        public boolean isSignatureValid() { return signatureValid; }
        public boolean isAuthValid() { return authValid; }
        public boolean isApiKeyValid() { return apiKeyValid; }
        public String getSignatureError() { return signatureError; }
        public String getAuthError() { return authError; }
        public String getApiKeyError() { return apiKeyError; }
        public long getValidationTime() { return validationTime; }
    }

    /**
     * 执行异步验证
     * @param request 验证请求
     * @return 异步验证结果
     */
    Mono<AsyncValidationResult> validateAsync(ValidationRequest request);

    /**
     * 执行异步验证（带配置检查）
     * @param request 验证请求
     * @return 异步验证结果
     */
    Mono<AsyncValidationResult> validateAsyncWithConfig(ValidationRequest request);

    /**
     * 批量异步验证
     * @param requests 验证请求列表
     * @return 批量验证结果
     */
    Flux<AsyncValidationResult> validateBatchAsync(List<ValidationRequest> requests);
}
