package com.jiangyang.datacalculation.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 空操作的 Seata 配置验证器替代类
 * 用于替代 base-service 中的 SeataConfigValidator，避免 Seata 配置验证输出
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Component
@Primary
public class NoOpSeataConfigValidator implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 什么都不做，避免 Seata 配置验证输出
        // 这个类的存在就是为了替代 base-service 中的 SeataConfigValidator
        // 确保在应用启动时不会有任何 Seata 相关的配置验证日志输出
    }
}