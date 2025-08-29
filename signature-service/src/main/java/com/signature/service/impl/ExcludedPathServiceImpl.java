package com.signature.service.impl;

import com.signature.entity.ExcludedPathConfig;
import com.signature.mapper.ExcludedPathConfigMapper;
import com.signature.service.ExcludedPathService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 排除路径配置服务实现类
 * 用于替代UnifiedSecurityFilter中的硬编码排除路径
 */
@Slf4j
@Service
public class ExcludedPathServiceImpl implements ExcludedPathService {

    @Autowired
    private ExcludedPathConfigMapper excludedPathConfigMapper;

    // 本地缓存，用于提高性能
    private final ConcurrentHashMap<String, ExcludedPathConfig> pathCache = new ConcurrentHashMap<>();
    
    // 定时任务执行器，用于定期刷新缓存
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * 构造函数，启动定时刷新缓存任务
     */
    public ExcludedPathServiceImpl() {
        // 每5分钟刷新一次缓存
        scheduler.scheduleAtFixedRate(this::refreshCache, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    @Cacheable(value = "excludedPaths", key = "#path + ':' + #method")
    public boolean isExcludedPath(String path, String method) {
        if (!StringUtils.hasText(path)) {
            return false;
        }

        try {
            // 先从本地缓存查询
            String cacheKey = path + ":" + method;
            ExcludedPathConfig cachedConfig = pathCache.get(cacheKey);
            if (cachedConfig != null) {
                log.debug("Found excluded path in cache: {} {} -> {}", method, path, cachedConfig.getPathName());
                return true;
            }

            // 从数据库查询
            ExcludedPathConfig config = excludedPathConfigMapper.selectByPathAndMethod(path, method);
            if (config != null) {
                // 更新本地缓存
                pathCache.put(cacheKey, config);
                log.debug("Found excluded path in database: {} {} -> {}", method, path, config.getPathName());
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Error checking excluded path: {} {}", method, path, e);
            // 发生异常时，使用硬编码的备用逻辑
            return isHardcodedExcludedPath(path, method);
        }
    }

    /**
     * 硬编码的备用排除路径逻辑（当数据库查询失败时使用）
     * @param path 请求路径
     * @param method HTTP方法
     * @return 是否需要跳过验证
     */
    private boolean isHardcodedExcludedPath(String path, String method) {
        return path.startsWith("/actuator") || 
               path.startsWith("/health") || 
               path.startsWith("/metrics") ||
               path.startsWith("/public") ||
               path.startsWith("/api/validation") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/webjars") ||
               path.equals("/swagger-ui.html");
    }

    @Override
    @CacheEvict(value = "excludedPaths", allEntries = true)
    public void refreshCache() {
        try {
            log.debug("Refreshing excluded path cache...");
            pathCache.clear();
            
            // 重新加载所有启用的配置到缓存
            List<ExcludedPathConfig> configs = excludedPathConfigMapper.selectAllEnabled();
            for (ExcludedPathConfig config : configs) {
                if (config.getHttpMethods() != null && !config.getHttpMethods().trim().isEmpty()) {
                    // 如果有指定HTTP方法，为每个方法创建缓存项
                    String[] methods = config.getHttpMethods().split(",");
                    for (String method : methods) {
                        String cacheKey = config.getPathPattern() + ":" + method.trim();
                        pathCache.put(cacheKey, config);
                    }
                } else {
                    // 如果没有指定HTTP方法，为所有常用方法创建缓存项
                    String[] commonMethods = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"};
                    for (String method : commonMethods) {
                        String cacheKey = config.getPathPattern() + ":" + method;
                        pathCache.put(cacheKey, config);
                    }
                }
            }
            
            log.info("Excluded path cache refreshed, loaded {} configurations", configs.size());
        } catch (Exception e) {
            log.error("Error refreshing excluded path cache", e);
        }
    }

    @Override
    public List<ExcludedPathConfig> getAllEnabledConfigs() {
        return excludedPathConfigMapper.selectAllEnabled();
    }

    @Override
    public ExcludedPathConfig getById(Long id) {
        return excludedPathConfigMapper.selectById(id);
    }

    @Override
    public boolean addConfig(ExcludedPathConfig config) {
        try {
            int result = excludedPathConfigMapper.insert(config);
            if (result > 0) {
                refreshCache();
                log.info("Added excluded path config: {}", config.getPathPattern());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error adding excluded path config: {}", config.getPathPattern(), e);
            return false;
        }
    }

    @Override
    public boolean updateConfig(ExcludedPathConfig config) {
        try {
            int result = excludedPathConfigMapper.update(config);
            if (result > 0) {
                refreshCache();
                log.info("Updated excluded path config: {}", config.getPathPattern());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error updating excluded path config: {}", config.getPathPattern(), e);
            return false;
        }
    }

    @Override
    public boolean deleteConfig(Long id) {
        try {
            ExcludedPathConfig config = excludedPathConfigMapper.selectById(id);
            if (config == null) {
                return false;
            }
            
            int result = excludedPathConfigMapper.deleteById(id);
            if (result > 0) {
                refreshCache();
                log.info("Deleted excluded path config: {}", config.getPathPattern());
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting excluded path config with id: {}", id, e);
            return false;
        }
    }

    @Override
    public List<ExcludedPathConfig> getConfigsByPage(int page, int size) {
        int offset = (page - 1) * size;
        return excludedPathConfigMapper.selectByPage(offset, size);
    }

    @Override
    public int getConfigCount() {
        return excludedPathConfigMapper.selectCount();
    }

    @Override
    public List<ExcludedPathConfig> getConfigsByPathPattern(String pathPattern) {
        return excludedPathConfigMapper.selectByPathPattern(pathPattern);
    }

    @Override
    public List<ExcludedPathConfig> getConfigsByStatus(Integer status) {
        return excludedPathConfigMapper.selectByStatus(status);
    }
}
