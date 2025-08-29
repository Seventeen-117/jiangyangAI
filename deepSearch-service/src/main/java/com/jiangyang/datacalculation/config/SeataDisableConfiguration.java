package com.jiangyang.datacalculation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Seata 禁用配置类
 * 设置 Seata 相关系统属性，确保完全禁用 Seata 功能
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "seata.enabled", havingValue = "false", matchIfMissing = true)
public class SeataDisableConfiguration {
    
    public SeataDisableConfiguration() {
        // 设置 Seata 禁用相关系统属性
        System.setProperty("seata.enabled", "false");
        System.setProperty("seata.auto-data-source-proxy", "false");
        System.setProperty("seata.enable-auto-data-source-proxy", "false");
        System.setProperty("seata.disable-global-transaction", "true");
        
        // 禁用 Seata 相关日志输出
        System.setProperty("logging.level.io.seata", "OFF");
        System.setProperty("logging.level.com.jiangyang.base.seata", "OFF");
        System.setProperty("logging.level.com.jiangyang.base.seata.util.SeataConfigValidator", "OFF");
    }
}