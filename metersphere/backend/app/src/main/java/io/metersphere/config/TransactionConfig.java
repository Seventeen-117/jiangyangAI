package io.metersphere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 事务配置类 - 确保事务管理器正确配置
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * 配置 JPA 事务管理器
     * 确保事务管理器能够正确处理数据库连接
     */
    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(dataSource);
        // 设置事务超时时间（秒）
        transactionManager.setDefaultTimeout(30);
        // 允许嵌套事务
        transactionManager.setNestedTransactionAllowed(true);
        // 设置回滚规则
        transactionManager.setRollbackOnCommitFailure(true);
        return transactionManager;
    }
}
