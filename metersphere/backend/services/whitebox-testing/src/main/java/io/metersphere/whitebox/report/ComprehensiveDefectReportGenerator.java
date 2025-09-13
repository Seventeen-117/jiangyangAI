package io.metersphere.whitebox.report;

import io.metersphere.whitebox.analyzer.*;
import io.metersphere.whitebox.vo.ComprehensiveDefectReport;
import io.metersphere.whitebox.vo.FixRecommendation;
import io.metersphere.whitebox.vo.ReportSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * 综合缺陷检测报告生成器
 * 整合六大类缺陷检测结果，生成完整的白盒测试报告
 */
@Component
public class ComprehensiveDefectReportGenerator {
    
    @Autowired
    private ControlFlowAnalyzer controlFlowAnalyzer;
    
    @Autowired
    private DataProcessingAnalyzer dataProcessingAnalyzer;
    
    @Autowired
    private FunctionImplementationAnalyzer functionImplementationAnalyzer;
    
    @Autowired
    private DependencyInteractionAnalyzer dependencyInteractionAnalyzer;
    
    @Autowired
    private SecurityPermissionAnalyzer securityPermissionAnalyzer;
    
    @Autowired
    private PerformanceResourceAnalyzer performanceResourceAnalyzer;
    
    /**
     * 生成综合缺陷检测报告
     */
    public ComprehensiveDefectReport generateComprehensiveReport(String serviceName, String sourcePath) {
        ComprehensiveDefectReport report = ComprehensiveDefectReport.builder()
                .serviceName(serviceName)
                .generatedTime(new Date())
                .reportId(UUID.randomUUID().toString())
                .build();
        
        try {
            // 1. 执行控制流分析
            ControlFlowAnalysisResult controlFlowResult = controlFlowAnalyzer.analyzeControlFlow(serviceName, sourcePath);
            controlFlowResult.calculateStatistics();
            report.setControlFlowAnalysis(controlFlowResult);
            
            // 2. 执行数据处理分析
            DataProcessingAnalysisResult dataProcessingResult = dataProcessingAnalyzer.analyzeDataProcessing(serviceName, sourcePath);
            dataProcessingResult.calculateStatistics();
            report.setDataProcessingAnalysis(dataProcessingResult);
            
            // 3. 执行函数实现分析
            FunctionImplementationAnalysisResult functionImplementationResult = functionImplementationAnalyzer.analyzeFunctionImplementation(serviceName, sourcePath);
            functionImplementationResult.calculateStatistics();
            report.setFunctionImplementationAnalysis(functionImplementationResult);
            
            // 4. 执行依赖交互分析
            DependencyInteractionAnalysisResult dependencyInteractionResult = dependencyInteractionAnalyzer.analyzeDependencyInteraction(serviceName, sourcePath);
            dependencyInteractionResult.calculateStatistics();
            report.setDependencyInteractionAnalysis(dependencyInteractionResult);
            
            // 5. 执行安全权限分析
            SecurityPermissionAnalysisResult securityPermissionResult = securityPermissionAnalyzer.analyzeSecurityPermission(serviceName, sourcePath);
            securityPermissionResult.calculateStatistics();
            report.setSecurityPermissionAnalysis(securityPermissionResult);
            
            // 6. 执行性能资源分析
            PerformanceResourceAnalysisResult performanceResourceResult = performanceResourceAnalyzer.analyzePerformanceResource(serviceName, sourcePath);
            performanceResourceResult.calculateStatistics();
            report.setPerformanceResourceAnalysis(performanceResourceResult);
            
            // 7. 计算综合统计信息
            calculateOverallStatistics(report);
            
            // 8. 生成风险评级
            generateRiskRating(report);
            
            // 9. 生成修复建议
            generateFixRecommendations(report);
            
        } catch (Exception e) {
            report.addError("生成报告过程中发生错误: " + e.getMessage());
        }
        
        return report;
    }
    
    /**
     * 计算综合统计信息
     */
    private void calculateOverallStatistics(ComprehensiveDefectReport report) {
        int totalIssues = 0;
        int criticalIssues = 0;
        int highIssues = 0;
        int mediumIssues = 0;
        int lowIssues = 0;
        
        // 统计控制流分析结果
        if (report.getControlFlowAnalysis() != null) {
            totalIssues += report.getControlFlowAnalysis().getTotalIssues();
            criticalIssues += report.getControlFlowAnalysis().getCriticalIssues();
            highIssues += report.getControlFlowAnalysis().getHighIssues();
            mediumIssues += report.getControlFlowAnalysis().getMediumIssues();
            lowIssues += report.getControlFlowAnalysis().getLowIssues();
        }
        
        // 统计数据处理分析结果
        if (report.getDataProcessingAnalysis() != null) {
            totalIssues += report.getDataProcessingAnalysis().getTotalIssues();
            criticalIssues += report.getDataProcessingAnalysis().getCriticalIssues();
            highIssues += report.getDataProcessingAnalysis().getHighIssues();
            mediumIssues += report.getDataProcessingAnalysis().getMediumIssues();
            lowIssues += report.getDataProcessingAnalysis().getLowIssues();
        }
        
        // 统计函数实现分析结果
        if (report.getFunctionImplementationAnalysis() != null) {
            totalIssues += report.getFunctionImplementationAnalysis().getTotalIssues();
            criticalIssues += report.getFunctionImplementationAnalysis().getCriticalIssues();
            highIssues += report.getFunctionImplementationAnalysis().getHighIssues();
            mediumIssues += report.getFunctionImplementationAnalysis().getMediumIssues();
            lowIssues += report.getFunctionImplementationAnalysis().getLowIssues();
        }
        
        // 统计依赖交互分析结果
        if (report.getDependencyInteractionAnalysis() != null) {
            totalIssues += report.getDependencyInteractionAnalysis().getTotalIssues();
            criticalIssues += report.getDependencyInteractionAnalysis().getCriticalIssues();
            highIssues += report.getDependencyInteractionAnalysis().getHighIssues();
            mediumIssues += report.getDependencyInteractionAnalysis().getMediumIssues();
            lowIssues += report.getDependencyInteractionAnalysis().getLowIssues();
        }
        
        // 统计安全权限分析结果
        if (report.getSecurityPermissionAnalysis() != null) {
            totalIssues += report.getSecurityPermissionAnalysis().getTotalIssues();
            criticalIssues += report.getSecurityPermissionAnalysis().getCriticalIssues();
            highIssues += report.getSecurityPermissionAnalysis().getHighIssues();
            mediumIssues += report.getSecurityPermissionAnalysis().getMediumIssues();
            lowIssues += report.getSecurityPermissionAnalysis().getLowIssues();
        }
        
        // 统计性能资源分析结果
        if (report.getPerformanceResourceAnalysis() != null) {
            totalIssues += report.getPerformanceResourceAnalysis().getTotalIssues();
            criticalIssues += report.getPerformanceResourceAnalysis().getCriticalIssues();
            highIssues += report.getPerformanceResourceAnalysis().getHighIssues();
            mediumIssues += report.getPerformanceResourceAnalysis().getMediumIssues();
            lowIssues += report.getPerformanceResourceAnalysis().getLowIssues();
        }
        
        report.setTotalIssues(totalIssues);
        report.setCriticalIssues(criticalIssues);
        report.setHighIssues(highIssues);
        report.setMediumIssues(mediumIssues);
        report.setLowIssues(lowIssues);
    }
    
    /**
     * 生成风险评级
     */
    private void generateRiskRating(ComprehensiveDefectReport report) {
        int criticalCount = report.getCriticalIssues();
        int highCount = report.getHighIssues();
        int mediumCount = report.getMediumIssues();
        int lowCount = report.getLowIssues();
        
        String riskLevel;
        String riskDescription;
        
        if (criticalCount > 0) {
            riskLevel = "CRITICAL";
            riskDescription = "存在严重缺陷，需要立即修复";
        } else if (highCount > 5) {
            riskLevel = "HIGH";
            riskDescription = "存在多个高风险缺陷，建议优先修复";
        } else if (highCount > 0 || mediumCount > 10) {
            riskLevel = "MEDIUM";
            riskDescription = "存在中等风险缺陷，建议逐步修复";
        } else if (mediumCount > 0 || lowCount > 20) {
            riskLevel = "LOW";
            riskDescription = "存在少量低风险缺陷，可选择性修复";
        } else {
            riskLevel = "SAFE";
            riskDescription = "代码质量良好，无明显缺陷";
        }
        
        report.setRiskLevel(riskLevel);
        report.setRiskDescription(riskDescription);
    }
    
    /**
     * 生成修复建议
     */
    private void generateFixRecommendations(ComprehensiveDefectReport report) {
        List<FixRecommendation> recommendations = new ArrayList<>();
        
        // 根据缺陷类型生成修复建议
        if (report.getCriticalIssues() > 0) {
            FixRecommendation rec1 = FixRecommendation.builder()
                    .priority("URGENT")
                    .category("Critical Issues")
                    .description("立即修复所有严重缺陷，这些缺陷可能导致系统崩溃或安全漏洞")
                    .estimatedEffort("2-5 days")
                    .build();
            recommendations.add(rec1);
        }
        
        if (report.getHighIssues() > 0) {
            FixRecommendation rec2 = FixRecommendation.builder()
                    .priority("HIGH")
                    .category("High Priority Issues")
                    .description("优先修复高风险缺陷，这些缺陷可能影响系统稳定性")
                    .estimatedEffort("1-3 days")
                    .build();
            recommendations.add(rec2);
        }
        
        if (report.getMediumIssues() > 0) {
            FixRecommendation rec3 = FixRecommendation.builder()
                    .priority("MEDIUM")
                    .category("Medium Priority Issues")
                    .description("逐步修复中等风险缺陷，提升代码质量")
                    .estimatedEffort("3-7 days")
                    .build();
            recommendations.add(rec3);
        }
        
        if (report.getLowIssues() > 0) {
            FixRecommendation rec4 = FixRecommendation.builder()
                    .priority("LOW")
                    .category("Low Priority Issues")
                    .description("可选择性修复低风险缺陷，优化代码可维护性")
                    .estimatedEffort("1-2 days")
                    .build();
            recommendations.add(rec4);
        }
        
        // 添加通用建议
        FixRecommendation rec5 = FixRecommendation.builder()
                .priority("GENERAL")
                .category("Code Quality Improvement")
                .description("建议建立代码审查机制，定期进行白盒测试，持续改进代码质量")
                .estimatedEffort("Ongoing")
                .build();
        recommendations.add(rec5);
        
        report.setFixRecommendations(recommendations);
    }
    
    /**
     * 生成报告摘要
     */
    public ReportSummary generateReportSummary(ComprehensiveDefectReport report) {
        return ReportSummary.builder()
                .serviceName(report.getServiceName())
                .generatedTime(report.getGeneratedTime())
                .totalIssues(report.getTotalIssues())
                .criticalIssues(report.getCriticalIssues())
                .highIssues(report.getHighIssues())
                .mediumIssues(report.getMediumIssues())
                .lowIssues(report.getLowIssues())
                .riskLevel(report.getRiskLevel())
                .riskDescription(report.getRiskDescription())
                .build();
    }
}
