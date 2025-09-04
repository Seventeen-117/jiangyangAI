package io.metersphere.whitebox.vo;

import io.metersphere.whitebox.analyzer.*;
import lombok.Data;
import java.util.*;

/**
 * 综合缺陷检测报告
 */
@Data
public class ComprehensiveDefectReport {
    private String reportId;
    private String serviceName;
    private Date generatedTime;
    
    // 六大类分析结果
    private ControlFlowAnalysisResult controlFlowAnalysis;
    private DataProcessingAnalysisResult dataProcessingAnalysis;
    private FunctionImplementationAnalysisResult functionImplementationAnalysis;
    private DependencyInteractionAnalysisResult dependencyInteractionAnalysis;
    private SecurityPermissionAnalysisResult securityPermissionAnalysis;
    private PerformanceResourceAnalysisResult performanceResourceAnalysis;
    
    // 综合统计信息
    private int totalIssues;
    private int criticalIssues;
    private int highIssues;
    private int mediumIssues;
    private int lowIssues;
    
    // 风险评级
    private String riskLevel;
    private String riskDescription;
    
    // 修复建议
    private List<FixRecommendation> fixRecommendations = new ArrayList<>();
    
    // 错误信息
    private List<String> errors = new ArrayList<>();
    
    public void addError(String error) {
        this.errors.add(error);
    }
}

