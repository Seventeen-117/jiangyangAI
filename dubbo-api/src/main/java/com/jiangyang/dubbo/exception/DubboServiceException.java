package com.jiangyang.dubbo.exception;

/**
 * Dubbo服务异常
 * 
 * @author jiangyang
 * @since 1.0.0
 */
public class DubboServiceException extends RuntimeException {
    
    private final String errorCode;
    
    public DubboServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public DubboServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

/**
 * 服务降级异常
 */
class ServiceDegradationException extends RuntimeException {
    
    private final String serviceName;
    private final String methodName;
    
    public ServiceDegradationException(String serviceName, String methodName, String message) {
        super(message);
        this.serviceName = serviceName;
        this.methodName = methodName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public String getMethodName() {
        return methodName;
    }
}

/**
 * 服务熔断异常
 */
class ServiceCircuitBreakerException extends RuntimeException {
    
    private final String serviceName;
    
    public ServiceCircuitBreakerException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}