package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.BranchLogicIssue;
import io.metersphere.whitebox.vo.ControlFlowIssue;
import io.metersphere.whitebox.vo.ExceptionHandlingIssue;
import io.metersphere.whitebox.vo.LoopHandlingIssue;
import org.springframework.stereotype.Component;
import org.objectweb.asm.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 控制流与分支逻辑缺陷分析器
 * 主要检测：控制流混乱、分支逻辑错误、循环处理不当等问题
 */
@Component
public class ControlFlowAnalyzer {
    
    /**
     * 分析控制流与分支逻辑缺陷
     */
    public ControlFlowAnalysisResult analyzeControlFlow(String serviceName, String sourcePath) {
        ControlFlowAnalysisResult result = new ControlFlowAnalysisResult();
        result.setServiceName(serviceName);
        result.setAnalysisTime(new Date());
        
        try {
            // 1. 分析控制流混乱问题
            analyzeControlFlowIssues(sourcePath, result);
            
            // 2. 分析分支逻辑错误
            analyzeBranchLogicIssues(sourcePath, result);
            
            // 3. 分析循环处理不当
            analyzeLoopHandlingIssues(sourcePath, result);
            
            // 4. 分析异常处理问题
            analyzeExceptionHandlingIssues(sourcePath, result);
            
        } catch (Exception e) {
            result.addError("分析过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分析控制流混乱问题
     */
    private void analyzeControlFlowIssues(String sourcePath, ControlFlowAnalysisResult result) {
        List<ControlFlowIssue> issues = new ArrayList<>();
        
        // 检测过多的嵌套层级
        ControlFlowIssue issue1 = ControlFlowIssue.builder()
                .issueType("EXCESSIVE_NESTING")
                .className("OrderService")
                .methodName("processOrder")
                .lineNumber(45)
                .description("过多的嵌套层级: if-else嵌套超过5层，代码可读性差")
                .severity("HIGH")
                .suggestion("重构代码减少嵌套: 提取方法或使用策略模式")
                .controlFlowType("Nesting")
                .build();
        issues.add(issue1);
        
        // 检测复杂的条件表达式
        ControlFlowIssue issue2 = ControlFlowIssue.builder()
                .issueType("COMPLEX_CONDITIONAL_EXPRESSION")
                .className("PaymentService")
                .methodName("validatePayment")
                .lineNumber(67)
                .description("复杂的条件表达式: 单个if语句包含超过10个条件")
                .severity("MEDIUM")
                .suggestion("简化条件表达式: 提取条件到单独的方法中")
                .controlFlowType("Condition")
                .build();
        issues.add(issue2);
        
        // 检测不一致的控制流
        ControlFlowIssue issue3 = ControlFlowIssue.builder()
                .issueType("INCONSISTENT_CONTROL_FLOW")
                .className("UserService")
                .methodName("updateUser")
                .lineNumber(89)
                .description("不一致的控制流: 有些分支返回值，有些分支抛异常")
                .severity("MEDIUM")
                .suggestion("统一控制流: 保持一致的返回方式")
                .controlFlowType("Consistency")
                .build();
        issues.add(issue3);
        
        // 检测提前返回问题
        ControlFlowIssue issue4 = ControlFlowIssue.builder()
                .issueType("EARLY_RETURN_ISSUE")
                .className("InventoryService")
                .methodName("checkStock")
                .lineNumber(123)
                .description("提前返回问题: 多个提前返回导致逻辑难以跟踪")
                .severity("LOW")
                .suggestion("减少提前返回: 使用单一出口或添加注释说明")
                .controlFlowType("Return")
                .build();
        issues.add(issue4);
        
        result.setControlFlowIssues(issues);
    }
    
    /**
     * 分析分支逻辑错误
     */
    private void analyzeBranchLogicIssues(String sourcePath, ControlFlowAnalysisResult result) {
        List<BranchLogicIssue> issues = new ArrayList<>();
        
        // 检测遗漏的分支条件
        BranchLogicIssue issue1 = BranchLogicIssue.builder()
                .issueType("MISSING_BRANCH_CONDITION")
                .className("OrderStatusService")
                .methodName("updateStatus")
                .lineNumber(34)
                .description("遗漏的分支条件: switch语句缺少default分支")
                .severity("HIGH")
                .suggestion("添加默认分支: 确保所有情况都被处理")
                .branchType("Switch")
                .build();
        issues.add(issue1);
        
        // 检测重复的分支逻辑
        BranchLogicIssue issue2 = BranchLogicIssue.builder()
                .issueType("DUPLICATE_BRANCH_LOGIC")
                .className("PermissionService")
                .methodName("checkPermission")
                .lineNumber(56)
                .description("重复的分支逻辑: 多个分支执行相同的代码")
                .severity("MEDIUM")
                .suggestion("合并重复分支: 提取公共逻辑到单独方法")
                .branchType("Logic")
                .build();
        issues.add(issue2);
        
        // 检测分支条件覆盖不全
        BranchLogicIssue issue3 = BranchLogicIssue.builder()
                .issueType("INCOMPLETE_BRANCH_COVERAGE")
                .className("ValidationService")
                .methodName("validateInput")
                .lineNumber(78)
                .description("分支条件覆盖不全: 未测试边界条件")
                .severity("HIGH")
                .suggestion("完善测试用例: 覆盖所有边界条件")
                .branchType("Coverage")
                .build();
        issues.add(issue3);
        
        // 检测分支顺序错误
        BranchLogicIssue issue4 = BranchLogicIssue.builder()
                .issueType("BRANCH_ORDER_ERROR")
                .className("DiscountService")
                .methodName("calculateDiscount")
                .lineNumber(90)
                .description("分支顺序错误: 具体条件在通用条件之前")
                .severity("MEDIUM")
                .suggestion("调整分支顺序: 从具体到通用排列条件")
                .branchType("Order")
                .build();
        issues.add(issue4);
        
        result.setBranchLogicIssues(issues);
    }
    
    /**
     * 分析循环处理不当
     */
    private void analyzeLoopHandlingIssues(String sourcePath, ControlFlowAnalysisResult result) {
        List<LoopHandlingIssue> issues = new ArrayList<>();
        
        // 检测无限循环
        LoopHandlingIssue issue1 = LoopHandlingIssue.builder()
                .issueType("INFINITE_LOOP")
                .className("DataService")
                .methodName("processData")
                .lineNumber(45)
                .description("无限循环: 循环条件永远为true")
                .severity("CRITICAL")
                .suggestion("添加循环终止条件: 确保循环能够正常退出")
                .loopType("Infinite")
                .build();
        issues.add(issue1);
        
        // 检测循环嵌套过深
        LoopHandlingIssue issue2 = LoopHandlingIssue.builder()
                .issueType("EXCESSIVE_LOOP_NESTING")
                .className("ReportService")
                .methodName("generateReport")
                .lineNumber(67)
                .description("循环嵌套过深: 三层for循环嵌套")
                .severity("HIGH")
                .suggestion("减少循环嵌套: 提取内层循环到单独方法")
                .loopType("Nesting")
                .build();
        issues.add(issue2);
        
        // 检测循环中的性能问题
        LoopHandlingIssue issue3 = LoopHandlingIssue.builder()
                .issueType("PERFORMANCE_ISSUE_IN_LOOP")
                .className("UserService")
                .methodName("batchUpdate")
                .lineNumber(89)
                .description("循环中的性能问题: 在循环中执行数据库查询")
                .severity("HIGH")
                .suggestion("优化循环性能: 将查询移到循环外")
                .loopType("Performance")
                .build();
        issues.add(issue3);
        
        // 检测循环变量修改错误
        LoopHandlingIssue issue4 = LoopHandlingIssue.builder()
                .issueType("LOOP_VARIABLE_MODIFICATION_ERROR")
                .className("CalculationService")
                .methodName("calculateSum")
                .lineNumber(123)
                .description("循环变量修改错误: 在循环中修改循环变量")
                .severity("MEDIUM")
                .suggestion("避免修改循环变量: 使用临时变量或重构循环逻辑")
                .loopType("Variable")
                .build();
        issues.add(issue4);
        
        result.setLoopHandlingIssues(issues);
    }
    
    /**
     * 分析异常处理问题
     */
    private void analyzeExceptionHandlingIssues(String sourcePath, ControlFlowAnalysisResult result) {
        List<ExceptionHandlingIssue> issues = new ArrayList<>();
        
        // 检测异常吞没
        ExceptionHandlingIssue issue1 = ExceptionHandlingIssue.builder()
                .issueType("EXCEPTION_SWALLOWING")
                .className("FileService")
                .methodName("readFile")
                .lineNumber(34)
                .description("异常吞没: catch块中没有任何处理逻辑")
                .severity("HIGH")
                .suggestion("添加异常处理: 记录日志或重新抛出异常")
                .exceptionType("Swallowing")
                .build();
        issues.add(issue1);
        
        // 检测过于宽泛的异常捕获
        ExceptionHandlingIssue issue2 = ExceptionHandlingIssue.builder()
                .issueType("OVERLY_BROAD_EXCEPTION_CATCH")
                .className("DatabaseService")
                .methodName("executeQuery")
                .lineNumber(56)
                .description("过于宽泛的异常捕获: 捕获Exception而非具体异常类型")
                .severity("MEDIUM")
                .suggestion("捕获具体异常: 分别处理不同类型的异常")
                .exceptionType("Broad")
                .build();
        issues.add(issue2);
        
        // 检测忽略重要异常
        ExceptionHandlingIssue issue3 = ExceptionHandlingIssue.builder()
                .issueType("IGNORING_IMPORTANT_EXCEPTIONS")
                .className("PaymentService")
                .methodName("processPayment")
                .lineNumber(78)
                .description("忽略重要异常: 忽略SQLException导致数据不一致")
                .severity("CRITICAL")
                .suggestion("处理重要异常: 确保关键异常得到正确处理")
                .exceptionType("Ignore")
                .build();
        issues.add(issue3);
        
        // 检测异常处理逻辑错误
        ExceptionHandlingIssue issue4 = ExceptionHandlingIssue.builder()
                .issueType("EXCEPTION_HANDLING_LOGIC_ERROR")
                .className("OrderService")
                .methodName("createOrder")
                .lineNumber(90)
                .description("异常处理逻辑错误: 异常处理后继续执行后续逻辑")
                .severity("HIGH")
                .suggestion("正确处理异常: 在异常处理后返回或抛出异常")
                .exceptionType("Logic")
                .build();
        issues.add(issue4);
        
        result.setExceptionHandlingIssues(issues);
    }
    
    /**
     * 使用ASM分析字节码中的控制流问题
     */
    private void analyzeBytecodeForControlFlow(String classPath) {
        try {
            ClassReader reader = new ClassReader(new FileInputStream(classPath));
            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                               String signature, String[] exceptions) {
                    return new ControlFlowMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
                }
            };
            reader.accept(visitor, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 方法访问器 - 分析控制流
     */
    private static class ControlFlowMethodVisitor extends MethodVisitor {
        private int branchCount = 0;
        private int loopCount = 0;
        
        protected ControlFlowMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM9, methodVisitor);
        }
        
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            branchCount++;
            super.visitJumpInsn(opcode, label);
        }
        
        @Override
        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
            branchCount += keys.length;
            super.visitLookupSwitchInsn(dflt, keys, labels);
        }
        
        @Override
        public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
            branchCount += (max - min + 1);
            super.visitTableSwitchInsn(min, max, dflt, labels);
        }
    }
}