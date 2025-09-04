package io.metersphere.whitebox.controller;

import io.metersphere.whitebox.analyzer.*;
import io.metersphere.whitebox.service.WhiteboxTestingService;
import io.metersphere.whitebox.vo.ComprehensiveDefectReport;
import io.metersphere.whitebox.vo.ReportSummary;
import io.metersphere.whitebox.vo.WhiteboxTestReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/whitebox-testing")
public class WhiteboxTestingController {

    @Autowired
    private WhiteboxTestingService whiteboxTestingService;

    /**
     * 执行完整的白盒测试
     * @param serviceName 服务名称
     * @return 白盒测试报告
     */
    @PostMapping("/execute/{serviceName}")
    public WhiteboxTestReport executeWhiteboxTesting(@PathVariable String serviceName) {
        return whiteboxTestingService.executeWhiteboxTesting(serviceName);
    }

    /**
     * 解析JaCoCo覆盖率报告
     * @param reportPath 报告路径
     * @return 解析结果
     */
    @PostMapping("/jacoco/parse")
    public Object parseJacocoReport(@RequestParam String reportPath) {
        return whiteboxTestingService.parseJacocoReport(reportPath);
    }

    /**
     * 验证业务逻辑
     * @param serviceName 服务名称
     * @return 验证结果
     */
    @PostMapping("/business-logic/validate/{serviceName}")
    public Object validateBusinessLogic(@PathVariable String serviceName) {
        return whiteboxTestingService.validateBusinessLogic(serviceName);
    }

    /**
     * 分析圈复杂度
     * @param serviceName 服务名称
     * @return 分析结果
     */
    @PostMapping("/complexity/analyze/{serviceName}")
    public Object analyzeComplexity(@PathVariable String serviceName) {
        return whiteboxTestingService.analyzeComplexity(serviceName);
    }

    /**
     * 验证依赖注入
     * @param serviceName 服务名称
     * @return 验证结果
     */
    @PostMapping("/dependencies/validate/{serviceName}")
    public Object validateDependencies(@PathVariable String serviceName) {
        return whiteboxTestingService.validateDependencies(serviceName);
    }

    /**
     * 验证API契约
     * @param serviceName 服务名称
     * @return 验证结果
     */
    @PostMapping("/api-contracts/validate/{serviceName}")
    public Object validateApiContracts(@PathVariable String serviceName) {
        return whiteboxTestingService.validateApiContracts(serviceName);
    }

    /**
     * 测试容错能力
     * @param serviceName 服务名称
     * @return 测试结果
     */
    @PostMapping("/fault-tolerance/test/{serviceName}")
    public Object testFaultTolerance(@PathVariable String serviceName) {
        return whiteboxTestingService.testFaultTolerance(serviceName);
    }

    /**
     * 测试降级逻辑
     * @param serviceName 服务名称
     * @return 测试结果
     */
    @PostMapping("/fallback/test/{serviceName}")
    public Object testFallback(@PathVariable String serviceName) {
        return whiteboxTestingService.testFallback(serviceName);
    }

    /**
     * 验证安全性
     * @param serviceName 服务名称
     * @return 验证结果
     */
    @PostMapping("/security/validate/{serviceName}")
    public Object validateSecurity(@PathVariable String serviceName) {
        return whiteboxTestingService.validateSecurity(serviceName);
    }

    /**
     * 验证数据加密
     * @param serviceName 服务名称
     * @return 验证结果
     */
    @PostMapping("/data-encryption/validate/{serviceName}")
    public Object validateDataEncryption(@PathVariable String serviceName) {
        return whiteboxTestingService.validateDataEncryption(serviceName);
    }

    /**
     * 分析代码注释
     * @param serviceName 服务名称
     * @return 分析结果
     */
    @PostMapping("/comments/analyze/{serviceName}")
    public Object analyzeComments(@PathVariable String serviceName) {
        return whiteboxTestingService.analyzeComments(serviceName);
    }

    /**
     * 验证代码风格
     * @param serviceName 服务名称
     * @return 验证结果
     */
    @PostMapping("/code-style/validate/{serviceName}")
    public Object validateCodeStyle(@PathVariable String serviceName) {
        return whiteboxTestingService.validateCodeStyle(serviceName);
    }

    // ==================== 新增的六大类缺陷检测API ====================

    /**
     * 分析代码逻辑与控制流缺陷
     * @param serviceName 服务名称
     * @param sourcePath 源码路径
     * @return 控制流分析结果
     */
    @PostMapping("/control-flow/analyze/{serviceName}")
    public ControlFlowAnalysisResult analyzeControlFlow(@PathVariable String serviceName, 
                                                       @RequestParam String sourcePath) {
        return whiteboxTestingService.analyzeControlFlow(serviceName, sourcePath);
    }

    /**
     * 分析数据处理与变量管理缺陷
     * @param serviceName 服务名称
     * @param sourcePath 源码路径
     * @return 数据处理分析结果
     */
    @PostMapping("/data-processing/analyze/{serviceName}")
    public DataProcessingAnalysisResult analyzeDataProcessing(@PathVariable String serviceName, 
                                                             @RequestParam String sourcePath) {
        return whiteboxTestingService.analyzeDataProcessing(serviceName, sourcePath);
    }

    /**
     * 分析函数/方法实现缺陷
     * @param serviceName 服务名称
     * @param sourcePath 源码路径
     * @return 函数实现分析结果
     */
    @PostMapping("/function-implementation/analyze/{serviceName}")
    public FunctionImplementationAnalysisResult analyzeFunctionImplementation(@PathVariable String serviceName, 
                                                                             @RequestParam String sourcePath) {
        return whiteboxTestingService.analyzeFunctionImplementation(serviceName, sourcePath);
    }

    /**
     * 分析依赖与交互缺陷
     * @param serviceName 服务名称
     * @param sourcePath 源码路径
     * @return 依赖交互分析结果
     */
    @PostMapping("/dependency-interaction/analyze/{serviceName}")
    public DependencyInteractionAnalysisResult analyzeDependencyInteraction(@PathVariable String serviceName, 
                                                                           @RequestParam String sourcePath) {
        return whiteboxTestingService.analyzeDependencyInteraction(serviceName, sourcePath);
    }

    /**
     * 分析安全性与权限缺陷
     * @param serviceName 服务名称
     * @param sourcePath 源码路径
     * @return 安全权限分析结果
     */
    @PostMapping("/security-permission/analyze/{serviceName}")
    public SecurityPermissionAnalysisResult analyzeSecurityPermission(@PathVariable String serviceName, 
                                                                     @RequestParam String sourcePath) {
        return whiteboxTestingService.analyzeSecurityPermission(serviceName, sourcePath);
    }

    /**
     * 分析性能与资源缺陷
     * @param serviceName 服务名称
     * @param sourcePath 源码路径
     * @return 性能资源分析结果
     */
    @PostMapping("/performance-resource/analyze/{serviceName}")
    public PerformanceResourceAnalysisResult analyzePerformanceResource(@PathVariable String serviceName, 
                                                                       @RequestParam String sourcePath) {
        return whiteboxTestingService.analyzePerformanceResource(serviceName, sourcePath);
    }

    /**
     * 生成综合缺陷检测报告
     * @param serviceName 服务名称
     * @param sourcePath 源码路径
     * @return 综合缺陷检测报告
     */
    @PostMapping("/comprehensive-report/{serviceName}")
    public ComprehensiveDefectReport generateComprehensiveReport(@PathVariable String serviceName,
                                                                 @RequestParam String sourcePath) {
        return whiteboxTestingService.generateComprehensiveReport(serviceName, sourcePath);
    }

    /**
     * 获取报告摘要
     * @param serviceName 服务名称
     * @param sourcePath 源码路径
     * @return 报告摘要
     */
    @GetMapping("/report-summary/{serviceName}")
    public ReportSummary getReportSummary(@PathVariable String serviceName,
                                          @RequestParam String sourcePath) {
        return whiteboxTestingService.getReportSummary(serviceName, sourcePath);
    }
}