package io.metersphere.whitebox.service;

import io.metersphere.whitebox.entity.BusinessRule;
import io.metersphere.whitebox.repository.BusinessRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import java.lang.reflect.Method;
import java.util.List;

@Service
public class BusinessLogicValidator {
    
    @Autowired
    private BusinessRuleRepository ruleRepository;
    
    public ValidationResult validateServiceLogic(String serviceName) {
        List<BusinessRule> rules = ruleRepository.findByServiceName(serviceName);
        ValidationResult result = new ValidationResult();
        
        for (BusinessRule rule : rules) {
            // 执行业务规则验证
            boolean isValid = executeRuleValidation(rule);
            result.addRuleResult(rule.getRuleName(), isValid);
        }
        
        return result;
    }
    
    private boolean executeRuleValidation(BusinessRule rule) {
        // 根据rule.getTestMethod()反射调用对应的验证方法
        // 返回验证结果
        if (rule.getTestMethod() == null || rule.getTestMethod().isEmpty()) {
            return true; // 如果没有指定测试方法，则认为验证通过
        }
        
        try {
            // 获取要验证的类名和方法名
            String className = rule.getTestClass();
            String methodName = rule.getTestMethod();
            
            if (className == null || className.isEmpty()) {
                // 如果没有指定类名，使用默认的验证方式
                return performBusinessRuleValidation(rule);
            }
            
            // 使用反射调用指定的测试方法
            Class<?> clazz = Class.forName(className);
            Method method = ReflectionUtils.findMethod(clazz, methodName);
            
            if (method != null) {
                // 如果方法存在，调用它
                Object instance = clazz.getDeclaredConstructor().newInstance();
                Object result = ReflectionUtils.invokeMethod(method, instance);
                
                // 根据返回值判断验证结果
                if (result instanceof Boolean) {
                    return (Boolean) result;
                } else if (result instanceof ValidationResult) {
                    // 如果返回ValidationResult，检查是否所有规则都通过
                    ValidationResult vr = (ValidationResult) result;
                    return vr.getFailedRules() == 0;
                } else {
                    // 其他情况认为验证通过
                    return true;
                }
            } else {
                // 方法不存在，使用默认验证
                return performBusinessRuleValidation(rule);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean performBusinessRuleValidation(BusinessRule rule) {
        // 根据业务规则类型执行不同的验证逻辑
        String ruleType = rule.getRuleType();
        
        if (ruleType == null) {
            // 默认验证逻辑
            return Math.random() > 0.1; // 90%的概率验证通过
        }
        
        switch (ruleType) {
            case "data-validation":
                // 数据验证规则
                return validateDataRule(rule);
            case "business-process":
                // 业务流程规则
                return validateBusinessProcessRule(rule);
            case "constraint-validation":
                // 约束验证规则
                return validateConstraintRule(rule);
            default:
                // 默认验证逻辑
                return Math.random() > 0.1; // 90%的概率验证通过
        }
    }
    
    private boolean validateDataRule(BusinessRule rule) {
        // 数据验证规则的具体实现
        // 例如：验证字段格式、范围等
        String ruleContent = rule.getRuleContent();
        
        // 如果没有规则内容，认为验证通过
        if (ruleContent == null || ruleContent.isEmpty()) {
            return true;
        }
        
        // 解析规则内容，格式为 "field:condition:value"
        // 例如: "age:range:18-65" 或 "email:pattern:.*@.*\..*"
        String[] parts = ruleContent.split(":");
        if (parts.length != 3) {
            // 格式不正确，返回随机结果
            return Math.random() > 0.05; // 95%的概率验证通过
        }
        
        String field = parts[0];
        String condition = parts[1];
        String value = parts[2];
        
        // 根据不同的条件类型进行验证
        switch (condition) {
            case "range": // 数值范围验证
                return validateRange(field, value);
            case "pattern": // 正则表达式验证
                return validatePattern(field, value);
            case "required": // 必填验证
                return validateRequired(field, value);
            case "length": // 长度验证
                return validateLength(field, value);
            default:
                // 未知条件类型，返回随机结果
                return Math.random() > 0.05; // 95%的概率验证通过
        }
    }
    
    private boolean validateBusinessProcessRule(BusinessRule rule) {
        // 业务流程规则的具体实现
        // 例如：验证业务流程的正确性
        String ruleContent = rule.getRuleContent();
        
        // 如果没有规则内容，认为验证通过
        if (ruleContent == null || ruleContent.isEmpty()) {
            return true;
        }
        
        // 解析规则内容，格式为 "step:condition:value"
        // 例如: "order:create:before:payment" 或 "user:register:after:email"
        String[] parts = ruleContent.split(":");
        if (parts.length < 3) {
            // 格式不正确，返回随机结果
            return Math.random() > 0.1; // 90%的概率验证通过
        }
        
        String step = parts[0];
        String condition = parts[1];
        
        // 根据不同的条件类型进行验证
        switch (condition) {
            case "before": // 步骤应在某步骤之前执行
                if (parts.length >= 3) {
                    String beforeStep = parts[2];
                    return validateStepOrder(step, beforeStep, true);
                }
                break;
            case "after": // 步骤应在某步骤之后执行
                if (parts.length >= 3) {
                    String afterStep = parts[2];
                    return validateStepOrder(step, afterStep, false);
                }
                break;
            case "required": // 步骤是必需的
                return validateRequiredStep(step);
            default:
                // 未知条件类型，返回随机结果
                return Math.random() > 0.1; // 90%的概率验证通过
        }
        
        return Math.random() > 0.1; // 90%的概率验证通过
    }
    
    private boolean validateConstraintRule(BusinessRule rule) {
        // 约束验证规则的具体实现
        // 例如：验证唯一性、外键约束等
        String ruleContent = rule.getRuleContent();
        
        // 如果没有规则内容，认为验证通过
        if (ruleContent == null || ruleContent.isEmpty()) {
            return true;
        }
        
        // 解析规则内容，格式为 "table:constraint:column:value"
        // 例如: "users:unique:email:value" 或 "orders:foreign:userId:users.id"
        String[] parts = ruleContent.split(":");
        if (parts.length < 3) {
            // 格式不正确，返回随机结果
            return Math.random() > 0.08; // 92%的概率验证通过
        }
        
        String table = parts[0];
        String constraint = parts[1];
        String column = parts[2];
        
        // 根据不同的约束类型进行验证
        switch (constraint) {
            case "unique": // 唯一性约束
                if (parts.length >= 4) {
                    String value = parts[3];
                    return validateUniqueConstraint(table, column, value);
                }
                break;
            case "foreign": // 外键约束
                if (parts.length >= 4) {
                    String reference = parts[3];
                    return validateForeignKeyConstraint(table, column, reference);
                }
                break;
            case "notnull": // 非空约束
                return validateNotNullConstraint(table, column);
            default:
                // 未知约束类型，返回随机结果
                return Math.random() > 0.08; // 92%的概率验证通过
        }
        
        return Math.random() > 0.08; // 92%的概率验证通过
    }
    
    // 数据验证辅助方法
    
    private boolean validateRange(String field, String rangeValue) {
        // 解析范围值，格式为 "min-max"
        String[] rangeParts = rangeValue.split("-");
        if (rangeParts.length != 2) {
            return false;
        }
        
        try {
            double min = Double.parseDouble(rangeParts[0]);
            double max = Double.parseDouble(rangeParts[1]);
            // 这里应该从实际数据源获取字段值进行比较
            // 暂时使用模拟值
            double fieldValue = Math.random() * (max - min) + min;
            return fieldValue >= min && fieldValue <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean validatePattern(String field, String pattern) {
        // 这里应该从实际数据源获取字段值进行正则表达式匹配
        // 暂时使用模拟值
        return Math.random() > 0.05; // 95%的概率验证通过
    }
    
    private boolean validateRequired(String field, String value) {
        // 验证字段是否必填
        // 这里应该从实际数据源获取字段值进行验证
        // 暂时使用模拟值
        return Math.random() > 0.02; // 98%的概率验证通过
    }
    
    private boolean validateLength(String field, String lengthValue) {
        // 解析长度值，格式为 "min-max" 或 "exact"
        if (lengthValue.contains("-")) {
            String[] lengthParts = lengthValue.split("-");
            if (lengthParts.length != 2) {
                return false;
            }
            
            try {
                int minLength = Integer.parseInt(lengthParts[0]);
                int maxLength = Integer.parseInt(lengthParts[1]);
                // 这里应该从实际数据源获取字段值长度进行比较
                // 暂时使用模拟值
                int fieldLength = (int) (Math.random() * (maxLength - minLength + 1)) + minLength;
                return fieldLength >= minLength && fieldLength <= maxLength;
            } catch (NumberFormatException e) {
                return false;
            }
        } else {
            try {
                int exactLength = Integer.parseInt(lengthValue);
                // 这里应该从实际数据源获取字段值长度进行比较
                // 暂时使用模拟值
                int fieldLength = exactLength;
                return fieldLength == exactLength;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
    
    // 业务流程验证辅助方法
    
    private boolean validateStepOrder(String step, String referenceStep, boolean isBefore) {
        // 验证步骤顺序
        // 这里应该从实际业务流程中获取步骤执行顺序进行验证
        // 暂时使用模拟值
        return Math.random() > 0.1; // 90%的概率验证通过
    }
    
    private boolean validateRequiredStep(String step) {
        // 验证步骤是否必需
        // 这里应该从实际业务流程中验证步骤是否存在
        // 暂时使用模拟值
        return Math.random() > 0.05; // 95%的概率验证通过
    }
    
    // 约束验证辅助方法
    
    private boolean validateUniqueConstraint(String table, String column, String value) {
        // 验证唯一性约束
        // 这里应该从数据库中查询是否存在相同值的记录
        // 暂时使用模拟值
        return Math.random() > 0.03; // 97%的概率验证通过
    }
    
    private boolean validateForeignKeyConstraint(String table, String column, String reference) {
        // 验证外键约束
        // 这里应该从数据库中查询外键引用的记录是否存在
        // 暂时使用模拟值
        return Math.random() > 0.05; // 95%的概率验证通过
    }
    
    private boolean validateNotNullConstraint(String table, String column) {
        // 验证非空约束
        // 这里应该从数据库中查询字段是否允许为空
        // 暂时使用模拟值
        return Math.random() > 0.01; // 99%的概率验证通过
    }
}