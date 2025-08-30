package com.jiangyang.dubbo.config;

import org.apache.dubbo.config.MetadataReportConfig;
import org.apache.dubbo.config.MetricsConfig;
import org.apache.dubbo.config.ProviderConfig;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo服务治理配置
 * 包括健康检查、监控、元数据报告等功能
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Configuration
@EnableDubbo
@ConditionalOnProperty(name = "dubbo.governance.enabled", havingValue = "true", matchIfMissing = true)
public class DubboGovernanceConfig {

    // Provider配置参数
    @Value("${dubbo.provider.threads:200}")
    private Integer providerThreads;
    
    @Value("${dubbo.provider.accepts:1000}")
    private Integer providerAccepts;
    
    @Value("${dubbo.provider.timeout:5000}")
    private Integer providerTimeout;
    
    @Value("${dubbo.provider.retries:0}")
    private Integer providerRetries;
    
    @Value("${dubbo.provider.stub:true}")
    private String providerStub;
    
    @Value("${dubbo.application.version:1.0.0}")
    private String applicationVersion;
    
    // 元数据报告配置参数
    @Value("${dubbo.metadata-report.address:nacos}")
    private String metadataReportAddress;
    
    @Value("${dubbo.metadata-report.group}")
    private String metadataReportGroup;
    
    @Value("${dubbo.metadata-report.retry-times:3}")
    private Integer metadataRetryTimes;
    
    @Value("${dubbo.metadata-report.retry-period:5000}")
    private Integer metadataRetryPeriod;
    
    @Value("${dubbo.metadata-report.cycle-report:false}")
    private Boolean metadataCycleReport;
    
    // 监控指标配置参数
    @Value("${dubbo.metrics.protocol:prometheus}")
    private String metricsProtocol;
    
    @Value("${dubbo.metrics.port:28080}")
    private String metricsPort;
    
    @Value("${dubbo.metrics.enable-jvm:true}")
    private Boolean metricsEnableJvm;

    /**
     * 提供者配置增强
     */
    @Bean
    @ConditionalOnProperty(name = "dubbo.provider.enhanced.enabled", havingValue = "true", matchIfMissing = true)
    public ProviderConfig providerConfig() {
        ProviderConfig config = new ProviderConfig();
        
        // 设置线程池配置
        config.setThreads(providerThreads);
        config.setAccepts(providerAccepts);
        
        // 设置超时和重试
        config.setTimeout(providerTimeout);
        config.setRetries(providerRetries);
        
        // 启用服务降级
        config.setStub(providerStub);
        
        // 设置服务版本
        config.setVersion(applicationVersion);
        
        return config;
    }

    /**
     * 元数据报告配置
     */
    @Bean
    @ConditionalOnProperty(name = "dubbo.metadata-report.enabled", havingValue = "true", matchIfMissing = true)
    public MetadataReportConfig metadataReportConfig() {
        MetadataReportConfig config = new MetadataReportConfig();
        
        // 使用配置的元数据中心地址
        config.setAddress(metadataReportAddress);
        config.setGroup(metadataReportGroup);
        
        // 设置重试配置
        config.setRetryTimes(metadataRetryTimes);
        config.setRetryPeriod(metadataRetryPeriod);
        
        // 启用本地缓存
        config.setCycleReport(metadataCycleReport);
        
        return config;
    }

    /**
     * 监控指标配置
     */
    @Bean
    @ConditionalOnProperty(name = "dubbo.metrics.enabled", havingValue = "true", matchIfMissing = false)
    public MetricsConfig metricsConfig() {
        MetricsConfig config = new MetricsConfig();
        
        // 设置协议
        config.setProtocol(metricsProtocol);
        
        // 设置端口（避免冲突）
        config.setProtocol(metricsPort);
        
        // 启用JVM指标
        config.setEnableJvm(metricsEnableJvm);
        
        return config;
    }
}