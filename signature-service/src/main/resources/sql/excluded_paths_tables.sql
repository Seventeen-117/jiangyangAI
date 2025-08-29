-- ================================================
-- 排除路径配置表结构和初始数据
-- 用于替代UnifiedSecurityFilter中的硬编码排除路径
-- ================================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ================================================
-- 1. 创建排除路径配置表
-- ================================================
CREATE TABLE IF NOT EXISTS `excluded_path_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `path_pattern` VARCHAR(200) NOT NULL COMMENT '路径模式（支持前缀匹配和精确匹配）',
    `path_name` VARCHAR(100) NOT NULL COMMENT '路径名称',
    `path_type` VARCHAR(20) NOT NULL DEFAULT 'PREFIX' COMMENT '路径类型：PREFIX-前缀匹配，EXACT-精确匹配',
    `http_methods` VARCHAR(100) COMMENT 'HTTP方法（GET,POST,PUT,DELETE等，多个用逗号分隔，为空表示所有方法）',
    `exclude_reason` VARCHAR(200) COMMENT '排除原因',
    `description` TEXT COMMENT '路径描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_path_pattern` (`path_pattern`),
    INDEX `idx_path_type` (`path_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排除路径配置表';

-- ================================================
-- 2. 插入初始数据（从硬编码转换而来）
-- ================================================
INSERT INTO `excluded_path_config` 
(`path_pattern`, `path_name`, `path_type`, `http_methods`, `exclude_reason`, `description`, `status`, `sort_order`) 
VALUES
-- 监控和健康检查路径
('/actuator', 'Actuator监控端点', 'PREFIX', '', '系统监控端点，需要公开访问', 'Spring Boot Actuator监控端点，用于系统监控和健康检查', 1, 10),
('/health', '健康检查端点', 'PREFIX', '', '健康检查端点，需要公开访问', '系统健康检查端点，用于负载均衡器和服务发现', 1, 20),
('/metrics', '指标监控端点', 'PREFIX', '', '指标监控端点，需要公开访问', '系统指标监控端点，用于性能监控', 1, 30),

-- 公共资源路径
('/public', '公共资源路径', 'PREFIX', '', '公共资源，无需验证', '静态公共资源，如图片、CSS、JS等', 1, 40),

-- API验证相关路径
('/api/validation', 'API验证端点', 'PREFIX', '', '验证服务自身端点，避免循环验证', 'API验证服务自身的端点，用于验证状态查询', 1, 50),

-- Swagger文档路径
('/swagger-ui', 'Swagger UI界面', 'PREFIX', '', 'API文档界面，开发环境需要访问', 'Swagger API文档界面，用于API文档查看和测试', 1, 60),
('/swagger-ui.html', 'Swagger UI主页', 'EXACT', '', 'Swagger UI主页，开发环境需要访问', 'Swagger UI主页，API文档入口', 1, 70),
('/v3/api-docs', 'OpenAPI文档', 'PREFIX', '', 'OpenAPI规范文档，开发环境需要访问', 'OpenAPI 3.0规范文档，用于API文档生成', 1, 80),

-- WebJars资源路径
('/webjars', 'WebJars资源', 'PREFIX', '', 'WebJars静态资源，无需验证', 'WebJars管理的静态资源，如Bootstrap、jQuery等', 1, 90);

-- ================================================
-- 3. 创建排除路径缓存表（可选，用于性能优化）
-- ================================================
CREATE TABLE IF NOT EXISTS `excluded_path_cache` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `cache_key` VARCHAR(200) NOT NULL UNIQUE COMMENT '缓存键（path:method）',
    `path_pattern` VARCHAR(200) NOT NULL COMMENT '匹配的路径模式',
    `is_excluded` TINYINT DEFAULT 1 COMMENT '是否排除：0-不排除，1-排除',
    `cache_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '缓存时间',
    `expire_time` DATETIME COMMENT '过期时间',
    INDEX `idx_cache_key` (`cache_key`),
    INDEX `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排除路径缓存表';

-- ================================================
-- 4. 创建排除路径统计表（可选，用于监控）
-- ================================================
CREATE TABLE IF NOT EXISTS `excluded_path_stats` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `path_pattern` VARCHAR(200) NOT NULL COMMENT '路径模式',
    `http_method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
    `exclude_count` BIGINT DEFAULT 0 COMMENT '排除次数',
    `last_access_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_path_method` (`path_pattern`, `http_method`),
    INDEX `idx_exclude_count` (`exclude_count`),
    INDEX `idx_last_access_time` (`last_access_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排除路径统计表';

SET FOREIGN_KEY_CHECKS = 1;
