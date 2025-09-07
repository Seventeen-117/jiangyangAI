-- MeterSphere Whitebox Testing 模块数据库表修复脚本
-- 修复字段名不匹配问题

USE metersphere;

-- 删除已存在的表（如果存在）
DROP TABLE IF EXISTS `whitebox_metrics`;
DROP TABLE IF EXISTS `business_rules`;

-- 重新创建业务规则表，使用正确的字段名
CREATE TABLE `business_rules` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `service_name` VARCHAR(255) DEFAULT NULL COMMENT '服务名称',
    `rule_name` VARCHAR(255) DEFAULT NULL COMMENT '规则名称',
    `rule_description` TEXT DEFAULT NULL COMMENT '规则描述',
    `rule_expression` TEXT DEFAULT NULL COMMENT '规则表达式',
    `rule_content` TEXT DEFAULT NULL COMMENT '规则内容',
    `rule_type` VARCHAR(100) DEFAULT NULL COMMENT '规则类型',
    `is_core_rule` BOOLEAN DEFAULT FALSE COMMENT '是否为核心规则',
    `test_class` VARCHAR(500) DEFAULT NULL COMMENT '验证类',
    `test_method` VARCHAR(255) DEFAULT NULL COMMENT '验证方法',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_service_name` (`service_name`),
    KEY `idx_rule_type` (`rule_type`),
    KEY `idx_is_core_rule` (`is_core_rule`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='业务规则表';

-- 重新创建白盒测试指标表
CREATE TABLE `whitebox_metrics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `service_name` VARCHAR(255) DEFAULT NULL COMMENT '服务名称',
    `metric_type` VARCHAR(100) DEFAULT NULL COMMENT '指标类型（覆盖率、复杂度等）',
    `metric_name` VARCHAR(255) DEFAULT NULL COMMENT '指标名称',
    `current_value` DOUBLE DEFAULT NULL COMMENT '当前值',
    `threshold_value` DOUBLE DEFAULT NULL COMMENT '阈值',
    `status` VARCHAR(50) DEFAULT NULL COMMENT '状态（PASS/FAIL）',
    `last_updated` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `created_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_service_name` (`service_name`),
    KEY `idx_metric_type` (`metric_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='白盒测试指标表';

-- 插入一些示例数据
INSERT INTO `business_rules` (`service_name`, `rule_name`, `rule_description`, `rule_expression`, `rule_content`, `rule_type`, `is_core_rule`, `test_class`, `test_method`) VALUES
('gateway-service', 'rate_limit_validation', '网关限流验证规则', 'rate_limit > 0', 'rate_limit:range:1-1000', 'data-validation', TRUE, 'io.metersphere.gateway.validator.RateLimitValidator', 'validateRateLimit'),
('messages-service', 'message_format_validation', '消息格式验证规则', 'message_format != null', 'message_format:required:true', 'data-validation', TRUE, 'io.metersphere.messages.validator.MessageValidator', 'validateMessageFormat'),
('signature-service', 'signature_algorithm_validation', '签名算法验证规则', 'algorithm in [RSA, ECDSA]', 'algorithm:pattern:^(RSA|ECDSA)$', 'constraint-validation', TRUE, 'io.metersphere.signature.validator.SignatureValidator', 'validateAlgorithm');

INSERT INTO `whitebox_metrics` (`service_name`, `metric_type`, `metric_name`, `current_value`, `threshold_value`, `status`) VALUES
('gateway-service', 'coverage', 'line_coverage', 85.5, 80.0, 'PASS'),
('gateway-service', 'coverage', 'branch_coverage', 78.2, 75.0, 'PASS'),
('messages-service', 'complexity', 'cyclomatic_complexity', 12.3, 15.0, 'PASS'),
('messages-service', 'coverage', 'line_coverage', 92.1, 80.0, 'PASS'),
('signature-service', 'coverage', 'line_coverage', 88.7, 80.0, 'PASS'),
('signature-service', 'complexity', 'cyclomatic_complexity', 8.5, 15.0, 'PASS');

-- 显示创建结果
SELECT 'Whitebox testing tables created successfully with correct field names' as result;
