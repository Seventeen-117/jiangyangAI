package io.metersphere.whitebox.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "business_rules")
public class BusinessRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String serviceName;      // 服务名称
    private String ruleName;         // 规则名称
    private String ruleDescription;  // 规则描述
    private String ruleExpression;   // 规则表达式
    private String ruleContent;      // 规则内容
    private String ruleType;         // 规则类型
    private Boolean isCoreRule;      // 是否为核心规则
    private String testClass;        // 验证类
    private String testMethod;       // 验证方法
    
    // getters and setters
    
    public String getTestClass() {
        return testClass;
    }
    
    public String getRuleContent() {
        return ruleContent;
    }
    
    public String getRuleType() {
        return ruleType;
    }
}