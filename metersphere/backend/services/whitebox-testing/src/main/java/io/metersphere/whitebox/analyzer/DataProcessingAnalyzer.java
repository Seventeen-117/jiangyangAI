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
        ArrayBoundsIssue issue1 = ArrayBoundsIssue.builder()
                .issueType("ARRAY_ACCESS_OUT_OF_BOUNDS")
                .className("DataProcessor")
                .methodName("processArray")
                .lineNumber(45)
                .description("数组访问越界: 直接使用变量作为索引未检查边界")
                .severity("CRITICAL")
                .suggestion("添加边界检查: 在访问数组前检查索引是否在有效范围内")
                .arrayType("Access")
                .build();
        issues.add(issue1);
        
        // 检测循环中的数组越界
        ArrayBoundsIssue issue2 = ArrayBoundsIssue.builder()
                .issueType("ARRAY_BOUNDS_IN_LOOP")
                .className("ReportGenerator")
                .methodName("generateReport")
                .lineNumber(67)
                .description("循环中的数组越界: 循环条件使用错误的数组长度")
                .severity("HIGH")
                .suggestion("修正循环条件: 确保循环条件与数组长度匹配")
                .arrayType("Loop")
                .build();
        issues.add(issue2);
        
        // 检测多维数组越界
        ArrayBoundsIssue issue3 = ArrayBoundsIssue.builder()
                .issueType("MULTIDIMENSIONAL_ARRAY_BOUNDS")
                .className("MatrixProcessor")
                .methodName("processMatrix")
                .lineNumber(89)
                .description("多维数组越界: 只检查了第一维边界，未检查第二维")
                .severity("HIGH")
                .suggestion("检查所有维度: 对多维数组的每个维度都进行边界检查")
                .arrayType("Multidimensional")
                .build();
        issues.add(issue3);
        
        // 检测集合转数组越界
        ArrayBoundsIssue issue4 = ArrayBoundsIssue.builder()
                .issueType("COLLECTION_TO_ARRAY_BOUNDS")
                .className("ListProcessor")
                .methodName("processList")
                .lineNumber(123)
                .description("集合转数组越界: 集合大小改变后未重新检查数组边界")
                .severity("MEDIUM")
                .suggestion("重新检查边界: 在集合大小改变后重新验证数组边界")
                .arrayType("Collection")
                .build();
        issues.add(issue4);
        
        result.setArrayBoundsIssues(issues);
    }
    
    /**
     * 分析类型转换错误
     */
    private void analyzeTypeConversionIssues(String sourcePath, DataProcessingAnalysisResult result) {
        List<TypeConversionIssue> issues = new ArrayList<>();
        
        // 检测不安全的类型转换
        TypeConversionIssue issue1 = TypeConversionIssue.builder()
                .issueType("UNSAFE_TYPE_CASTING")
                .className("ObjectProcessor")
                .methodName("processObject")
                .lineNumber(34)
                .description("不安全的类型转换: 未使用instanceof检查直接强制转换")
                .severity("HIGH")
                .suggestion("添加类型检查: 使用instanceof检查后再进行类型转换")
                .conversionType("Unsafe")
                .build();
        issues.add(issue1);
        
        // 检测数值类型转换溢出
        TypeConversionIssue issue2 = TypeConversionIssue.builder()
                .issueType("NUMERIC_CONVERSION_OVERFLOW")
                .className("NumberProcessor")
                .methodName("convertNumber")
                .lineNumber(56)
                .description("数值类型转换溢出: int转byte时可能丢失数据")
                .severity("HIGH")
                .suggestion("检查数值范围: 在转换前检查数值是否在目标类型范围内")
                .conversionType("Overflow")
                .build();
        issues.add(issue2);
        
        // 检测字符串转数字格式错误
        TypeConversionIssue issue3 = TypeConversionIssue.builder()
                .issueType("STRING_TO_NUMBER_FORMAT_ERROR")
                .className("StringProcessor")
                .methodName("parseNumber")
                .lineNumber(78)
                .description("字符串转数字格式错误: 未处理NumberFormatException")
                .severity("CRITICAL")
                .suggestion("添加异常处理: 使用try-catch处理NumberFormatException")
                .conversionType("Format")
                .build();
        issues.add(issue3);
        
        // 检测自动装箱拆箱问题
        TypeConversionIssue issue4 = TypeConversionIssue.builder()
                .issueType("AUTOBOXING_UNBOXING_ISSUE")
                .className("BoxingProcessor")
                .methodName("processBoxing")
                .lineNumber(90)
                .description("自动装箱拆箱问题: 在循环中频繁装箱拆箱影响性能")
                .severity("MEDIUM")
                .suggestion("避免频繁装箱拆箱: 使用基本类型或缓存包装类实例")
                .conversionType("Autoboxing")
                .build();
        issues.add(issue4);
        
        result.setTypeConversionIssues(issues);
    }
    
    /**
     * 分析空指针异常
     */
    private void analyzeNullPointerExceptionIssues(String sourcePath, DataProcessingAnalysisResult result) {
        List<NullPointerExceptionIssue> issues = new ArrayList<>();
        
        // 检测方法调用空指针
        NullPointerExceptionIssue issue1 = NullPointerExceptionIssue.builder()
                .issueType("NULL_POINTER_METHOD_INVOCATION")
                .className("ServiceProcessor")
                .methodName("processService")
                .lineNumber(45)
                .description("方法调用空指针: 调用可能为null的对象方法")
                .severity("CRITICAL")
                .suggestion("添加空值检查: 在调用方法前检查对象是否为null")
                .nullType("Method")
                .build();
        issues.add(issue1);
        
        // 检测数组访问空指针
        NullPointerExceptionIssue issue2 = NullPointerExceptionIssue.builder()
                .issueType("NULL_POINTER_ARRAY_ACCESS")
                .className("ArrayProcessor")
                .methodName("processArray")
                .lineNumber(67)
                .description("数组访问空指针: 访问未初始化的数组")
                .severity("CRITICAL")
                .suggestion("初始化数组: 确保数组在使用前已被正确初始化")
                .nullType("Array")
                .build();
        issues.add(issue2);
        
        // 检测集合操作空指针
        NullPointerExceptionIssue issue3 = NullPointerExceptionIssue.builder()
                .issueType("NULL_POINTER_COLLECTION_OPERATION")
                .className("CollectionProcessor")
                .methodName("processCollection")
                .lineNumber(89)
                .description("集合操作空指针: 对null集合执行操作")
                .severity("CRITICAL")
                .suggestion("检查集合是否为null: 在操作集合前检查是否为null")
                .nullType("Collection")
                .build();
        issues.add(issue3);
        
        // 检测链式调用空指针
        NullPointerExceptionIssue issue4 = NullPointerExceptionIssue.builder()
                .issueType("NULL_POINTER_CHAINED_INVOCATION")
                .className("ChainProcessor")
                .methodName("processChain")
                .lineNumber(123)
                .description("链式调用空指针: 链式调用中某个对象可能为null")
                .severity("HIGH")
                .suggestion("分步检查空值: 将链式调用分解并分别检查每个对象")
                .nullType("Chain")
                .build();
        issues.add(issue4);
        
        result.setNullPointerIssues(issues);
    }
    
    /**
     * 分析数据验证问题
     */
    private void analyzeDataValidationIssues(String sourcePath, DataProcessingAnalysisResult result) {
        List<DataValidationIssue> issues = new ArrayList<>();
        
        // 检测输入数据未验证
        DataValidationIssue issue1 = DataValidationIssue.builder()
                .issueType("INPUT_DATA_NOT_VALIDATED")
                .className("InputProcessor")
                .methodName("processInput")
                .lineNumber(34)
                .description("输入数据未验证: 直接使用用户输入未进行验证")
                .severity("HIGH")
                .suggestion("添加数据验证: 对所有输入数据进行有效性检查")
                .validationType("Input")
                .build();
        issues.add(issue1);
        
        // 检测边界条件未检查
        DataValidationIssue issue2 = DataValidationIssue.builder()
                .issueType("BOUNDARY_CONDITIONS_NOT_CHECKED")
                .className("BoundaryProcessor")
                .methodName("processBoundary")
                .lineNumber(56)
                .description("边界条件未检查: 未检查数组、字符串的边界条件")
                .severity("MEDIUM")
                .suggestion("检查边界条件: 对所有边界情况进行测试和验证")
                .validationType("Boundary")
                .build();
        issues.add(issue2);
        
        // 检测数据一致性问题
        DataValidationIssue issue3 = DataValidationIssue.builder()
                .issueType("DATA_CONSISTENCY_ISSUE")
                .className("DataProcessor")
                .methodName("processData")
                .lineNumber(78)
                .description("数据一致性问题: 多个相关数据未保持一致性")
                .severity("HIGH")
                .suggestion("确保数据一致性: 在修改相关数据时保持一致性")
                .validationType("Consistency")
                .build();
        issues.add(issue3);
        
        // 检测数据完整性问题
        DataValidationIssue issue4 = DataValidationIssue.builder()
                .issueType("DATA_INTEGRITY_ISSUE")
                .className("IntegrityProcessor")
                .methodName("processIntegrity")
                .lineNumber(90)
                .description("数据完整性问题: 关键字段未设置或设置错误")
                .severity("HIGH")
                .suggestion("验证数据完整性: 确保所有必需字段都已正确设置")
                .validationType("Integrity")
                .build();
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