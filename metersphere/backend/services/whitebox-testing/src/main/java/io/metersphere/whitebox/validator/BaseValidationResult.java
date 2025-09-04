package io.metersphere.whitebox.validator;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础验证结果类
 * 包含所有验证器共用的字段和方法
 */
@Data
public abstract class BaseValidationResult {
    private String serviceName;
    protected int totalChecks;
    protected int passedChecks;
    protected int failedChecks;
    protected boolean overallPassed;
    private List<String> validationErrors = new ArrayList<>();
    
    /**
     * 添加验证错误
     */
    public void addValidationError(String error) {
        this.validationErrors.add(error);
        this.overallPassed = false;
    }
    
    /**
     * 计算统计信息
     */
    public void calculateStatistics() {
        this.totalChecks = this.passedChecks + this.failedChecks;
        this.overallPassed = this.failedChecks == 0;
    }
    
    /**
     * 获取通过率
     */
    public double getPassRate() {
        if (totalChecks == 0) {
            return 0.0;
        }
        return (double) passedChecks / totalChecks * 100;
    }
    
    /**
     * 获取失败率
     */
    public double getFailRate() {
        if (totalChecks == 0) {
            return 0.0;
        }
        return (double) failedChecks / totalChecks * 100;
    }
    
    /**
     * 是否有错误
     */
    public boolean hasErrors() {
        return !validationErrors.isEmpty();
    }
    
    /**
     * 获取错误数量
     */
    public int getErrorCount() {
        return validationErrors.size();
    }
}
