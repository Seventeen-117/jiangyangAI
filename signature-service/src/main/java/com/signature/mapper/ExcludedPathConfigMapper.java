package com.signature.mapper;

import com.signature.entity.ExcludedPathConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 排除路径配置Mapper接口
 */
@Mapper
public interface ExcludedPathConfigMapper {
    
    /**
     * 查询所有启用的排除路径配置
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> selectAllEnabled();
    
    /**
     * 根据路径和方法查询匹配的排除路径配置
     * @param path 请求路径
     * @param method HTTP方法
     * @return 匹配的排除路径配置
     */
    ExcludedPathConfig selectByPathAndMethod(@Param("path") String path, @Param("method") String method);
    
    /**
     * 根据ID查询排除路径配置
     * @param id 主键ID
     * @return 排除路径配置
     */
    ExcludedPathConfig selectById(@Param("id") Long id);
    
    /**
     * 插入排除路径配置
     * @param config 排除路径配置
     * @return 影响行数
     */
    int insert(ExcludedPathConfig config);
    
    /**
     * 更新排除路径配置
     * @param config 排除路径配置
     * @return 影响行数
     */
    int update(ExcludedPathConfig config);
    
    /**
     * 根据ID删除排除路径配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据路径模式查询排除路径配置
     * @param pathPattern 路径模式
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> selectByPathPattern(@Param("pathPattern") String pathPattern);
    
    /**
     * 根据状态查询排除路径配置
     * @param status 状态
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> selectByStatus(@Param("status") Integer status);
    
    /**
     * 分页查询排除路径配置
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 排除路径配置列表
     */
    List<ExcludedPathConfig> selectByPage(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询排除路径配置总数
     * @return 总数
     */
    int selectCount();
}
