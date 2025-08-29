package com.jiangyang.dubbo.config;

import org.apache.dubbo.config.ProtocolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Dubbo序列化配置
 * 统一使用Hessian2序列化协议
 * 
 * @author jiangyang
 * @since 1.0.0
 */
@Configuration
public class DubboSerializationConfig {

    @Value("${dubbo.protocol.name}")
    private String protocolName;

    @Value("${dubbo.protocol.port}")
    private Integer protocolPort;

    @Value("${dubbo.protocol.threads:200}")
    private Integer protocolThreads;

    @Value("${dubbo.protocol.accepts:1000}")
    private Integer protocolAccepts;
    
    @Value("${dubbo.protocol.serialization:hessian2}")
    private String serialization;
    
    @Value("${dubbo.protocol.heartbeat:60000}")
    private Integer heartbeat;
    
    @Value("${dubbo.protocol.transporter:netty4}")
    private String transporter;
    
    @Value("${dubbo.protocol.dispatcher:all}")
    private String dispatcher;
    
    @Value("${dubbo.protocol.buffer:8192}")
    private Integer buffer;

    /**
     * Dubbo协议配置
     * 统一使用hessian2序列化
     */
    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig config = new ProtocolConfig();
        
        // 设置协议名称
        config.setName(protocolName);
        
        // 设置端口（-1表示自动分配）
        config.setPort(protocolPort);
        
        // 使用配置的序列化方式
        config.setSerialization(serialization);
        
        // 设置线程池配置
        config.setThreads(protocolThreads);
        config.setAccepts(protocolAccepts);
        
        // 设置心跳间隔（毫秒）
        config.setHeartbeat(heartbeat);
        
        // 设置网络传输层
        config.setTransporter(transporter);
        config.setDispatcher(dispatcher);
        
        // 设置缓冲区大小
        config.setBuffer(buffer);
        
        return config;
    }
}