package com.jiangyang.dubbo.exception;

import com.jiangyang.dubbo.api.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Dubbo统一异常处理器
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class DubboExceptionHandler {

    /**
     * 处理Dubbo RPC异常
     */
    @ExceptionHandler(RpcException.class)
    public Result<?> handleRpcException(RpcException e) {
        log.error("Dubbo RPC异常: code={}, message={}", e.getCode(), e.getMessage(), e);
        
        String message;
        String code;
        
        switch (e.getCode()) {
            case RpcException.UNKNOWN_EXCEPTION:
                message = "服务调用未知异常";
                code = "DUBBO_UNKNOWN_ERROR";
                break;
            case RpcException.NETWORK_EXCEPTION:
                message = "网络异常，请检查网络连接";
                code = "DUBBO_NETWORK_ERROR";
                break;
            case RpcException.TIMEOUT_EXCEPTION:
                message = "服务调用超时";
                code = "DUBBO_TIMEOUT_ERROR";
                break;
            case RpcException.BIZ_EXCEPTION:
                message = "业务异常: " + e.getMessage();
                code = "DUBBO_BIZ_ERROR";
                break;
            case RpcException.FORBIDDEN_EXCEPTION:
                message = "服务被禁止访问";
                code = "DUBBO_FORBIDDEN_ERROR";
                break;
            case RpcException.SERIALIZATION_EXCEPTION:
                message = "序列化异常";
                code = "DUBBO_SERIALIZATION_ERROR";
                break;
            case RpcException.NO_INVOKER_AVAILABLE_AFTER_FILTER:
                message = "没有可用的服务提供者";
                code = "DUBBO_NO_PROVIDER_ERROR";
                break;
            default:
                message = "服务调用异常: " + e.getMessage();
                code = "DUBBO_GENERAL_ERROR";
        }
        
        return Result.failure(message, code);
    }

    /**
     * 处理Dubbo服务异常
     */
    @ExceptionHandler(DubboServiceException.class)
    public Result<?> handleDubboServiceException(DubboServiceException e) {
        log.error("Dubbo服务异常: code={}, message={}", e.getErrorCode(), e.getMessage(), e);
        
        return Result.failure(e.getMessage(), e.getErrorCode());
    }

    /**
     * 处理服务降级异常
     */
    @ExceptionHandler(ServiceDegradationException.class)
    public Result<?> handleServiceDegradationException(ServiceDegradationException e) {
        log.warn("服务降级: service={}, method={}, reason={}", 
                e.getServiceName(), e.getMethodName(), e.getMessage());
        
        return Result.failure("服务暂时不可用，已启用降级处理", "SERVICE_DEGRADED");
    }

    /**
     * 处理服务熔断异常
     */
    @ExceptionHandler(ServiceCircuitBreakerException.class)
    public Result<?> handleServiceCircuitBreakerException(ServiceCircuitBreakerException e) {
        log.warn("服务熔断: service={}, reason={}", e.getServiceName(), e.getMessage());
        
        return Result.failure("服务熔断中，请稍后重试", "SERVICE_CIRCUIT_BREAKER");
    }

    /**
     * 处理一般异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleGeneralException(Exception e) {
        log.error("系统异常", e);
        
        return Result.failure("系统异常: " + e.getMessage(), "SYSTEM_ERROR");
    }
}