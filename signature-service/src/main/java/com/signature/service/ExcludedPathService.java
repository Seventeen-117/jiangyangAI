package com.signature.service;

import com.signature.entity.ExcludedPathConfig;

import java.util.List;

/**
 * 排除路径配置服务接口
 * 用于替代UnifiedSecurityFilter中的硬编码排除路径
 */
public interface ExcludedPathService {
    
    /**
     * 检查路径是否需要跳过验证
     * @param path 请求路径
     * @param method HTTP方法
     * @return 是否需要跳过验证
     */
    boolean isExcludedPath(String path, String method);
    
    /**
     * 刷新本地缓存
     */
    void refreshCache();
    
    /**
     * 获取所有启用的排除路径配置
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> getAllEnabledConfigs();
    
    /**
     * 根据ID获取排除路径配置
     * @param id 配置ID
     * @return 排除路径配置
     */
    ExcludedPathConfig getById(Long id);
    
    /**
     * 添加排除路径配置
     * @param config 排除路径配置
     * @return 是否成功
     */
    boolean addConfig(ExcludedPathConfig config);
    
    /**
     * 更新排除路径配置
     * @param config 排除路径配置
     * @return 是否成功
     */
    boolean updateConfig(ExcludedPathConfig config);
    
    /**
     * 删除排除路径配置
     * @param id 配置ID
     * @return 是否成功
     */
    boolean deleteConfig(Long id);
    
    /**
     * 分页查询排除路径配置
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> getConfigsByPage(int page, int size);
    
    /**
     * 获取排除路径配置总数
     * @return 总数
     */
    int getConfigCount();
    
    /**
     * 根据路径模式查询配置
     * @param pathPattern 路径模式
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> getConfigsByPathPattern(String pathPattern);
    
    /**
     * 根据状态查询配置
     * @param status 状态
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> getConfigsByStatus(Integer status);
}
