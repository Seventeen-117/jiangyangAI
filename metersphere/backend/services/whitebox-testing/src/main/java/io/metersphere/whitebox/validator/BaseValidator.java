package io.metersphere.whitebox.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.util.function.Supplier;

/**
 * 基础验证器类
 * 包含所有验证器共用的字段和方法
 */
@Component
public abstract class BaseValidator {
    
    @Autowired
    protected ApplicationContext applicationContext;
    
    /**
     * 执行验证的通用方法
     */
    protected <T extends BaseValidationResult> T executeValidation(
            String serviceName, 
            Supplier<T> resultSupplier,
            ValidationTask<T> validationTask) {
        
        T result = resultSupplier.get();
        result.setServiceName(serviceName);
        
        try {
            validationTask.execute(result);
            result.calculateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            result.addValidationError("Error during validation: " + e.getMessage());
            result.setOverallPassed(false);
        }
        
        return result;
    }
    
    /**
     * 检查类名是否包含服务名称
     */
    protected boolean isServiceRelated(Class<?> clazz, String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return true;
        }
        return clazz.getSimpleName().contains(serviceName) || 
               clazz.getName().contains(serviceName);
    }
    
    /**
     * 检查Bean名称是否包含服务名称
     */
    protected boolean isServiceRelated(String beanName, String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            return true;
        }
        return beanName.contains(serviceName);
    }
    
    /**
     * 验证任务接口
     */
    @FunctionalInterface
    protected interface ValidationTask<T extends BaseValidationResult> {
        void execute(T result) throws Exception;
    }
}
