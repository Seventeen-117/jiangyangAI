package io.metersphere.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * Nacos配置类 - 强制使用HTTP传输
 * 在应用启动时设置系统属性，确保Nacos使用HTTP传输而不是gRPC
 */
@Configuration
@Order(1) // 确保在其他配置之前执行
public class NacosConfig implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // 强制设置Nacos使用HTTP传输
        System.setProperty("nacos.client.transport.type", "http");
        System.setProperty("nacos.client.naming.transport.type", "http");
        System.setProperty("nacos.client.config.transport.type", "http");
        System.setProperty("nacos.transport.type", "http");
        System.setProperty("nacos.naming.transport.type", "http");
        System.setProperty("nacos.config.transport.type", "http");
        
        // 禁用gRPC相关功能
        System.setProperty("nacos.client.grpc.enabled", "false");
        System.setProperty("nacos.client.grpc.transport.enabled", "false");
        
        // 设置HTTP传输相关参数
        System.setProperty("nacos.client.http.timeout", "10000");
        System.setProperty("nacos.client.http.retry.time", "20000");
        
        System.out.println("=== Nacos HTTP传输配置已强制设置 ===");
        System.out.println("nacos.client.transport.type: " + System.getProperty("nacos.client.transport.type"));
        System.out.println("nacos.client.naming.transport.type: " + System.getProperty("nacos.client.naming.transport.type"));
        System.out.println("nacos.client.config.transport.type: " + System.getProperty("nacos.client.config.transport.type"));
        System.out.println("=====================================");
    }
}
