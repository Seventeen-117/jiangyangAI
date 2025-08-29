package com.signature.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 排除路径配置实体类
 * 用于替代UnifiedSecurityFilter中的硬编码排除路径
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcludedPathConfig {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 路径模式（支持前缀匹配和精确匹配）
     */
    private String pathPattern;
    
    /**
     * 路径名称
     */
    private String pathName;
    
    /**
     * 路径类型：PREFIX-前缀匹配，EXACT-精确匹配
     */
    private String pathType;
    
    /**
     * HTTP方法（GET,POST,PUT,DELETE等，多个用逗号分隔，为空表示所有方法）
     */
    private String httpMethods;
    
    /**
     * 排除原因
     */
    private String excludeReason;
    
    /**
     * 路径描述
     */
    private String description;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    
    /**
     * 路径类型枚举
     */
    public enum PathType {
        PREFIX,  // 前缀匹配
        EXACT    // 精确匹配
    }
    
    /**
     * 检查路径是否匹配
     * @param requestPath 请求路径
     * @param requestMethod 请求方法
     * @return 是否匹配
     */
    public boolean matches(String requestPath, String requestMethod) {
        // 检查状态
        if (status != null && status != 1) {
            return false;
        }
        
        // 检查HTTP方法
        if (httpMethods != null && !httpMethods.trim().isEmpty()) {
            String[] allowedMethods = httpMethods.split(",");
            boolean methodMatched = false;
            for (String method : allowedMethods) {
                if (method.trim().equalsIgnoreCase(requestMethod)) {
                    methodMatched = true;
                    break;
                }
            }
            if (!methodMatched) {
                return false;
            }
        }
        
        // 检查路径匹配
        if (PathType.EXACT.name().equals(pathType)) {
            return pathPattern.equals(requestPath);
        } else {
            // 默认前缀匹配
            return requestPath.startsWith(pathPattern);
        }
    }
}
