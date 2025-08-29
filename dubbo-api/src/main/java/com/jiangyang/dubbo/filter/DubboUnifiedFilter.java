package com.jiangyang.dubbo.filter;

import com.jiangyang.dubbo.health.DubboHealthIndicator;
import com.jiangyang.dubbo.monitor.DubboServiceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Dubbo统一过滤器
 * 用于异常处理、监控统计、日志记录等
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Slf4j
@Component
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class DubboUnifiedFilter implements Filter {
    
    @Autowired(required = false)
    private DubboHealthIndicator healthIndicator;
    
    @Autowired(required = false)
    private DubboServiceMonitor serviceMonitor;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String serviceName = invoker.getInterface().getSimpleName();
        String methodName = invocation.getMethodName();
        long startTime = System.currentTimeMillis();
        
        boolean success = false;
        Result result = null;
        
        try {
            log.debug("Dubbo调用开始: {}.{}", serviceName, methodName);
            
            // 调用服务
            result = invoker.invoke(invocation);
            
            // 检查结果
            if (result != null && !result.hasException()) {
                success = true;
            } else if (result != null && result.hasException()) {
                log.error("Dubbo调用业务异常: {}.{}, exception: {}", 
                        serviceName, methodName, result.getException().getMessage());
            }
            
            return result;
            
        } catch (RpcException e) {
            log.error("Dubbo调用RPC异常: {}.{}, code: {}, message: {}", 
                    serviceName, methodName, e.getCode(), e.getMessage());
            throw e;
            
        } catch (Exception e) {
            log.error("Dubbo调用未知异常: {}.{}, message: {}", 
                    serviceName, methodName, e.getMessage(), e);
            throw new RpcException(RpcException.UNKNOWN_EXCEPTION, e.getMessage(), e);
            
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            
            log.debug("Dubbo调用结束: {}.{}, 耗时: {}ms, 成功: {}", 
                    serviceName, methodName, responseTime, success);
            
            // 记录健康状态
            if (healthIndicator != null) {
                healthIndicator.recordRequest(success);
                if (!success && result != null && result.hasException()) {
                    healthIndicator.recordError(result.getException().getMessage());
                }
            }
            
            // 记录监控统计
            if (serviceMonitor != null) {
                serviceMonitor.recordServiceCall(serviceName, methodName, responseTime, success);
            }
        }
    }
}