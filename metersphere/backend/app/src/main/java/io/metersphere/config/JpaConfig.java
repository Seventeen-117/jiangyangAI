package io.metersphere.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * JPA 配置类 - 为 whitebox-testing 模块提供 entityManagerFactory 支持
 * 确保 JPA 和 MyBatis 能够正确共存
 */
@Configuration
@ConditionalOnProperty(name = "spring.jpa.hibernate.ddl-auto", havingValue = "validate", matchIfMissing = true)
public class JpaConfig {

    /**
     * 配置 EntityManagerFactory
     * 使用与主数据源相同的 DataSource
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("io.metersphere.whitebox.entity"); // 只扫描 whitebox 相关的实体
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL8Dialect");
        vendorAdapter.setShowSql(false);
        vendorAdapter.setGenerateDdl(false); // 不自动生成 DDL，由 Flyway 管理
        
        em.setJpaVendorAdapter(vendorAdapter);
        
        // JPA 属性配置
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.use_sql_comments", "true");
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.batch_versioned_data", "true");
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "false");
        properties.setProperty("hibernate.cache.use_second_level_cache", "false");
        properties.setProperty("hibernate.cache.use_query_cache", "false");
        
        em.setJpaProperties(properties);
        
        return em;
    }

    // 事务管理器配置已移至 TransactionConfig 类
}
