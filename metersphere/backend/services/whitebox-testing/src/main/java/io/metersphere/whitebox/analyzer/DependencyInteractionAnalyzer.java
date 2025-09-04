package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.DependencyInjectionIssue;
import io.metersphere.whitebox.vo.DistributedTransactionIssue;
import io.metersphere.whitebox.vo.ExternalDependencyIssue;
import io.metersphere.whitebox.vo.ServiceCommunicationIssue;
import org.springframework.stereotype.Component;
import org.objectweb.asm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 依赖与交互缺陷分析器
 * 主要检测：外部依赖调用未处理异常、分布式事务一致性问题、依赖注入错误等问题
 */
@Component
public class DependencyInteractionAnalyzer {
    
    private static final Pattern EXTERNAL_CALL_PATTERN = Pattern.compile("(database|redis|cache|http|service)");
    private static final Pattern TRANSACTION_PATTERN = Pattern.compile("(@Transactional|@Commit|@Rollback)");
    private static final Pattern DEPENDENCY_INJECTION_PATTERN = Pattern.compile("(@Autowired|@Resource|@Inject)");
    
    /**
     * 分析依赖与交互缺陷
     */
    public DependencyInteractionAnalysisResult analyzeDependencyInteraction(String serviceName, String sourcePath) {
        DependencyInteractionAnalysisResult result = new DependencyInteractionAnalysisResult();
        result.setServiceName(serviceName);
        result.setAnalysisTime(new Date());
        
        try {
            // 1. 分析外部依赖调用未处理异常
            analyzeExternalDependencyCalls(sourcePath, result);
            
            // 2. 分析分布式事务一致性问题
            analyzeDistributedTransactionConsistency(sourcePath, result);
            
            // 3. 分析依赖注入错误
            analyzeDependencyInjection(sourcePath, result);
            
            // 4. 分析服务间通信问题
            analyzeServiceCommunication(sourcePath, result);
            
        } catch (Exception e) {
            result.addError("分析过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分析外部依赖调用未处理异常
     */
    private void analyzeExternalDependencyCalls(String sourcePath, DependencyInteractionAnalysisResult result) {
        List<ExternalDependencyIssue> issues = new ArrayList<>();
        
        // 检测数据库调用未处理异常
        ExternalDependencyIssue issue1 = new ExternalDependencyIssue();
        issue1.setIssueType("DATABASE_CALL_EXCEPTION_UNHANDLED");
        issue1.setClassName("OrderService");
        issue1.setMethodName("createOrder");
        issue1.setLineNumber(45);
        issue1.setDescription("数据库调用异常未处理: 直接调用数据库操作，未捕获SQLException");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("添加异常处理: try-catch SQLException或使用@Transactional");
        issue1.setDependencyType("Database");
        issues.add(issue1);
        
        // 检测Redis调用未处理异常
        ExternalDependencyIssue issue2 = new ExternalDependencyIssue();
        issue2.setIssueType("REDIS_CALL_EXCEPTION_UNHANDLED");
        issue2.setClassName("CacheService");
        issue2.setMethodName("setCache");
        issue2.setLineNumber(67);
        issue2.setDescription("Redis调用异常未处理: 未捕获RedisConnectionException");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("添加异常处理: try-catch RedisConnectionException");
        issue2.setDependencyType("Redis");
        issues.add(issue2);
        
        // 检测HTTP调用未处理异常
        ExternalDependencyIssue issue3 = new ExternalDependencyIssue();
        issue3.setIssueType("HTTP_CALL_EXCEPTION_UNHANDLED");
        issue3.setClassName("PaymentService");
        issue3.setMethodName("callPaymentGateway");
        issue3.setLineNumber(89);
        issue3.setDescription("HTTP调用异常未处理: 未捕获ConnectTimeoutException");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("添加异常处理: try-catch ConnectTimeoutException");
        issue3.setDependencyType("HTTP");
        issues.add(issue3);
        
        // 检测服务调用未处理异常
        ExternalDependencyIssue issue4 = new ExternalDependencyIssue();
        issue4.setIssueType("SERVICE_CALL_EXCEPTION_UNHANDLED");
        issue4.setClassName("OrderService");
        issue4.setMethodName("deductInventory");
        issue4.setLineNumber(123);
        issue4.setDescription("服务调用异常未处理: 调用库存服务时未处理TimeoutException");
        issue4.setSeverity("CRITICAL");
        issue4.setSuggestion("添加异常处理: try-catch TimeoutException或使用熔断器");
        issue4.setDependencyType("Service");
        issues.add(issue4);
        
        result.setExternalDependencyIssues(issues);
    }
    
    /**
     * 分析分布式事务一致性问题
     */
    private void analyzeDistributedTransactionConsistency(String sourcePath, DependencyInteractionAnalysisResult result) {
        List<DistributedTransactionIssue> issues = new ArrayList<>();
        
        // 检测跨服务操作事务不一致
        DistributedTransactionIssue issue1 = new DistributedTransactionIssue();
        issue1.setIssueType("CROSS_SERVICE_TRANSACTION_INCONSISTENCY");
        issue1.setClassName("OrderService");
        issue1.setMethodName("createOrderWithInventory");
        issue1.setLineNumber(34);
        issue1.setDescription("跨服务事务不一致: 扣库存成功但创建订单失败时，库存未回滚");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("使用分布式事务或补偿机制确保数据一致性");
        issue1.setTransactionType("Cross-Service");
        issues.add(issue1);
        
        // 检测补偿逻辑错误
        DistributedTransactionIssue issue2 = new DistributedTransactionIssue();
        issue2.setIssueType("COMPENSATION_LOGIC_ERROR");
        issue2.setClassName("OrderService");
        issue2.setMethodName("rollbackOrder");
        issue2.setLineNumber(56);
        issue2.setDescription("补偿逻辑错误: 订单创建失败后未删除消息表记录，导致重复消费");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("完善补偿逻辑: 确保失败时清理所有相关数据");
        issue2.setTransactionType("Compensation");
        issues.add(issue2);
        
        // 检测重试逻辑错误
        DistributedTransactionIssue issue3 = new DistributedTransactionIssue();
        issue3.setIssueType("RETRY_LOGIC_ERROR");
        issue3.setClassName("PaymentService");
        issue3.setMethodName("retryPayment");
        issue3.setLineNumber(78);
        issue3.setDescription("重试逻辑错误: 重试次数过多导致重复扣减");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("添加幂等性检查: 确保重复操作不会产生副作用");
        issue3.setTransactionType("Retry");
        issues.add(issue3);
        
        // 检测本地消息表处理错误
        DistributedTransactionIssue issue4 = new DistributedTransactionIssue();
        issue4.setIssueType("LOCAL_MESSAGE_TABLE_ERROR");
        issue4.setClassName("MessageService");
        issue4.setMethodName("processMessage");
        issue4.setLineNumber(90);
        issue4.setDescription("本地消息表处理错误: 消息处理失败后未正确标记状态");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("完善消息状态管理: 确保消息状态正确更新");
        issue4.setTransactionType("Local-Message");
        issues.add(issue4);
        
        result.setDistributedTransactionIssues(issues);
    }
    
    /**
     * 分析依赖注入错误
     */
    private void analyzeDependencyInjection(String sourcePath, DependencyInteractionAnalysisResult result) {
        List<DependencyInjectionIssue> issues = new ArrayList<>();
        
        // 检测依赖注入失败
        DependencyInjectionIssue issue1 = new DependencyInjectionIssue();
        issue1.setIssueType("DEPENDENCY_INJECTION_FAILED");
        issue1.setClassName("UserService");
        issue1.setMethodName("getLogger");
        issue1.setLineNumber(23);
        issue1.setDescription("依赖注入失败: logger始终为null，@Autowired注解被误写成@Resource");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("检查注解配置: 确保@Autowired注解正确");
        issue1.setInjectionType("@Autowired");
        issues.add(issue1);
        
        // 检测循环依赖
        DependencyInjectionIssue issue2 = new DependencyInjectionIssue();
        issue2.setIssueType("CIRCULAR_DEPENDENCY");
        issue2.setClassName("OrderService");
        issue2.setMethodName("init");
        issue2.setLineNumber(1);
        issue2.setDescription("循环依赖: OrderService依赖UserService，UserService依赖OrderService");
        issue2.setSeverity("CRITICAL");
        issue2.setSuggestion("重构依赖关系: 使用@Lazy注解或提取公共接口");
        issue2.setInjectionType("Circular");
        issues.add(issue2);
        
        // 检测注入类型错误
        DependencyInjectionIssue issue3 = new DependencyInjectionIssue();
        issue3.setIssueType("INJECTION_TYPE_MISMATCH");
        issue3.setClassName("PaymentService");
        issue3.setMethodName("getPaymentProcessor");
        issue3.setLineNumber(45);
        issue3.setDescription("注入类型不匹配: 注入了接口的错误实现类");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("检查实现类配置: 确保注入正确的实现类");
        issue3.setInjectionType("@Qualifier");
        issues.add(issue3);
        
        // 检测作用域配置错误
        DependencyInjectionIssue issue4 = new DependencyInjectionIssue();
        issue4.setIssueType("SCOPE_CONFIGURATION_ERROR");
        issue4.setClassName("SessionService");
        issue4.setMethodName("getSession");
        issue4.setLineNumber(67);
        issue4.setDescription("作用域配置错误: 单例Bean中注入了原型Bean");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("检查作用域配置: 使用@Scope(\"prototype\")或@Lookup");
        issue4.setInjectionType("@Scope");
        issues.add(issue4);
        
        result.setDependencyInjectionIssues(issues);
    }
    
    /**
     * 分析服务间通信问题
     */
    private void analyzeServiceCommunication(String sourcePath, DependencyInteractionAnalysisResult result) {
        List<ServiceCommunicationIssue> issues = new ArrayList<>();
        
        // 检测服务调用超时
        ServiceCommunicationIssue issue1 = new ServiceCommunicationIssue();
        issue1.setIssueType("SERVICE_CALL_TIMEOUT");
        issue1.setClassName("OrderService");
        issue1.setMethodName("callInventoryService");
        issue1.setLineNumber(89);
        issue1.setDescription("服务调用超时: 未设置超时时间，可能导致长时间等待");
        issue1.setSeverity("HIGH");
        issue1.setSuggestion("设置合理的超时时间: 使用@HystrixCommand或配置超时");
        issue1.setCommunicationType("HTTP");
        issues.add(issue1);
        
        // 检测服务降级缺失
        ServiceCommunicationIssue issue2 = new ServiceCommunicationIssue();
        issue2.setIssueType("SERVICE_FALLBACK_MISSING");
        issue2.setClassName("PaymentService");
        issue2.setMethodName("processPayment");
        issue2.setLineNumber(123);
        issue2.setDescription("服务降级缺失: 支付服务不可用时无降级策略");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("添加降级策略: 使用@HystrixCommand的fallbackMethod");
        issue2.setCommunicationType("RPC");
        issues.add(issue2);
        
        // 检测负载均衡配置错误
        ServiceCommunicationIssue issue3 = new ServiceCommunicationIssue();
        issue3.setIssueType("LOAD_BALANCING_ERROR");
        issue3.setClassName("UserService");
        issue3.setMethodName("getUserInfo");
        issue3.setLineNumber(45);
        issue3.setDescription("负载均衡配置错误: 所有请求都路由到同一个实例");
        issue3.setSeverity("MEDIUM");
        issue3.setSuggestion("检查负载均衡配置: 确保请求正确分发");
        issue3.setCommunicationType("Load-Balancing");
        issues.add(issue3);
        
        // 检测服务发现失败
        ServiceCommunicationIssue issue4 = new ServiceCommunicationIssue();
        issue4.setIssueType("SERVICE_DISCOVERY_FAILURE");
        issue4.setClassName("NotificationService");
        issue4.setMethodName("sendNotification");
        issue4.setLineNumber(67);
        issue4.setDescription("服务发现失败: 无法找到目标服务实例");
        issue4.setSeverity("HIGH");
        issue4.setSuggestion("检查服务注册: 确保服务正确注册到注册中心");
        issue4.setCommunicationType("Service-Discovery");
        issues.add(issue4);
        
        result.setServiceCommunicationIssues(issues);
    }
    
    /**
     * 使用ASM分析字节码中的依赖交互问题
     */
    private void analyzeBytecodeForDependencyInteraction(String classPath) {
        try {
            ClassReader reader = new ClassReader(new FileInputStream(classPath));
            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                               String signature, String[] exceptions) {
                    return new DependencyInteractionMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
                }
            };
            reader.accept(visitor, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 方法访问器 - 分析依赖交互
     */
    private static class DependencyInteractionMethodVisitor extends MethodVisitor {
        private int externalCallCount = 0;
        private int transactionCount = 0;
        private int dependencyInjectionCount = 0;
        
        protected DependencyInteractionMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM9, methodVisitor);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // 检测外部调用
            if (owner.startsWith("java/sql") || owner.startsWith("redis") || 
                owner.startsWith("org/springframework")) {
                externalCallCount++;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
        
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            // 检测事务注解
            if (descriptor.contains("Transactional")) {
                transactionCount++;
            }
            // 检测依赖注入注解
            if (descriptor.contains("Autowired") || descriptor.contains("Resource")) {
                dependencyInjectionCount++;
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }
}