package io.metersphere;

import io.metersphere.api.config.JmeterProperties;
import io.metersphere.system.config.MinioProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude = {
        QuartzAutoConfiguration.class,
        LdapAutoConfiguration.class,
        Neo4jAutoConfiguration.class,
        // 临时禁用Spring AI的自动配置，避免数据库连接问题
        org.springframework.ai.model.chat.memory.repository.jdbc.autoconfigure.JdbcChatMemoryRepositoryAutoConfiguration.class
})
@PropertySource(value = {
        "classpath:commons.properties",
        "classpath:minio.properties"
}, encoding = "UTF-8", ignoreResourceNotFound = true)
@ServletComponentScan
// 临时注释掉服务发现，避免Nacos gRPC问题
// @EnableDiscoveryClient
@EnableConfigurationProperties({
        MinioProperties.class,
        JmeterProperties.class
})
public class Application {
    public static void main(String[] args) {
        // 在Spring Boot启动之前强制设置Nacos使用HTTP传输
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
        System.out.println("=== 数据库配置将通过application-dev.yml加载 ===");
        System.out.println("=====================================");
        
        SpringApplication.run(Application.class, args);
    }
}
