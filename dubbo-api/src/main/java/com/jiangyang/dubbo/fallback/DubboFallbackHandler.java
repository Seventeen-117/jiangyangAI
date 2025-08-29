package com.jiangyang.dubbo.fallback;

import com.jiangyang.dubbo.api.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Dubbo服务降级处理器
 * 提供统一的降级策略
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Slf4j
@Component
public class DubboFallbackHandler {
    
    /**
     * 通用降级处理
     */
    public <T> Result<T> handleFallback(String serviceName, String methodName, Throwable throwable) {
        log.warn("服务降级处理: {}.{}, 原因: {}", serviceName, methodName, throwable.getMessage());
        
        return Result.failure(
            String.format("服务 %s.%s 暂时不可用，已启用降级处理", serviceName, methodName),
            "SERVICE_DEGRADED"
        );
    }
    
    /**
     * 认证服务降级
     */
    public Result<Object> authServiceFallback(String methodName, Throwable throwable) {
        log.warn("认证服务降级: {}, 原因: {}", methodName, throwable.getMessage());
        
        // 认证服务降级时返回默认的未认证状态
        return Result.failure("认证服务暂时不可用，请稍后重试", "AUTH_SERVICE_UNAVAILABLE");
    }
    
    /**
     * 签名服务降级
     */
    public Result<Object> signatureServiceFallback(String methodName, Throwable throwable) {
        log.warn("签名服务降级: {}, 原因: {}", methodName, throwable.getMessage());
        
        // 签名服务降级时跳过签名验证（根据业务需求调整）
        return Result.failure("签名服务暂时不可用，请稍后重试", "SIGNATURE_SERVICE_UNAVAILABLE");
    }
    
    /**
     * AI服务降级
     */
    public Result<Object> bgaiServiceFallback(String methodName, Throwable throwable) {
        log.warn("AI服务降级: {}, 原因: {}", methodName, throwable.getMessage());
        
        // AI服务降级时返回提示信息
        return Result.failure("AI服务暂时不可用，请稍后重试", "AI_SERVICE_UNAVAILABLE");
    }
    
    /**
     * 消息服务降级
     */
    public Result<Object> messageServiceFallback(String methodName, Throwable throwable) {
        log.warn("消息服务降级: {}, 原因: {}", methodName, throwable.getMessage());
        
        // 消息服务降级时可能需要本地存储待发送消息
        return Result.failure("消息服务暂时不可用，消息将稍后发送", "MESSAGE_SERVICE_UNAVAILABLE");
    }
    
    /**
     * 数据计算服务降级
     */
    public Result<Object> dataCalculationServiceFallback(String methodName, Throwable throwable) {
        log.warn("数据计算服务降级: {}, 原因: {}", methodName, throwable.getMessage());
        
        // 数据计算服务降级时返回缓存结果或简化计算
        return Result.failure("数据计算服务暂时不可用，请稍后重试", "CALCULATION_SERVICE_UNAVAILABLE");
    }
    
    /**
     * 事务事件服务降级
     */
    public Result<Object> transactionEventServiceFallback(String methodName, Throwable throwable) {
        log.warn("事务事件服务降级: {}, 原因: {}", methodName, throwable.getMessage());
        
        // 事务事件服务降级时可能需要本地记录事件
        return Result.failure("事务事件服务暂时不可用，事件将稍后处理", "TRANSACTION_SERVICE_UNAVAILABLE");
    }
}