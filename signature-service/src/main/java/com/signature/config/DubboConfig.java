package com.signature.config;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Dubbo 配置类
 * 优化关闭行为和减少警告信息
 */
@Configuration
@EnableDubbo
@ConditionalOnProperty(name = "dubbo.enabled", havingValue = "true", matchIfMissing = true)
public class DubboConfig {

    @Value("${dubbo.application.name:signature-service}")
    private String applicationName;

    @Value("${dubbo.registry.address:nacos://localhost:8848}")
    private String registryAddress;

    @Value("${dubbo.protocol.port:20881}")
    private Integer protocolPort;

    /**
     * 应用配置
     */
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig config = new ApplicationConfig();
        config.setName(applicationName);
        config.setVersion("1.0.0");
        config.setOwner("jiangyang");
        config.setOrganization("jiangyang-tech");
        
        // 禁用QOS服务（减少端口占用）
        config.setQosEnable(false);
        config.setQosPort(22222);
        config.setQosAcceptForeignIp(false);
        
        return config;
    }

    /**
     * 注册中心配置
     */
    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig config = new RegistryConfig();
        config.setAddress(registryAddress);
        config.setGroup("DEFAULT_GROUP");
        config.setTimeout(10000);
        config.setCheck(false); // 启动时不检查注册中心
        
        return config;
    }

    /**
     * 提供者配置
     */
    @Bean
    public ProviderConfig providerConfig() {
        ProviderConfig config = new ProviderConfig();
        config.setTimeout(5000);
        config.setRetries(0);
        config.setLoadbalance("roundrobin");
        config.setCluster("failfast");
        config.setSerialization("fastjson2");
        
        return config;
    }
}
