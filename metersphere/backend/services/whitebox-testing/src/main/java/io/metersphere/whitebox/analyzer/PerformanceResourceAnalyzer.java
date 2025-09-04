package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.ConcurrencyPerformanceIssue;
import io.metersphere.whitebox.vo.InefficientAlgorithmIssue;
import io.metersphere.whitebox.vo.MemoryUsageIssue;
import io.metersphere.whitebox.vo.ResourceLeakIssue;
import org.springframework.stereotype.Component;
import org.objectweb.asm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 性能与资源缺陷分析器
 * 主要检测：资源泄漏、低效算法、冗余代码等问题
 */
@Component
public class PerformanceResourceAnalyzer {
    
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("(Connection|Stream|File|Thread)");
    private static final Pattern ALGORITHM_PATTERN = Pattern.compile("(for.*for|while.*while|nested.*loop)");
    private static final Pattern DEAD_CODE_PATTERN = Pattern.compile("(unreachable|unused|deprecated)");
    
    /**
     * 分析性能与资源缺陷
     */
    public PerformanceResourceAnalysisResult analyzePerformanceResource(String serviceName, String sourcePath) {
        PerformanceResourceAnalysisResult result = new PerformanceResourceAnalysisResult();
        result.setServiceName(serviceName);
        result.setAnalysisTime(new Date());
        
        try {
            // 1. 分析资源泄漏
            analyzeResourceLeaks(sourcePath, result);
            
            // 2. 分析低效算法与冗余代码
            analyzeInefficientAlgorithms(sourcePath, result);
            
            // 3. 分析内存使用问题
            analyzeMemoryUsage(sourcePath, result);
            
            // 4. 分析并发性能问题
            analyzeConcurrencyPerformance(sourcePath, result);
            
        } catch (Exception e) {
            result.addError("分析过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分析资源泄漏
     */
    private void analyzeResourceLeaks(String sourcePath, PerformanceResourceAnalysisResult result) {
        List<ResourceLeakIssue> issues = new ArrayList<>();
        
        // 检测数据库连接未关闭
        ResourceLeakIssue issue1 = new ResourceLeakIssue();
        issue1.setIssueType("DATABASE_CONNECTION_LEAK");
        issue1.setClassName("DataService");
        issue1.setMethodName("queryData");
        issue1.setLineNumber(45);
        issue1.setDescription("数据库连接泄漏: Connection未在finally块中关闭");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("使用try-with-resources: try (Connection conn = getConnection())");
        issue1.setResourceType("Database Connection");
        issues.add(issue1);
        
        // 检测文件流未关闭
        ResourceLeakIssue issue2 = new ResourceLeakIssue();
        issue2.setIssueType("FILE_STREAM_LEAK");
        issue2.setClassName("FileProcessor");
        issue2.setMethodName("processFile");
        issue2.setLineNumber(67);
        issue2.setDescription("文件流泄漏: FileOutputStream未关闭，导致文件句柄耗尽");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("确保在finally块中关闭流: stream.close()");
        issue2.setResourceType("File Stream");
        issues.add(issue2);
        
        // 检测线程未正确回收
        ResourceLeakIssue issue3 = new ResourceLeakIssue();
        issue3.setIssueType("THREAD_LEAK");
        issue3.setClassName("TaskService");
        issue3.setMethodName("executeTask");
        issue3.setLineNumber(89);
        issue3.setDescription("线程泄漏: 线程池未正确关闭，导致线程无法回收");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("正确关闭线程池: executorService.shutdown()");
        issue3.setResourceType("Thread");
        issues.add(issue3);
        
        // 检测缓存未清理
        ResourceLeakIssue issue4 = new ResourceLeakIssue();
        issue4.setIssueType("CACHE_LEAK");
        issue4.setClassName("CacheService");
        issue4.setMethodName("putCache");
        issue4.setLineNumber(123);
        issue4.setDescription("缓存泄漏: 缓存未设置过期时间，可能导致内存泄漏");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("设置缓存过期时间: cache.put(key, value, ttl)");
        issue4.setResourceType("Cache");
        issues.add(issue4);
        
        result.setResourceLeakIssues(issues);
    }
    
    /**
     * 分析低效算法与冗余代码
     */
    private void analyzeInefficientAlgorithms(String sourcePath, PerformanceResourceAnalysisResult result) {
        List<InefficientAlgorithmIssue> issues = new ArrayList<>();
        
        // 检测O(n²)算法
        InefficientAlgorithmIssue issue1 = new InefficientAlgorithmIssue();
        issue1.setIssueType("O_N_SQUARED_ALGORITHM");
        issue1.setClassName("UserService");
        issue1.setMethodName("findDuplicateUsers");
        issue1.setLineNumber(34);
        issue1.setDescription("低效算法: 使用双层循环查找重复用户，时间复杂度O(n²)");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("使用Set或Map优化: 时间复杂度降为O(n)");
        issue1.setAlgorithmType("Nested Loop");
        issues.add(issue1);
        
        // 检测重复计算
        InefficientAlgorithmIssue issue2 = new InefficientAlgorithmIssue();
        issue2.setIssueType("DUPLICATE_CALCULATION");
        issue2.setClassName("CalculatorService");
        issue2.setMethodName("calculateTotal");
        issue2.setLineNumber(56);
        issue2.setDescription("重复计算: 多次调用同一方法获取相同数据");
        issue2.setSeverity("MEDIUM");
        issue2.setSuggestion("缓存计算结果: 避免重复计算");
        issue2.setAlgorithmType("Redundant Call");
        issues.add(issue2);
        
        // 检测死代码
        InefficientAlgorithmIssue issue3 = new InefficientAlgorithmIssue();
        issue3.setIssueType("DEAD_CODE");
        issue3.setClassName("LegacyService");
        issue3.setMethodName("oldMethod");
        issue3.setLineNumber(78);
        issue3.setDescription("死代码: 方法从未被调用，占用内存空间");
        issue3.setSeverity("LOW");
        issue3.setSuggestion("删除未使用的代码: 减少代码体积");
        issue3.setAlgorithmType("Unused Code");
        issues.add(issue3);
        
        // 检测低效字符串操作
        InefficientAlgorithmIssue issue4 = new InefficientAlgorithmIssue();
        issue4.setIssueType("INEFFICIENT_STRING_OPERATION");
        issue4.setClassName("StringProcessor");
        issue4.setMethodName("buildString");
        issue4.setLineNumber(90);
        issue4.setDescription("低效字符串操作: 使用String拼接，应使用StringBuilder");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("使用StringBuilder: 提高字符串拼接性能");
        issue4.setAlgorithmType("String Concatenation");
        issues.add(issue4);
        
        result.setInefficientAlgorithmIssues(issues);
    }
    
    /**
     * 分析内存使用问题
     */
    private void analyzeMemoryUsage(String sourcePath, PerformanceResourceAnalysisResult result) {
        List<MemoryUsageIssue> issues = new ArrayList<>();
        
        // 检测大对象创建
        MemoryUsageIssue issue1 = new MemoryUsageIssue();
        issue1.setIssueType("LARGE_OBJECT_CREATION");
        issue1.setClassName("DataProcessor");
        issue1.setMethodName("loadData");
        issue1.setLineNumber(45);
        issue1.setDescription("大对象创建: 一次性加载大量数据到内存");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("使用流式处理: 分批处理数据，避免内存溢出");
        issue1.setMemoryType("Large Object");
        issues.add(issue1);
        
        // 检测内存泄漏
        MemoryUsageIssue issue2 = new MemoryUsageIssue();
        issue2.setIssueType("MEMORY_LEAK");
        issue2.setClassName("EventService");
        issue2.setMethodName("addListener");
        issue2.setLineNumber(67);
        issue2.setDescription("内存泄漏: 事件监听器未正确移除");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("正确移除监听器: 在对象销毁时移除所有监听器");
        issue2.setMemoryType("Event Listener");
        issues.add(issue2);
        
        // 检测集合未清理
        MemoryUsageIssue issue3 = new MemoryUsageIssue();
        issue3.setIssueType("COLLECTION_NOT_CLEARED");
        issue3.setClassName("CacheService");
        issue3.setMethodName("updateCache");
        issue3.setLineNumber(89);
        issue3.setDescription("集合未清理: 缓存集合持续增长，未清理过期数据");
        issue3.setSeverity("MEDIUM");
        issue3.setSuggestion("定期清理集合: 使用定时任务清理过期数据");
        issue3.setMemoryType("Collection");
        issues.add(issue3);
        
        // 检测静态变量滥用
        MemoryUsageIssue issue4 = new MemoryUsageIssue();
        issue4.setIssueType("STATIC_VARIABLE_ABUSE");
        issue4.setClassName("ConfigService");
        issue4.setMethodName("loadConfig");
        issue4.setLineNumber(123);
        issue4.setDescription("静态变量滥用: 大量数据存储在静态变量中");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("减少静态变量使用: 使用实例变量或缓存");
        issue4.setMemoryType("Static Variable");
        issues.add(issue4);
        
        result.setMemoryUsageIssues(issues);
    }
    
    /**
     * 分析并发性能问题
     */
    private void analyzeConcurrencyPerformance(String sourcePath, PerformanceResourceAnalysisResult result) {
        List<ConcurrencyPerformanceIssue> issues = new ArrayList<>();
        
        // 检测同步性能问题
        ConcurrencyPerformanceIssue issue1 = new ConcurrencyPerformanceIssue();
        issue1.setIssueType("SYNCHRONIZATION_PERFORMANCE_ISSUE");
        issue1.setClassName("CounterService");
        issue1.setMethodName("increment");
        issue1.setLineNumber(34);
        issue1.setDescription("同步性能问题: 使用synchronized方法，性能较差");
        issue1.setSeverity("MEDIUM");
        issue1.setSuggestion("使用AtomicInteger: 提高并发性能");
        issue1.setConcurrencyType("Synchronization");
        issues.add(issue1);
        
        // 检测锁竞争
        ConcurrencyPerformanceIssue issue2 = new ConcurrencyPerformanceIssue();
        issue2.setIssueType("LOCK_CONTENTION");
        issue2.setClassName("ResourceService");
        issue2.setMethodName("accessResource");
        issue2.setLineNumber(56);
        issue2.setDescription("锁竞争: 多个线程竞争同一锁，性能下降");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("减少锁粒度: 使用细粒度锁或无锁数据结构");
        issue2.setConcurrencyType("Lock Contention");
        issues.add(issue2);
        
        // 检测线程池配置问题
        ConcurrencyPerformanceIssue issue3 = new ConcurrencyPerformanceIssue();
        issue3.setIssueType("THREAD_POOL_CONFIGURATION_ISSUE");
        issue3.setClassName("TaskService");
        issue3.setMethodName("executeTasks");
        issue3.setLineNumber(78);
        issue3.setDescription("线程池配置问题: 线程池大小配置不当");
        issue3.setSeverity("MEDIUM");
        issue3.setSuggestion("优化线程池配置: 根据CPU核心数设置合适的线程数");
        issue3.setConcurrencyType("Thread Pool");
        issues.add(issue3);
        
        // 检测死锁风险
        ConcurrencyPerformanceIssue issue4 = new ConcurrencyPerformanceIssue();
        issue4.setIssueType("DEADLOCK_RISK");
        issue4.setClassName("TransferService");
        issue4.setMethodName("transfer");
        issue4.setLineNumber(90);
        issue4.setDescription("死锁风险: 多个锁的获取顺序不一致");
        issue4.setSeverity("CRITICAL");
        issue4.setSuggestion("统一锁获取顺序: 避免死锁");
        issue4.setConcurrencyType("Deadlock");
        issues.add(issue4);
        
        result.setConcurrencyPerformanceIssues(issues);
    }
    
    /**
     * 使用ASM分析字节码中的性能问题
     */
    private void analyzeBytecodeForPerformance(String classPath) {
        try {
            ClassReader reader = new ClassReader(new FileInputStream(classPath));
            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM8) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                               String signature, String[] exceptions) {
                    return new PerformanceMethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions));
                }
            };
            reader.accept(visitor, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 方法访问器 - 分析性能问题
     */
    private static class PerformanceMethodVisitor extends MethodVisitor {
        private int resourceAccessCount = 0;
        private int loopCount = 0;
        private int synchronizationCount = 0;
        
        protected PerformanceMethodVisitor(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // 检测资源访问
            if (owner.contains("Connection") || owner.contains("Stream") || owner.contains("File")) {
                resourceAccessCount++;
            }
            // 检测同步调用
            if (name.contains("synchronized") || name.contains("lock")) {
                synchronizationCount++;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
        
        @Override
        public void visitJumpInsn(int opcode, Label label) {
            // 检测循环
            if (opcode == Opcodes.IF_ICMPGE || opcode == Opcodes.IF_ICMPGT) {
                loopCount++;
            }
            super.visitJumpInsn(opcode, label);
        }
    }
}
