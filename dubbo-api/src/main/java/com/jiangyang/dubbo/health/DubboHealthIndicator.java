package com.jiangyang.dubbo.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Dubbo服务健康检查指示器
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Slf4j
@Component("dubboHealthIndicator")
public class DubboHealthIndicator implements HealthIndicator {
    
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successRequests = new AtomicLong(0);
    private final AtomicLong errorRequests = new AtomicLong(0);
    
    private volatile long lastCheckTime = System.currentTimeMillis();
    private volatile String lastError = null;

    @Override
    public Health health() {
        try {
            // 检查Dubbo服务状态
            Health.Builder builder = new Health.Builder();
            
            // 计算成功率
            long total = totalRequests.get();
            long success = successRequests.get();
            long errors = errorRequests.get();
            
            double successRate = total > 0 ? (double) success / total * 100 : 100.0;
            
            // 判断健康状态
            if (successRate >= 95.0 && lastError == null) {
                builder.up();
            } else if (successRate >= 80.0) {
                builder.status("DEGRADED");
            } else {
                builder.down();
            }
            
            // 添加详细信息
            builder.withDetail("totalRequests", total)
                   .withDetail("successRequests", success)
                   .withDetail("errorRequests", errors)
                   .withDetail("successRate", String.format("%.2f%%", successRate))
                   .withDetail("lastCheckTime", lastCheckTime)
                   .withDetail("upTime", System.currentTimeMillis() - lastCheckTime);
            
            if (lastError != null) {
                builder.withDetail("lastError", lastError);
            }
            
            // 更新检查时间
            lastCheckTime = System.currentTimeMillis();
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("Dubbo健康检查异常", e);
            lastError = e.getMessage();
            
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("checkTime", System.currentTimeMillis())
                    .build();
        }
    }
    
    /**
     * 记录请求
     */
    public void recordRequest(boolean success) {
        totalRequests.incrementAndGet();
        if (success) {
            successRequests.incrementAndGet();
            lastError = null; // 清除错误状态
        } else {
            errorRequests.incrementAndGet();
        }
    }
    
    /**
     * 记录错误
     */
    public void recordError(String error) {
        errorRequests.incrementAndGet();
        lastError = error;
    }
    
    /**
     * 重置统计
     */
    public void reset() {
        totalRequests.set(0);
        successRequests.set(0);
        errorRequests.set(0);
        lastError = null;
        lastCheckTime = System.currentTimeMillis();
    }
}