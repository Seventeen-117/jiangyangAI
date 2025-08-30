package io.metersphere.config;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class FlywayConfig {

    @Value("${flyway.url:jdbc:mysql://8.133.246.113:3306/metersphere?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&allowMultiQueries=true&rewriteBatchedStatements=true&useServerPrepStmts=true&cachePrepStmts=true&defaultFetchSize=1000&autoReconnect=true&failOverReadOnly=false&maxReconnects=10}")
    private String flywayUrl;

    @Value("${flyway.user:bgtech}")
    private String flywayUser;

    @Value("${flyway.password:Zly689258..}")
    private String flywayPassword;

    @Bean
    @Primary
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(flywayUrl, flywayUser, flywayPassword)
                .locations("classpath:migration")
                .table("metersphere_version")
                .baselineOnMigrate(true)
                .baselineVersion(MigrationVersion.fromVersion("0"))
                .encoding("UTF-8")
                .validateOnMigrate(false)
                .initSql("SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci")
                .connectRetries(3)
                .connectRetriesInterval(1)
                .load();
        
        return flyway;
    }

    @Bean
    @Primary
    public FlywayMigrationInitializer flywayMigrationInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }
}
