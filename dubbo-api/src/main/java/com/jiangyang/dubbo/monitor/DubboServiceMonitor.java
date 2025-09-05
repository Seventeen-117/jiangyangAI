package com.jiangyang.dubbo.monitor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dubbo服务监控统计组件
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Slf4j
@Component
public class DubboServiceMonitor {
    
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, ServiceMetrics> serviceMetrics = new ConcurrentHashMap<>();
    private volatile boolean monitoring = false;
    
    /**
     * 启动监控
     */
    public void startMonitoring() {
        if (monitoring) {
            return;
        }
        
        monitoring = true;
        log.info("启动Dubbo服务监控");
        
        // 定期输出统计信息
        scheduler.scheduleAtFixedRate(this::printStatistics, 60, 60, TimeUnit.SECONDS);
        
        // 定期清理过期数据
        scheduler.scheduleAtFixedRate(this::cleanupExpiredData, 300, 300, TimeUnit.SECONDS);
    }
    
    /**
     * 停止监控
     */
    @PreDestroy
    public void stopMonitoring() {
        monitoring = false;
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Dubbo服务监控已停止");
    }
    
    /**
     * 记录服务调用
     */
    public void recordServiceCall(String serviceName, String methodName, long responseTime, boolean success) {
        String key = serviceName + "#" + methodName;
        ServiceMetrics metrics = serviceMetrics.computeIfAbsent(key, k -> new ServiceMetrics(serviceName, methodName));
        
        metrics.recordCall(responseTime, success);
    }
    
    /**
     * 获取服务指标
     */
    public ServiceMetrics getServiceMetrics(String serviceName, String methodName) {
        String key = serviceName + "#" + methodName;
        return serviceMetrics.get(key);
    }
    
    /**
     * 获取所有服务指标
     */
    public ConcurrentHashMap<String, ServiceMetrics> getAllMetrics() {
        return new ConcurrentHashMap<>(serviceMetrics);
    }
    
    /**
     * 打印统计信息
     */
    private void printStatistics() {
        if (serviceMetrics.isEmpty()) {
            return;
        }
        
        log.info("=== Dubbo服务监控统计 ===");
        serviceMetrics.forEach((key, metrics) -> {
            log.info("服务: {}, 总调用: {}, 成功: {}, 失败: {}, 成功率: {:.2f}%, 平均响应时间: {}ms, 最大响应时间: {}ms",
                    key,
                    metrics.getTotalCalls(),
                    metrics.getSuccessCalls(),
                    metrics.getErrorCalls(),
                    metrics.getSuccessRate(),
                    metrics.getAverageResponseTime(),
                    metrics.getMaxResponseTime()
            );
        });
    }
    
    /**
     * 清理过期数据
     */
    private void cleanupExpiredData() {
        long currentTime = System.currentTimeMillis();
        serviceMetrics.entrySet().removeIf(entry -> {
            ServiceMetrics metrics = entry.getValue();
            // 清理5分钟内无调用的服务统计
            return (currentTime - metrics.getLastCallTime()) > 300_000;
        });
    }
    
    /**
     * 服务指标数据
     */
    @Data
    public static class ServiceMetrics {
        private final String serviceName;
        private final String methodName;
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successCalls = new AtomicLong(0);
        private final AtomicLong errorCalls = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private volatile long maxResponseTime = 0;
        private volatile long minResponseTime = Long.MAX_VALUE;
        private volatile long lastCallTime = System.currentTimeMillis();
        private final Object lockObject = new Object();
        
        public ServiceMetrics(String serviceName, String methodName) {
            this.serviceName = serviceName;
            this.methodName = methodName;
        }
        
        /**
         * 记录调用
         */
        public void recordCall(long responseTime, boolean success) {
            totalCalls.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
            lastCallTime = System.currentTimeMillis();
            
            if (success) {
                successCalls.incrementAndGet();
            } else {
                errorCalls.incrementAndGet();
            }
            
            // 更新最大和最小响应时间
            synchronized (lockObject) {
                if (responseTime > maxResponseTime) {
                    maxResponseTime = responseTime;
                }
                if (responseTime < minResponseTime) {
                    minResponseTime = responseTime;
                }
            }
        }
        
        /**
         * 获取成功率
         */
        public double getSuccessRate() {
            long total = totalCalls.get();
            if (total == 0) {
                return 0.0;
            }
            return (double) successCalls.get() / total * 100;
        }
        
        /**
         * 获取平均响应时间
         */
        public long getAverageResponseTime() {
            long total = totalCalls.get();
            if (total == 0) {
                return 0;
            }
            return totalResponseTime.get() / total;
        }
        
        /**
         * 重置统计
         */
        public void reset() {
            totalCalls.set(0);
            successCalls.set(0);
            errorCalls.set(0);
            totalResponseTime.set(0);
            maxResponseTime = 0;
            minResponseTime = Long.MAX_VALUE;
            lastCallTime = System.currentTimeMillis();
        }
        
        /**
         * 获取总调用次数
         */
        public long getTotalCalls() {
            return totalCalls.get();
        }
        
        /**
         * 获取成功调用次数
         */
        public long getSuccessCalls() {
            return successCalls.get();
        }
        
        /**
         * 获取失败调用次数
         */
        public long getErrorCalls() {
            return errorCalls.get();
        }
        
        /**
         * 获取最大响应时间
         */
        public long getMaxResponseTime() {
            return maxResponseTime;
        }
        
        /**
         * 获取最后调用时间
         */
        public long getLastCallTime() {
            return lastCallTime;
        }
    }
}