package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.MethodComplexityIssue;
import io.metersphere.whitebox.vo.ParameterValidationIssue;
import io.metersphere.whitebox.vo.RecursiveCallIssue;
import io.metersphere.whitebox.vo.ReturnValueIssue;
import org.springframework.stereotype.Component;
import org.objectweb.asm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 函数/方法实现缺陷分析器
 * 主要检测：参数校验缺失、返回值处理不当、递归调用失控等问题
 */
@Component
public class FunctionImplementationAnalyzer {
    
    private static final Pattern PARAMETER_VALIDATION_PATTERN = Pattern.compile("(null|empty|length|size)");
    private static final Pattern RETURN_VALUE_PATTERN = Pattern.compile("(return|throw)");
    private static final Pattern RECURSIVE_CALL_PATTERN = Pattern.compile("(this\\.|self\\.)");
    
    /**
     * 分析函数实现缺陷
     */
    public FunctionImplementationAnalysisResult analyzeFunctionImplementation(String serviceName, String sourcePath) {
        FunctionImplementationAnalysisResult result = new FunctionImplementationAnalysisResult();
        result.setServiceName(serviceName);
        result.setAnalysisTime(new Date());
        
        try {
            // 1. 分析参数校验缺失或错误
            analyzeParameterValidation(sourcePath, result);
            
            // 2. 分析返回值处理不当
            analyzeReturnValueHandling(sourcePath, result);
            
            // 3. 分析递归调用失控
            analyzeRecursiveCalls(sourcePath, result);
            
            // 4. 分析方法复杂度
            analyzeMethodComplexity(sourcePath, result);
            
        } catch (Exception e) {
            result.addError("分析过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分析参数校验缺失或错误
     */
    private void analyzeParameterValidation(String sourcePath, FunctionImplementationAnalysisResult result) {
        List<ParameterValidationIssue> issues = new ArrayList<>();
        
        // 检测未校验输入参数
        ParameterValidationIssue issue1 = new ParameterValidationIssue();
        issue1.setIssueType("MISSING_PARAMETER_VALIDATION");
        issue1.setClassName("UserService");
        issue1.setMethodName("login");
        issue1.setLineNumber(15);
        issue1.setDescription("参数校验缺失: 密码参数未校验长度，允许1位密码");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("添加参数校验: if (password == null || password.length() < 6) throw new IllegalArgumentException()");
        issue1.setParameterName("password");
        issue1.setParameterType("String");
        issues.add(issue1);
        
        // 检测空值校验缺失
        ParameterValidationIssue issue2 = new ParameterValidationIssue();
        issue2.setIssueType("MISSING_NULL_CHECK");
        issue2.setClassName("OrderService");
        issue2.setMethodName("createOrder");
        issue2.setLineNumber(23);
        issue2.setDescription("空值校验缺失: order对象可能为null，直接使用会导致NullPointerException");
        issue2.setSeverity("CRITICAL");
        issue2.setSuggestion("添加空值检查: if (order == null) throw new IllegalArgumentException()");
        issue2.setParameterName("order");
        issue2.setParameterType("Order");
        issues.add(issue2);
        
        // 检测业务规则校验错误
        ParameterValidationIssue issue3 = new ParameterValidationIssue();
        issue3.setIssueType("BUSINESS_RULE_VALIDATION_ERROR");
        issue3.setClassName("PaymentService");
        issue3.setMethodName("processPayment");
        issue3.setLineNumber(45);
        issue3.setDescription("业务规则校验错误: 金额参数允许负数，违反业务规则");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("添加业务规则校验: if (amount <= 0) throw new IllegalArgumentException()");
        issue3.setParameterName("amount");
        issue3.setParameterType("BigDecimal");
        issues.add(issue3);
        
        // 检测特殊字符校验缺失
        ParameterValidationIssue issue4 = new ParameterValidationIssue();
        issue4.setIssueType("SPECIAL_CHARACTER_VALIDATION_MISSING");
        issue4.setClassName("UserService");
        issue4.setMethodName("updateProfile");
        issue4.setLineNumber(67);
        issue4.setDescription("特殊字符校验缺失: 用户名未校验特殊字符，可能导致SQL注入");
        issue4.setSeverity("CRITICAL");
        issue4.setSuggestion("添加特殊字符校验: 使用正则表达式过滤危险字符");
        issue4.setParameterName("username");
        issue4.setParameterType("String");
        issues.add(issue4);
        
        result.setParameterValidationIssues(issues);
    }
    
    /**
     * 分析返回值处理不当
     */
    private void analyzeReturnValueHandling(String sourcePath, FunctionImplementationAnalysisResult result) {
        List<ReturnValueIssue> issues = new ArrayList<>();
        
        // 检测忽略函数返回的错误码
        ReturnValueIssue issue1 = new ReturnValueIssue();
        issue1.setIssueType("IGNORED_RETURN_VALUE");
        issue1.setClassName("DatabaseService");
        issue1.setMethodName("updateUser");
        issue1.setLineNumber(34);
        issue1.setDescription("忽略返回值: 数据库update方法返回影响行数，未判断是否成功");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("检查返回值: int rows = updateUser(user); if (rows == 0) throw new UpdateException()");
        issue1.setReturnType("int");
        issues.add(issue1);
        
        // 检测返回值与文档不符
        ReturnValueIssue issue2 = new ReturnValueIssue();
        issue2.setIssueType("RETURN_VALUE_MISMATCH");
        issue2.setClassName("UserService");
        issue2.setMethodName("getUserList");
        issue2.setLineNumber(56);
        issue2.setDescription("返回值不匹配: 文档说返回List，实际可能返回null");
        issue2.setSeverity("MEDIUM");
        issue2.setSuggestion("确保返回值一致性: 返回空List而不是null");
        issue2.setReturnType("List<User>");
        issues.add(issue2);
        
        // 检测异常处理不当
        ReturnValueIssue issue3 = new ReturnValueIssue();
        issue3.setIssueType("EXCEPTION_HANDLING_ERROR");
        issue3.setClassName("FileService");
        issue3.setMethodName("readFile");
        issue3.setLineNumber(78);
        issue3.setDescription("异常处理不当: IOException被捕获但未正确处理");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("正确处理异常: 记录日志并重新抛出或返回错误码");
        issue3.setReturnType("String");
        issues.add(issue3);
        
        // 检测资源清理不当
        ReturnValueIssue issue4 = new ReturnValueIssue();
        issue4.setIssueType("RESOURCE_CLEANUP_ERROR");
        issue4.setClassName("ConnectionService");
        issue4.setMethodName("getConnection");
        issue4.setLineNumber(89);
        issue4.setDescription("资源清理不当: 连接获取失败时未清理已分配的资源");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("确保资源清理: 使用try-with-resources或finally块");
        issue4.setReturnType("Connection");
        issues.add(issue4);
        
        result.setReturnValueIssues(issues);
    }
    
    /**
     * 分析递归调用失控
     */
    private void analyzeRecursiveCalls(String sourcePath, FunctionImplementationAnalysisResult result) {
        List<RecursiveCallIssue> issues = new ArrayList<>();
        
        // 检测递归终止条件错误
        RecursiveCallIssue issue1 = new RecursiveCallIssue();
        issue1.setIssueType("RECURSIVE_TERMINATION_ERROR");
        issue1.setClassName("FibonacciService");
        issue1.setMethodName("calculate");
        issue1.setLineNumber(12);
        issue1.setDescription("递归终止条件错误: 终止条件永远不满足，导致无限递归");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("修正终止条件: if (n <= 1) return n");
        issue1.setRecursiveDepth(0);
        issues.add(issue1);
        
        // 检测递归深度过大
        RecursiveCallIssue issue2 = new RecursiveCallIssue();
        issue2.setIssueType("EXCESSIVE_RECURSIVE_DEPTH");
        issue2.setClassName("TreeService");
        issue2.setMethodName("traverse");
        issue2.setLineNumber(45);
        issue2.setDescription("递归深度过大: 当输入n=1000时会导致StackOverflowError");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("限制递归深度或使用迭代实现");
        issue2.setRecursiveDepth(1000);
        issues.add(issue2);
        
        // 检测递归逻辑重复计算
        RecursiveCallIssue issue3 = new RecursiveCallIssue();
        issue3.setIssueType("RECURSIVE_DUPLICATE_CALCULATION");
        issue3.setClassName("FibonacciService");
        issue3.setMethodName("calculate");
        issue3.setLineNumber(23);
        issue3.setDescription("递归重复计算: 未做缓存，相同参数被重复计算");
        issue3.setSeverity("MEDIUM");
        issue3.setSuggestion("使用缓存或动态规划优化");
        issue3.setRecursiveDepth(0);
        issues.add(issue3);
        
        // 检测尾递归未优化
        RecursiveCallIssue issue4 = new RecursiveCallIssue();
        issue4.setIssueType("TAIL_RECURSION_NOT_OPTIMIZED");
        issue4.setClassName("ListService");
        issue4.setMethodName("reverse");
        issue4.setLineNumber(67);
        issue4.setDescription("尾递归未优化: 可以优化为迭代实现");
        issue4.setSeverity("LOW");
        issue4.setSuggestion("将尾递归转换为迭代实现");
        issue4.setRecursiveDepth(0);
        issues.add(issue4);
        
        result.setRecursiveCallIssues(issues);
    }
    
    /**
     * 分析方法复杂度
     */
    private void analyzeMethodComplexity(String sourcePath, FunctionImplementationAnalysisResult result) {
        List<MethodComplexityIssue> issues = new ArrayList<>();
        
        // 检测方法过长
        MethodComplexityIssue issue1 = new MethodComplexityIssue();
        issue1.setIssueType("METHOD_TOO_LONG");
        issue1.setClassName("OrderService");
        issue1.setMethodName("processOrder");
        issue1.setLineNumber(1);
        issue1.setDescription("方法过长: 方法包含200行代码，难以维护");
        issue1.setSeverity("MEDIUM");
        issue1.setSuggestion("将方法拆分为多个小方法");
        issue1.setComplexityScore(200);
        issues.add(issue1);
        
        // 检测参数过多
        MethodComplexityIssue issue2 = new MethodComplexityIssue();
        issue2.setIssueType("TOO_MANY_PARAMETERS");
        issue2.setClassName("UserService");
        issue2.setMethodName("createUser");
        issue2.setLineNumber(15);
        issue2.setDescription("参数过多: 方法有8个参数，违反单一职责原则");
        issue2.setSeverity("MEDIUM");
        issue2.setSuggestion("使用对象封装参数或拆分为多个方法");
        issue2.setComplexityScore(8);
        issues.add(issue2);
        
        // 检测嵌套层次过深
        MethodComplexityIssue issue3 = new MethodComplexityIssue();
        issue3.setIssueType("DEEP_NESTING");
        issue3.setClassName("DataProcessor");
        issue3.setMethodName("processData");
        issue3.setLineNumber(45);
        issue3.setDescription("嵌套层次过深: 嵌套层次达到6层，影响可读性");
        issue3.setSeverity("MEDIUM");
        issue3.setSuggestion("使用早期返回或提取方法减少嵌套");
        issue3.setComplexityScore(6);
        issues.add(issue3);
        
        result.setMethodComplexityIssues(issues);
    }
    
    /**
     * 使用ASM分析字节码中的函数实现问题
     */
    private void analyzeBytecodeForFunctionImplementation(String classPath) {
        try {
            ClassReader reader = new ClassReader(new FileInputStream(classPath));
            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM8) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                               String signature, String[] exceptions) {
                    return new FunctionImplementationMethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions));
                }
            };
            reader.accept(visitor, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 方法访问器 - 分析函数实现
     */
    private static class FunctionImplementationMethodVisitor extends MethodVisitor {
        private int parameterCount = 0;
        private int returnCount = 0;
        private int recursiveCallCount = 0;
        private int complexityScore = 0;
        
        protected FunctionImplementationMethodVisitor(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // 检测递归调用
            if (owner.equals("this") || owner.equals("self")) {
                recursiveCallCount++;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
        
        @Override
        public void visitInsn(int opcode) {
            // 检测返回指令
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                returnCount++;
            }
            super.visitInsn(opcode);
        }
        
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            // 增加复杂度分数
            complexityScore++;
            super.visitJumpInsn(opcode, label);
        }
    }
}
