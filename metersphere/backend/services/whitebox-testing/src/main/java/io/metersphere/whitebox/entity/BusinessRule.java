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
    
    @Column(name = "service_name")
    private String serviceName;      // 服务名称
    
    @Column(name = "rule_name")
    private String ruleName;         // 规则名称
    
    @Column(name = "rule_description")
    private String ruleDescription;  // 规则描述
    
    @Column(name = "rule_expression")
    private String ruleExpression;   // 规则表达式
    
    @Column(name = "rule_content")
    private String ruleContent;      // 规则内容
    
    @Column(name = "rule_type")
    private String ruleType;         // 规则类型
    
    @Column(name = "is_core_rule")
    private Boolean isCoreRule;      // 是否为核心规则
    
    @Column(name = "test_class")
    private String testClass;        // 验证类
    
    @Column(name = "test_method")
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