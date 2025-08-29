package com.signature.service;

import java.util.Map;

/**
 * 验证指标服务接口
 * 用于监控验证成功率和响应时间
 */
public interface ValidationMetricsService {

    /**
     * 记录验证成功
     */
    void recordSuccess(String validationType, long responseTime);

    /**
     * 记录验证失败
     */
    void recordFailure(String validationType, long responseTime);

    /**
     * 获取验证成功率
     */
    double getSuccessRate(String validationType);

    /**
     * 获取平均响应时间
     */
    double getAverageResponseTime(String validationType);

    /**
     * 获取验证统计信息
     */
    Map<String, Object> getValidationStats(String validationType);

    /**
     * 获取所有验证类型的统计信息
     */
    Map<String, Map<String, Object>> getAllValidationStats();

    /**
     * 重置统计信息
     */
    void resetStats();
}
