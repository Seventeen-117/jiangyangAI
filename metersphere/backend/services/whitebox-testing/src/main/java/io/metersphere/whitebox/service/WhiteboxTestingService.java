package io.metersphere.whitebox.service;

import io.metersphere.whitebox.analyzer.*;
import io.metersphere.whitebox.parser.JacocoReportParser;
import io.metersphere.whitebox.parser.SonarQubeReportParser;
import io.metersphere.whitebox.report.*;
import io.metersphere.whitebox.tester.FaultToleranceTester;
import io.metersphere.whitebox.validator.*;
import io.metersphere.whitebox.vo.ComprehensiveDefectReport;
import io.metersphere.whitebox.vo.ReportSummary;
import io.metersphere.whitebox.vo.WhiteboxTestReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class WhiteboxTestingService {

    @Autowired
    private JacocoReportParser jacocoReportParser;

    @Autowired
    private SonarQubeReportParser sonarQubeReportParser;

    @Autowired
    private BusinessLogicValidator businessLogicValidator;

    @Autowired
    private CyclomaticComplexityAnalyzer cyclomaticComplexityAnalyzer;

    @Autowired
    private DependencyInjectionValidator dependencyInjectionValidator;

    @Autowired
    private ApiContractValidator apiContractValidator;

    @Autowired
    private FaultToleranceTester faultToleranceTester;

    @Autowired
    private SecurityValidator securityValidator;

    @Autowired
    private CommentAnalyzer commentAnalyzer;

    @Autowired
    private CodeStyleValidator codeStyleValidator;

    @Autowired
    private WhiteboxTestReportGenerator reportGenerator;

    // 新增的分析器
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

    @Autowired
    private ComprehensiveDefectReportGenerator comprehensiveDefectReportGenerator;

    /**
     * 执行完整的白盒测试
     * @param serviceName 服务名称
     * @return 白盒测试报告
     */
    public WhiteboxTestReport executeWhiteboxTesting(String serviceName) {
        // 生成综合测试报告
        WhiteboxTestReport report = reportGenerator.generateReport(serviceName);
        
        // 执行额外的测试并添加到报告中
        List<Object> additionalResults = new ArrayList<>();
        
        // 执行业务逻辑验证
        additionalResults.add(businessLogicValidator.validateServiceLogic(serviceName));
        
        // 分析圈复杂度
        additionalResults.add(cyclomaticComplexityAnalyzer.analyzeServiceComplexity(serviceName));
        
        // 验证依赖注入
        additionalResults.add(dependencyInjectionValidator.validateDependencies(serviceName));
        
        // 验证API契约
        additionalResults.add(apiContractValidator.validateApiContracts(serviceName));
        
        // 测试容错能力
        additionalResults.add(faultToleranceTester.testCircuitBreaker(serviceName));
        
        // 测试降级逻辑
        additionalResults.add(faultToleranceTester.testFallback(serviceName));
        
        // 验证安全性
        additionalResults.add(securityValidator.validateAuthorization(serviceName));
        
        // 验证数据加密
        additionalResults.add(securityValidator.validateDataEncryption(serviceName));
        
        // 分析代码注释
        additionalResults.add(commentAnalyzer.analyzeComments(serviceName));
        
        // 验证代码风格
        additionalResults.add(codeStyleValidator.validateCodeStyle(serviceName));
        
        // 返回完整的测试报告
        return report;
    }

    /**
     * 执行完整的白盒测试（带参数）
     * @param serviceName 服务名称
     * @param jacocoReportPath JaCoCo报告路径
     * @param sonarProjectKey SonarQube项目Key
     * @return 白盒测试报告
     */
    public WhiteboxTestReport executeWhiteboxTesting(String serviceName, String jacocoReportPath, String sonarProjectKey) {
        // 生成综合测试报告
        WhiteboxTestReport report = reportGenerator.generateReport(serviceName, jacocoReportPath, sonarProjectKey);
        
        // 执行额外的测试并添加到报告中
        List<Object> additionalResults = new ArrayList<>();
        
        // 执行业务逻辑验证
        additionalResults.add(businessLogicValidator.validateServiceLogic(serviceName));
        
        // 分析圈复杂度
        additionalResults.add(cyclomaticComplexityAnalyzer.analyzeServiceComplexity(serviceName));
        
        // 验证依赖注入
        additionalResults.add(dependencyInjectionValidator.validateDependencies(serviceName));
        
        // 验证API契约
        additionalResults.add(apiContractValidator.validateApiContracts(serviceName));
        
        // 测试容错能力
        additionalResults.add(faultToleranceTester.testCircuitBreaker(serviceName));
        
        // 测试降级逻辑
        additionalResults.add(faultToleranceTester.testFallback(serviceName));
        
        // 验证安全性
        additionalResults.add(securityValidator.validateAuthorization(serviceName));
        
        // 验证数据加密
        additionalResults.add(securityValidator.validateDataEncryption(serviceName));
        
        // 分析代码注释
        additionalResults.add(commentAnalyzer.analyzeComments(serviceName));
        
        // 验证代码风格
        additionalResults.add(codeStyleValidator.validateCodeStyle(serviceName));
        
        // 返回完整的测试报告
        return report;
    }

    /**
     * 解析JaCoCo覆盖率报告
     * @param reportPath 报告路径
     * @return 解析结果
     */
    public Object parseJacocoReport(String reportPath) {
        return jacocoReportParser.parseReport(reportPath);
    }

    /**
     * 验证业务逻辑
     * @param serviceName 服务名称
     * @return 验证结果
     */
    public Object validateBusinessLogic(String serviceName) {
        return businessLogicValidator.validateServiceLogic(serviceName);
    }

    /**
     * 分析圈复杂度
     * @param serviceName 服务名称
     * @return 分析结果
     */
    public Object analyzeComplexity(String serviceName) {
        return cyclomaticComplexityAnalyzer.analyzeServiceComplexity(serviceName);
    }

    /**
     * 验证依赖注入
     * @param serviceName 服务名称
     * @return 验证结果
     */
    public Object validateDependencies(String serviceName) {
        return dependencyInjectionValidator.validateDependencies(serviceName);
    }

    /**
     * 验证API契约
     * @param serviceName 服务名称
     * @return 验证结果
     */
    public Object validateApiContracts(String serviceName) {
        return apiContractValidator.validateApiContracts(serviceName);
    }

    /**
     * 测试容错能力
     * @param serviceName 服务名称
     * @return 测试结果
     */
    public Object testFaultTolerance(String serviceName) {
        return faultToleranceTester.testCircuitBreaker(serviceName);
    }

    /**
     * 测试降级逻辑
     * @param serviceName 服务名称
     * @return 测试结果
     */
    public Object testFallback(String serviceName) {
        return faultToleranceTester.testFallback(serviceName);
    }

    /**
     * 验证安全性
     * @param serviceName 服务名称
     * @return 验证结果
     */
    public Object validateSecurity(String serviceName) {
        return securityValidator.validateAuthorization(serviceName);
    }

    /**
     * 验证数据加密
     * @param serviceName 服务名称
     * @return 验证结果
     */
    public Object validateDataEncryption(String serviceName) {
        return securityValidator.validateDataEncryption(serviceName);
    }

    /**
     * 分析代码注释
     * @param serviceName 服务名称
     * @return 分析结果
     */
    public Object analyzeComments(String serviceName) {
        return commentAnalyzer.analyzeComments(serviceName);
    }

    /**
     * 验证代码风格
     * @param serviceName 服务名称
     * @return 验证结果
     */
    public Object validateCodeStyle(String serviceName) {
        return codeStyleValidator.validateCodeStyle(serviceName);
    }

    // ==================== 新增的六大类缺陷检测方法 ====================

    /**
     * 分析代码逻辑与控制流缺陷
     */
    public ControlFlowAnalysisResult analyzeControlFlow(String serviceName, String sourcePath) {
        return controlFlowAnalyzer.analyzeControlFlow(serviceName, sourcePath);
    }

    /**
     * 分析数据处理与变量管理缺陷
     */
    public DataProcessingAnalysisResult analyzeDataProcessing(String serviceName, String sourcePath) {
        return dataProcessingAnalyzer.analyzeDataProcessing(serviceName, sourcePath);
    }

    /**
     * 分析函数/方法实现缺陷
     */
    public FunctionImplementationAnalysisResult analyzeFunctionImplementation(String serviceName, String sourcePath) {
        return functionImplementationAnalyzer.analyzeFunctionImplementation(serviceName, sourcePath);
    }

    /**
     * 分析依赖与交互缺陷
     */
    public DependencyInteractionAnalysisResult analyzeDependencyInteraction(String serviceName, String sourcePath) {
        return dependencyInteractionAnalyzer.analyzeDependencyInteraction(serviceName, sourcePath);
    }

    /**
     * 分析安全性与权限缺陷
     */
    public SecurityPermissionAnalysisResult analyzeSecurityPermission(String serviceName, String sourcePath) {
        return securityPermissionAnalyzer.analyzeSecurityPermission(serviceName, sourcePath);
    }

    /**
     * 分析性能与资源缺陷
     */
    public PerformanceResourceAnalysisResult analyzePerformanceResource(String serviceName, String sourcePath) {
        return performanceResourceAnalyzer.analyzePerformanceResource(serviceName, sourcePath);
    }

    /**
     * 生成综合缺陷检测报告
     */
    public ComprehensiveDefectReport generateComprehensiveReport(String serviceName, String sourcePath) {
        return comprehensiveDefectReportGenerator.generateComprehensiveReport(serviceName, sourcePath);
    }

    /**
     * 获取报告摘要
     */
    public ReportSummary getReportSummary(String serviceName, String sourcePath) {
        ComprehensiveDefectReport report = generateComprehensiveReport(serviceName, sourcePath);
        return comprehensiveDefectReportGenerator.generateReportSummary(report);
    }
}