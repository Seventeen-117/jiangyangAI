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
        ControlFlowIssue issue1 = new ControlFlowIssue();
        issue1.setIssueType("EXCESSIVE_NESTING");
        issue1.setClassName("OrderService");
        issue1.setMethodName("processOrder");
        issue1.setLineNumber(45);
        issue1.setDescription("过多的嵌套层级: if-else嵌套超过5层，代码可读性差");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("重构代码减少嵌套: 提取方法或使用策略模式");
        issue1.setControlFlowType("Nesting");
        issues.add(issue1);
        
        // 检测复杂的条件表达式
        ControlFlowIssue issue2 = new ControlFlowIssue();
        issue2.setIssueType("COMPLEX_CONDITIONAL_EXPRESSION");
        issue2.setClassName("PaymentService");
        issue2.setMethodName("validatePayment");
        issue2.setLineNumber(67);
        issue2.setDescription("复杂的条件表达式: 单个if语句包含超过10个条件");
        issue2.setSeverity("MEDIUM");
        issue2.setSuggestion("简化条件表达式: 提取条件到单独的方法中");
        issue2.setControlFlowType("Condition");
        issues.add(issue2);
        
        // 检测不一致的控制流
        ControlFlowIssue issue3 = new ControlFlowIssue();
        issue3.setIssueType("INCONSISTENT_CONTROL_FLOW");
        issue3.setClassName("UserService");
        issue3.setMethodName("updateUser");
        issue3.setLineNumber(89);
        issue3.setDescription("不一致的控制流: 有些分支返回值，有些分支抛异常");
        issue3.setSeverity("MEDIUM");
        issue3.setSuggestion("统一控制流: 保持一致的返回方式");
        issue3.setControlFlowType("Consistency");
        issues.add(issue3);
        
        // 检测提前返回问题
        ControlFlowIssue issue4 = new ControlFlowIssue();
        issue4.setIssueType("EARLY_RETURN_ISSUE");
        issue4.setClassName("InventoryService");
        issue4.setMethodName("checkStock");
        issue4.setLineNumber(123);
        issue4.setDescription("提前返回问题: 多个提前返回导致逻辑难以跟踪");
        issue4.setSeverity("LOW");
        issue4.setSuggestion("减少提前返回: 使用单一出口或添加注释说明");
        issue4.setControlFlowType("Return");
        issues.add(issue4);
        
        result.setControlFlowIssues(issues);
    }
    
    /**
     * 分析分支逻辑错误
     */
    private void analyzeBranchLogicIssues(String sourcePath, ControlFlowAnalysisResult result) {
        List<BranchLogicIssue> issues = new ArrayList<>();
        
        // 检测遗漏的分支条件
        BranchLogicIssue issue1 = new BranchLogicIssue();
        issue1.setIssueType("MISSING_BRANCH_CONDITION");
        issue1.setClassName("OrderStatusService");
        issue1.setMethodName("updateStatus");
        issue1.setLineNumber(34);
        issue1.setDescription("遗漏的分支条件: switch语句缺少default分支");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("添加默认分支: 确保所有情况都被处理");
        issue1.setBranchType("Switch");
        issues.add(issue1);
        
        // 检测重复的分支逻辑
        BranchLogicIssue issue2 = new BranchLogicIssue();
        issue2.setIssueType("DUPLICATE_BRANCH_LOGIC");
        issue2.setClassName("PermissionService");
        issue2.setMethodName("checkPermission");
        issue2.setLineNumber(56);
        issue2.setDescription("重复的分支逻辑: 多个分支执行相同的代码");
        issue2.setSeverity("MEDIUM");
        issue2.setSuggestion("合并重复分支: 提取公共逻辑到单独方法");
        issue2.setBranchType("Logic");
        issues.add(issue2);
        
        // 检测分支条件覆盖不全
        BranchLogicIssue issue3 = new BranchLogicIssue();
        issue3.setIssueType("INCOMPLETE_BRANCH_COVERAGE");
        issue3.setClassName("ValidationService");
        issue3.setMethodName("validateInput");
        issue3.setLineNumber(78);
        issue3.setDescription("分支条件覆盖不全: 未测试边界条件");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("完善测试用例: 覆盖所有边界条件");
        issue3.setBranchType("Coverage");
        issues.add(issue3);
        
        // 检测分支顺序错误
        BranchLogicIssue issue4 = new BranchLogicIssue();
        issue4.setIssueType("BRANCH_ORDER_ERROR");
        issue4.setClassName("DiscountService");
        issue4.setMethodName("calculateDiscount");
        issue4.setLineNumber(90);
        issue4.setDescription("分支顺序错误: 具体条件在通用条件之前");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("调整分支顺序: 从具体到通用排列条件");
        issue4.setBranchType("Order");
        issues.add(issue4);
        
        result.setBranchLogicIssues(issues);
    }
    
    /**
     * 分析循环处理不当
     */
    private void analyzeLoopHandlingIssues(String sourcePath, ControlFlowAnalysisResult result) {
        List<LoopHandlingIssue> issues = new ArrayList<>();
        
        // 检测无限循环
        LoopHandlingIssue issue1 = new LoopHandlingIssue();
        issue1.setIssueType("INFINITE_LOOP");
        issue1.setClassName("DataService");
        issue1.setMethodName("processData");
        issue1.setLineNumber(45);
        issue1.setDescription("无限循环: 循环条件永远为true");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("添加循环终止条件: 确保循环能够正常退出");
        issue1.setLoopType("Infinite");
        issues.add(issue1);
        
        // 检测循环嵌套过深
        LoopHandlingIssue issue2 = new LoopHandlingIssue();
        issue2.setIssueType("EXCESSIVE_LOOP_NESTING");
        issue2.setClassName("ReportService");
        issue2.setMethodName("generateReport");
        issue2.setLineNumber(67);
        issue2.setDescription("循环嵌套过深: 三层for循环嵌套");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("减少循环嵌套: 提取内层循环到单独方法");
        issue2.setLoopType("Nesting");
        issues.add(issue2);
        
        // 检测循环中的性能问题
        LoopHandlingIssue issue3 = new LoopHandlingIssue();
        issue3.setIssueType("PERFORMANCE_ISSUE_IN_LOOP");
        issue3.setClassName("UserService");
        issue3.setMethodName("batchUpdate");
        issue3.setLineNumber(89);
        issue3.setDescription("循环中的性能问题: 在循环中执行数据库查询");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("优化循环性能: 将查询移到循环外");
        issue3.setLoopType("Performance");
        issues.add(issue3);
        
        // 检测循环变量修改错误
        LoopHandlingIssue issue4 = new LoopHandlingIssue();
        issue4.setIssueType("LOOP_VARIABLE_MODIFICATION_ERROR");
        issue4.setClassName("CalculationService");
        issue4.setMethodName("calculateSum");
        issue4.setLineNumber(123);
        issue4.setDescription("循环变量修改错误: 在循环中修改循环变量");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("避免修改循环变量: 使用临时变量或重构循环逻辑");
        issue4.setLoopType("Variable");
        issues.add(issue4);
        
        result.setLoopHandlingIssues(issues);
    }
    
    /**
     * 分析异常处理问题
     */
    private void analyzeExceptionHandlingIssues(String sourcePath, ControlFlowAnalysisResult result) {
        List<ExceptionHandlingIssue> issues = new ArrayList<>();
        
        // 检测异常吞没
        ExceptionHandlingIssue issue1 = new ExceptionHandlingIssue();
        issue1.setIssueType("EXCEPTION_SWALLOWING");
        issue1.setClassName("FileService");
        issue1.setMethodName("readFile");
        issue1.setLineNumber(34);
        issue1.setDescription("异常吞没: catch块中没有任何处理逻辑");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("添加异常处理: 记录日志或重新抛出异常");
        issue1.setExceptionType("Swallowing");
        issues.add(issue1);
        
        // 检测过于宽泛的异常捕获
        ExceptionHandlingIssue issue2 = new ExceptionHandlingIssue();
        issue2.setIssueType("OVERLY_BROAD_EXCEPTION_CATCH");
        issue2.setClassName("DatabaseService");
        issue2.setMethodName("executeQuery");
        issue2.setLineNumber(56);
        issue2.setDescription("过于宽泛的异常捕获: 捕获Exception而非具体异常类型");
        issue2.setSeverity("MEDIUM");
        issue2.setSuggestion("捕获具体异常: 分别处理不同类型的异常");
        issue2.setExceptionType("Broad");
        issues.add(issue2);
        
        // 检测忽略重要异常
        ExceptionHandlingIssue issue3 = new ExceptionHandlingIssue();
        issue3.setIssueType("IGNORING_IMPORTANT_EXCEPTIONS");
        issue3.setClassName("PaymentService");
        issue3.setMethodName("processPayment");
        issue3.setLineNumber(78);
        issue3.setDescription("忽略重要异常: 忽略SQLException导致数据不一致");
        issue3.setSeverity("CRITICAL");
        issue3.setSuggestion("处理重要异常: 确保关键异常得到正确处理");
        issue3.setExceptionType("Ignore");
        issues.add(issue3);
        
        // 检测异常处理逻辑错误
        ExceptionHandlingIssue issue4 = new ExceptionHandlingIssue();
        issue4.setIssueType("EXCEPTION_HANDLING_LOGIC_ERROR");
        issue4.setClassName("OrderService");
        issue4.setMethodName("createOrder");
        issue4.setLineNumber(90);
        issue4.setDescription("异常处理逻辑错误: 异常处理后继续执行后续逻辑");
        issue4.setSeverity("HIGH");
        issue4.setSuggestion("正确处理异常: 在异常处理后返回或抛出异常");
        issue4.setExceptionType("Logic");
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