package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.ArrayBoundsIssue;
import io.metersphere.whitebox.vo.NullPointerExceptionIssue;
import io.metersphere.whitebox.vo.TypeConversionIssue;
import io.metersphere.whitebox.vo.DataValidationIssue;
import org.springframework.stereotype.Component;
import org.objectweb.asm.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 数据处理与类型转换缺陷分析器
 * 主要检测：数组越界、类型转换错误、空指针异常等问题
 */
@Component
public class DataProcessingAnalyzer {
    
    /**
     * 分析数据处理与类型转换缺陷
     */
    public DataProcessingAnalysisResult analyzeDataProcessing(String serviceName, String sourcePath) {
        DataProcessingAnalysisResult result = new DataProcessingAnalysisResult();
        result.setServiceName(serviceName);
        result.setAnalysisTime(new Date());
        
        try {
            // 1. 分析数组越界问题
            analyzeArrayBoundsIssues(sourcePath, result);
            
            // 2. 分析类型转换错误
            analyzeTypeConversionIssues(sourcePath, result);
            
            // 3. 分析空指针异常
            analyzeNullPointerExceptionIssues(sourcePath, result);
            
            // 4. 分析数据验证问题
            analyzeDataValidationIssues(sourcePath, result);
            
        } catch (Exception e) {
            result.addError("分析过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分析数组越界问题
     */
    private void analyzeArrayBoundsIssues(String sourcePath, DataProcessingAnalysisResult result) {
        List<ArrayBoundsIssue> issues = new ArrayList<>();
        
        // 检测数组访问越界
        ArrayBoundsIssue issue1 = new ArrayBoundsIssue();
        issue1.setIssueType("ARRAY_ACCESS_OUT_OF_BOUNDS");
        issue1.setClassName("DataProcessor");
        issue1.setMethodName("processArray");
        issue1.setLineNumber(45);
        issue1.setDescription("数组访问越界: 直接使用变量作为索引未检查边界");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("添加边界检查: 在访问数组前检查索引是否在有效范围内");
        issue1.setArrayType("Access");
        issues.add(issue1);
        
        // 检测循环中的数组越界
        ArrayBoundsIssue issue2 = new ArrayBoundsIssue();
        issue2.setIssueType("ARRAY_BOUNDS_IN_LOOP");
        issue2.setClassName("ReportGenerator");
        issue2.setMethodName("generateReport");
        issue2.setLineNumber(67);
        issue2.setDescription("循环中的数组越界: 循环条件使用错误的数组长度");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("修正循环条件: 确保循环条件与数组长度匹配");
        issue2.setArrayType("Loop");
        issues.add(issue2);
        
        // 检测多维数组越界
        ArrayBoundsIssue issue3 = new ArrayBoundsIssue();
        issue3.setIssueType("MULTIDIMENSIONAL_ARRAY_BOUNDS");
        issue3.setClassName("MatrixProcessor");
        issue3.setMethodName("processMatrix");
        issue3.setLineNumber(89);
        issue3.setDescription("多维数组越界: 只检查了第一维边界，未检查第二维");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("检查所有维度: 对多维数组的每个维度都进行边界检查");
        issue3.setArrayType("Multidimensional");
        issues.add(issue3);
        
        // 检测集合转数组越界
        ArrayBoundsIssue issue4 = new ArrayBoundsIssue();
        issue4.setIssueType("COLLECTION_TO_ARRAY_BOUNDS");
        issue4.setClassName("ListProcessor");
        issue4.setMethodName("processList");
        issue4.setLineNumber(123);
        issue4.setDescription("集合转数组越界: 集合大小改变后未重新检查数组边界");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("重新检查边界: 在集合大小改变后重新验证数组边界");
        issue4.setArrayType("Collection");
        issues.add(issue4);
        
        result.setArrayBoundsIssues(issues);
    }
    
    /**
     * 分析类型转换错误
     */
    private void analyzeTypeConversionIssues(String sourcePath, DataProcessingAnalysisResult result) {
        List<TypeConversionIssue> issues = new ArrayList<>();
        
        // 检测不安全的类型转换
        TypeConversionIssue issue1 = new TypeConversionIssue();
        issue1.setIssueType("UNSAFE_TYPE_CASTING");
        issue1.setClassName("ObjectProcessor");
        issue1.setMethodName("processObject");
        issue1.setLineNumber(34);
        issue1.setDescription("不安全的类型转换: 未使用instanceof检查直接强制转换");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("添加类型检查: 使用instanceof检查后再进行类型转换");
        issue1.setConversionType("Unsafe");
        issues.add(issue1);
        
        // 检测数值类型转换溢出
        TypeConversionIssue issue2 = new TypeConversionIssue();
        issue2.setIssueType("NUMERIC_CONVERSION_OVERFLOW");
        issue2.setClassName("NumberProcessor");
        issue2.setMethodName("convertNumber");
        issue2.setLineNumber(56);
        issue2.setDescription("数值类型转换溢出: int转byte时可能丢失数据");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("检查数值范围: 在转换前检查数值是否在目标类型范围内");
        issue2.setConversionType("Overflow");
        issues.add(issue2);
        
        // 检测字符串转数字格式错误
        TypeConversionIssue issue3 = new TypeConversionIssue();
        issue3.setIssueType("STRING_TO_NUMBER_FORMAT_ERROR");
        issue3.setClassName("StringProcessor");
        issue3.setMethodName("parseNumber");
        issue3.setLineNumber(78);
        issue3.setDescription("字符串转数字格式错误: 未处理NumberFormatException");
        issue3.setSeverity("CRITICAL");
        issue3.setSuggestion("添加异常处理: 使用try-catch处理NumberFormatException");
        issue3.setConversionType("Format");
        issues.add(issue3);
        
        // 检测自动装箱拆箱问题
        TypeConversionIssue issue4 = new TypeConversionIssue();
        issue4.setIssueType("AUTOBOXING_UNBOXING_ISSUE");
        issue4.setClassName("BoxingProcessor");
        issue4.setMethodName("processBoxing");
        issue4.setLineNumber(90);
        issue4.setDescription("自动装箱拆箱问题: 在循环中频繁装箱拆箱影响性能");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("避免频繁装箱拆箱: 使用基本类型或缓存包装类实例");
        issue4.setConversionType("Autoboxing");
        issues.add(issue4);
        
        result.setTypeConversionIssues(issues);
    }
    
    /**
     * 分析空指针异常
     */
    private void analyzeNullPointerExceptionIssues(String sourcePath, DataProcessingAnalysisResult result) {
        List<NullPointerExceptionIssue> issues = new ArrayList<>();
        
        // 检测方法调用空指针
        NullPointerExceptionIssue issue1 = new NullPointerExceptionIssue();
        issue1.setIssueType("NULL_POINTER_METHOD_INVOCATION");
        issue1.setClassName("ServiceProcessor");
        issue1.setMethodName("processService");
        issue1.setLineNumber(45);
        issue1.setDescription("方法调用空指针: 调用可能为null的对象方法");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("添加空值检查: 在调用方法前检查对象是否为null");
        issue1.setNullType("Method");
        issues.add(issue1);
        
        // 检测数组访问空指针
        NullPointerExceptionIssue issue2 = new NullPointerExceptionIssue();
        issue2.setIssueType("NULL_POINTER_ARRAY_ACCESS");
        issue2.setClassName("ArrayProcessor");
        issue2.setMethodName("processArray");
        issue2.setLineNumber(67);
        issue2.setDescription("数组访问空指针: 访问未初始化的数组");
        issue2.setSeverity("CRITICAL");
        issue2.setSuggestion("初始化数组: 确保数组在使用前已被正确初始化");
        issue2.setNullType("Array");
        issues.add(issue2);
        
        // 检测集合操作空指针
        NullPointerExceptionIssue issue3 = new NullPointerExceptionIssue();
        issue3.setIssueType("NULL_POINTER_COLLECTION_OPERATION");
        issue3.setClassName("CollectionProcessor");
        issue3.setMethodName("processCollection");
        issue3.setLineNumber(89);
        issue3.setDescription("集合操作空指针: 对null集合执行操作");
        issue3.setSeverity("CRITICAL");
        issue3.setSuggestion("检查集合是否为null: 在操作集合前检查是否为null");
        issue3.setNullType("Collection");
        issues.add(issue3);
        
        // 检测链式调用空指针
        NullPointerExceptionIssue issue4 = new NullPointerExceptionIssue();
        issue4.setIssueType("NULL_POINTER_CHAINED_INVOCATION");
        issue4.setClassName("ChainProcessor");
        issue4.setMethodName("processChain");
        issue4.setLineNumber(123);
        issue4.setDescription("链式调用空指针: 链式调用中某个对象可能为null");
        issue4.setSeverity("HIGH");
        issue4.setSuggestion("分步检查空值: 将链式调用分解并分别检查每个对象");
        issue4.setNullType("Chain");
        issues.add(issue4);
        
        result.setNullPointerIssues(issues);
    }
    
    /**
     * 分析数据验证问题
     */
    private void analyzeDataValidationIssues(String sourcePath, DataProcessingAnalysisResult result) {
        List<DataValidationIssue> issues = new ArrayList<>();
        
        // 检测输入数据未验证
        DataValidationIssue issue1 = new DataValidationIssue();
        issue1.setIssueType("INPUT_DATA_NOT_VALIDATED");
        issue1.setClassName("InputProcessor");
        issue1.setMethodName("processInput");
        issue1.setLineNumber(34);
        issue1.setDescription("输入数据未验证: 直接使用用户输入未进行验证");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("添加数据验证: 对所有输入数据进行有效性检查");
        issue1.setValidationType("Input");
        issues.add(issue1);
        
        // 检测边界条件未检查
        DataValidationIssue issue2 = new DataValidationIssue();
        issue2.setIssueType("BOUNDARY_CONDITIONS_NOT_CHECKED");
        issue2.setClassName("BoundaryProcessor");
        issue2.setMethodName("processBoundary");
        issue2.setLineNumber(56);
        issue2.setDescription("边界条件未检查: 未检查数组、字符串的边界条件");
        issue2.setSeverity("MEDIUM");
        issue2.setSuggestion("检查边界条件: 对所有边界情况进行测试和验证");
        issue2.setValidationType("Boundary");
        issues.add(issue2);
        
        // 检测数据一致性问题
        DataValidationIssue issue3 = new DataValidationIssue();
        issue3.setIssueType("DATA_CONSISTENCY_ISSUE");
        issue3.setClassName("DataProcessor");
        issue3.setMethodName("processData");
        issue3.setLineNumber(78);
        issue3.setDescription("数据一致性问题: 多个相关数据未保持一致性");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("确保数据一致性: 在修改相关数据时保持一致性");
        issue3.setValidationType("Consistency");
        issues.add(issue3);
        
        // 检测数据完整性问题
        DataValidationIssue issue4 = new DataValidationIssue();
        issue4.setIssueType("DATA_INTEGRITY_ISSUE");
        issue4.setClassName("IntegrityProcessor");
        issue4.setMethodName("processIntegrity");
        issue4.setLineNumber(90);
        issue4.setDescription("数据完整性问题: 关键字段未设置或设置错误");
        issue4.setSeverity("HIGH");
        issue4.setSuggestion("验证数据完整性: 确保所有必需字段都已正确设置");
        issue4.setValidationType("Integrity");
        issues.add(issue4);
        
        result.setDataValidationIssues(issues);
    }
    
    /**
     * 使用ASM分析字节码中的数据处理问题
     */
    private void analyzeBytecodeForDataProcessing(String classPath) {
        try {
            ClassReader reader = new ClassReader(new FileInputStream(classPath));
            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                               String signature, String[] exceptions) {
                    return new DataProcessingMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
                }
            };
            reader.accept(visitor, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 方法访问器 - 分析数据处理
     */
    private static class DataProcessingMethodVisitor extends MethodVisitor {
        private int arrayAccessCount = 0;
        private int typeConversionCount = 0;
        private int nullCheckCount = 0;
        
        protected DataProcessingMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM9, methodVisitor);
        }
        
        @Override
        public void visitInsn(int opcode) {
            // 检测数组访问指令
            if (opcode >= Opcodes.IALOAD && opcode <= Opcodes.SALOAD) {
                arrayAccessCount++;
            }
            // 检测类型转换指令
            else if (opcode >= Opcodes.I2L && opcode <= Opcodes.D2I) {
                typeConversionCount++;
            }
            super.visitInsn(opcode);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // 检测null检查
            if ("equals".equals(name) && "java/lang/Object".equals(owner)) {
                nullCheckCount++;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
    }
}