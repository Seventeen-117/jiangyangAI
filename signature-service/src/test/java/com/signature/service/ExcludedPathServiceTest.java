package com.signature.service;

import com.signature.entity.ExcludedPathConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcludedPathService 测试类
 */
@SpringBootTest
@ActiveProfiles("test")
public class ExcludedPathServiceTest {

    @Autowired
    private ExcludedPathService excludedPathService;

    @Test
    public void testIsExcludedPath() {
        // 测试排除路径
        assertTrue(excludedPathService.isExcludedPath("/actuator/health", "GET"));
        assertTrue(excludedPathService.isExcludedPath("/health", "GET"));
        assertTrue(excludedPathService.isExcludedPath("/swagger-ui.html", "GET"));
        assertTrue(excludedPathService.isExcludedPath("/public/images/logo.png", "GET"));
        
        // 测试非排除路径
        assertFalse(excludedPathService.isExcludedPath("/api/users", "GET"));
        assertFalse(excludedPathService.isExcludedPath("/api/orders", "POST"));
    }

    @Test
    public void testGetAllEnabledConfigs() {
        List<ExcludedPathConfig> configs = excludedPathService.getAllEnabledConfigs();
        assertNotNull(configs);
        assertFalse(configs.isEmpty());
        
        // 验证配置项的基本信息
        for (ExcludedPathConfig config : configs) {
            assertNotNull(config.getId());
            assertNotNull(config.getPathPattern());
            assertNotNull(config.getPathName());
            assertNotNull(config.getPathType());
            assertEquals(1, config.getStatus()); // 启用的配置
        }
    }

    @Test
    public void testAddAndDeleteConfig() {
        // 创建测试配置
        ExcludedPathConfig testConfig = ExcludedPathConfig.builder()
                .pathPattern("/test/path")
                .pathName("测试路径")
                .pathType("PREFIX")
                .httpMethods("GET,POST")
                .excludeReason("测试用途")
                .description("用于测试的排除路径")
                .status(1)
                .sortOrder(999)
                .build();

        // 添加配置
        boolean addResult = excludedPathService.addConfig(testConfig);
        assertTrue(addResult);

        // 验证配置是否生效
        assertTrue(excludedPathService.isExcludedPath("/test/path", "GET"));
        assertTrue(excludedPathService.isExcludedPath("/test/path/sub", "POST"));

        // 获取配置列表，验证是否包含新配置
        List<ExcludedPathConfig> configs = excludedPathService.getAllEnabledConfigs();
        boolean found = configs.stream()
                .anyMatch(config -> "/test/path".equals(config.getPathPattern()));
        assertTrue(found);

        // 清理：删除测试配置
        if (testConfig.getId() != null) {
            boolean deleteResult = excludedPathService.deleteConfig(testConfig.getId());
            assertTrue(deleteResult);
        }
    }

    @Test
    public void testGetConfigsByPage() {
        // 测试分页查询
        List<ExcludedPathConfig> page1 = excludedPathService.getConfigsByPage(1, 5);
        assertNotNull(page1);
        assertTrue(page1.size() <= 5);

        List<ExcludedPathConfig> page2 = excludedPathService.getConfigsByPage(2, 5);
        assertNotNull(page2);

        // 验证总数
        int total = excludedPathService.getConfigCount();
        assertTrue(total > 0);
    }

    @Test
    public void testGetConfigsByPathPattern() {
        // 测试根据路径模式查询
        List<ExcludedPathConfig> actuatorConfigs = excludedPathService.getConfigsByPathPattern("/actuator");
        assertNotNull(actuatorConfigs);
        assertFalse(actuatorConfigs.isEmpty());
        
        for (ExcludedPathConfig config : actuatorConfigs) {
            assertEquals("/actuator", config.getPathPattern());
        }
    }

    @Test
    public void testGetConfigsByStatus() {
        // 测试根据状态查询
        List<ExcludedPathConfig> enabledConfigs = excludedPathService.getConfigsByStatus(1);
        assertNotNull(enabledConfigs);
        
        for (ExcludedPathConfig config : enabledConfigs) {
            assertEquals(1, config.getStatus());
        }
    }

    @Test
    public void testRefreshCache() {
        // 测试缓存刷新（不应该抛出异常）
        assertDoesNotThrow(() -> excludedPathService.refreshCache());
    }

    @Test
    public void testNullAndEmptyPath() {
        // 测试空路径和null路径
        assertFalse(excludedPathService.isExcludedPath(null, "GET"));
        assertFalse(excludedPathService.isExcludedPath("", "GET"));
        assertFalse(excludedPathService.isExcludedPath("   ", "GET"));
    }
}
