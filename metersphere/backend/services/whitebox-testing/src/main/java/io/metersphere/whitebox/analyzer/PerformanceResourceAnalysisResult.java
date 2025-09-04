package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.ConcurrencyPerformanceIssue;
import io.metersphere.whitebox.vo.InefficientAlgorithmIssue;
import io.metersphere.whitebox.vo.MemoryUsageIssue;
import io.metersphere.whitebox.vo.ResourceLeakIssue;
import lombok.Data;
import java.util.*;

/**
 * 性能资源分析结果
 */
@Data
public class PerformanceResourceAnalysisResult {
    private String serviceName;
    private Date analysisTime;
    private List<ResourceLeakIssue> resourceLeakIssues = new ArrayList<>();
    private List<InefficientAlgorithmIssue> inefficientAlgorithmIssues = new ArrayList<>();
    private List<MemoryUsageIssue> memoryUsageIssues = new ArrayList<>();
    private List<ConcurrencyPerformanceIssue> concurrencyPerformanceIssues = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    
    // 统计信息
    private int totalIssues;
    private int criticalIssues;
    private int highIssues;
    private int mediumIssues;
    private int lowIssues;
    
    public void addError(String error) {
        this.errors.add(error);
    }
    
    public void calculateStatistics() {
        totalIssues = resourceLeakIssues.size() + inefficientAlgorithmIssues.size() + 
                     memoryUsageIssues.size() + concurrencyPerformanceIssues.size();
        
        criticalIssues = (int) resourceLeakIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) inefficientAlgorithmIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) memoryUsageIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) concurrencyPerformanceIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count();
        
        highIssues = (int) resourceLeakIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) inefficientAlgorithmIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) memoryUsageIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) concurrencyPerformanceIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();
        
        mediumIssues = (int) resourceLeakIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) inefficientAlgorithmIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) memoryUsageIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) concurrencyPerformanceIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count();
        
        lowIssues = (int) resourceLeakIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) inefficientAlgorithmIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) memoryUsageIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) concurrencyPerformanceIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count();
    }
}

