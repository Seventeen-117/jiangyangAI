package com.signature.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.signature.entity.*;
import com.signature.service.*;
import com.signature.service.DynamicConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;

/**
 * 配置管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private ConfigCategoryService configCategoryService;

    @Autowired
    private PathConfigService pathConfigService;

    @Autowired
    private InternalServiceConfigService internalServiceConfigService;

    @Autowired
    private ValidationRuleConfigService validationRuleConfigService;

    @Autowired
    private CacheConfigService cacheConfigService;

    @Autowired
    private DynamicConfigService dynamicConfigService;

    // ==================== 配置分类管理 ====================

    /**
     * 获取配置分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) Integer status) {
        
        Page<ConfigCategory> pageParam = new Page<>(page, size);
        QueryWrapper<ConfigCategory> queryWrapper = new QueryWrapper<>();
        
        if (categoryCode != null && !categoryCode.isEmpty()) {
            queryWrapper.like("category_code", categoryCode);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByAsc("sort_order", "id");
        
        Page<ConfigCategory> result = configCategoryService.page(pageParam, queryWrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取配置分类详情
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<ConfigCategory> getCategory(@PathVariable Long id) {
        ConfigCategory category = configCategoryService.getById(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }

    /**
     * 创建配置分类
     */
    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody ConfigCategory category) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证必需字段
        if (category.getCategoryCode() == null || category.getCategoryCode().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "category_code is required");
            response.put("field", "categoryCode");
            return ResponseEntity.badRequest().body(response);
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "category_name is required");
            response.put("field", "categoryName");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置默认值
            if (category.getStatus() == null) {
                category.setStatus(1); // 默认启用
            }
            if (category.getSortOrder() == null) {
                category.setSortOrder(0); // 默认排序
            }
            
            // 保存配置分类
            configCategoryService.save(category);
            
            response.put("success", true);
            response.put("message", "Category created successfully");
            response.put("data", category);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to create category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新配置分类
     */
    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody ConfigCategory category) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证分类是否存在
        ConfigCategory existingCategory = configCategoryService.getById(id);
        if (existingCategory == null) {
            response.put("success", false);
            response.put("error", "NOT_FOUND");
            response.put("message", "Category not found with id: " + id);
            return ResponseEntity.notFound().build();
        }
        
        // 验证必需字段
        if (category.getCategoryCode() == null || category.getCategoryCode().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "category_code is required");
            response.put("field", "categoryCode");
            return ResponseEntity.badRequest().body(response);
        }
        if (category.getCategoryName() == null || category.getCategoryName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "category_name is required");
            response.put("field", "categoryName");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置ID和默认值
            category.setId(id);
            if (category.getStatus() == null) {
                category.setStatus(existingCategory.getStatus());
            }
            if (category.getSortOrder() == null) {
                category.setSortOrder(existingCategory.getSortOrder());
            }
            
            // 更新配置分类
            configCategoryService.updateById(category);
            
            response.put("success", true);
            response.put("message", "Category updated successfully");
            response.put("data", category);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to update category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除配置分类
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        configCategoryService.removeById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== 路径配置管理 ====================

    /**
     * 获取路径配置列表
     */
    @GetMapping("/paths")
    public ResponseEntity<Map<String, Object>> getPaths(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String pathType,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        
        Page<PathConfig> pageParam = new Page<>(page, size);
        QueryWrapper<PathConfig> queryWrapper = new QueryWrapper<>();
        
        if (pathType != null && !pathType.isEmpty()) {
            queryWrapper.eq("path_type", pathType);
        }
        if (categoryId != null) {
            queryWrapper.eq("category_id", categoryId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByAsc("sort_order", "id");
        
        Page<PathConfig> result = pathConfigService.page(pageParam, queryWrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取路径配置详情
     */
    @GetMapping("/paths/{id}")
    public ResponseEntity<PathConfig> getPath(@PathVariable Long id) {
        PathConfig path = pathConfigService.getById(id);
        if (path == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(path);
    }

    /**
     * 创建路径配置
     */
    @PostMapping("/paths")
    public ResponseEntity<Map<String, Object>> createPath(@RequestBody PathConfig path) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证必需字段
        if (path.getCategoryId() == null) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "category_id is required");
            response.put("field", "categoryId");
            return ResponseEntity.badRequest().body(response);
        }
        if (path.getPathPattern() == null || path.getPathPattern().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "path_pattern is required");
            response.put("field", "pathPattern");
            return ResponseEntity.badRequest().body(response);
        }
        if (path.getPathName() == null || path.getPathName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "path_name is required");
            response.put("field", "pathName");
            return ResponseEntity.badRequest().body(response);
        }
        if (path.getPathType() == null || path.getPathType().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "path_type is required");
            response.put("field", "pathType");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置默认值
            if (path.getStatus() == null) {
                path.setStatus(1); // 默认启用
            }
            if (path.getSortOrder() == null) {
                path.setSortOrder(0); // 默认排序
            }
            if (path.getHttpMethods() == null || path.getHttpMethods().trim().isEmpty()) {
                path.setHttpMethods("GET,POST,PUT,DELETE"); // 默认HTTP方法
            }
            
            // 保存路径配置
            pathConfigService.save(path);
            
            response.put("success", true);
            response.put("message", "Path created successfully");
            response.put("data", path);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to create path: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新路径配置
     */
    @PutMapping("/paths/{id}")
    public ResponseEntity<Map<String, Object>> updatePath(@PathVariable Long id, @RequestBody PathConfig path) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证路径是否存在
        PathConfig existingPath = pathConfigService.getById(id);
        if (existingPath == null) {
            response.put("success", false);
            response.put("error", "NOT_FOUND");
            response.put("message", "Path not found with id: " + id);
            return ResponseEntity.notFound().build();
        }
        
        // 验证必需字段
        if (path.getCategoryId() == null) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "category_id is required");
            response.put("field", "categoryId");
            return ResponseEntity.badRequest().body(response);
        }
        if (path.getPathPattern() == null || path.getPathPattern().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "path_pattern is required");
            response.put("field", "pathPattern");
            return ResponseEntity.badRequest().body(response);
        }
        if (path.getPathName() == null || path.getPathName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "path_name is required");
            response.put("field", "pathName");
            return ResponseEntity.badRequest().body(response);
        }
        if (path.getPathType() == null || path.getPathType().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "path_type is required");
            response.put("field", "pathType");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置ID和默认值
            path.setId(id);
            if (path.getStatus() == null) {
                path.setStatus(existingPath.getStatus());
            }
            if (path.getSortOrder() == null) {
                path.setSortOrder(existingPath.getSortOrder());
            }
            if (path.getHttpMethods() == null || path.getHttpMethods().trim().isEmpty()) {
                path.setHttpMethods(existingPath.getHttpMethods());
            }
            
            // 更新路径配置
            pathConfigService.updateById(path);
            
            response.put("success", true);
            response.put("message", "Path updated successfully");
            response.put("data", path);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to update path: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除路径配置
     */
    @DeleteMapping("/paths/{id}")
    public ResponseEntity<Void> deletePath(@PathVariable Long id) {
        pathConfigService.removeById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== 内部服务配置管理 ====================

    /**
     * 获取内部服务配置列表
     */
    @GetMapping("/services")
    public ResponseEntity<Map<String, Object>> getServices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) Integer status) {
        
        Page<InternalServiceConfig> pageParam = new Page<>(page, size);
        QueryWrapper<InternalServiceConfig> queryWrapper = new QueryWrapper<>();
        
        if (serviceType != null && !serviceType.isEmpty()) {
            queryWrapper.eq("service_type", serviceType);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByAsc("sort_order", "id");
        
        Page<InternalServiceConfig> result = internalServiceConfigService.page(pageParam, queryWrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取内部服务配置详情
     */
    @GetMapping("/services/{id}")
    public ResponseEntity<InternalServiceConfig> getService(@PathVariable Long id) {
        InternalServiceConfig service = internalServiceConfigService.getById(id);
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service);
    }

    /**
     * 创建内部服务配置
     */
    @PostMapping("/services")
    public ResponseEntity<Map<String, Object>> createService(@RequestBody InternalServiceConfig service) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证必需字段
        if (service.getServiceCode() == null || service.getServiceCode().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "service_code is required");
            response.put("field", "serviceCode");
            return ResponseEntity.badRequest().body(response);
        }
        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "service_name is required");
            response.put("field", "serviceName");
            return ResponseEntity.badRequest().body(response);
        }
        if (service.getServiceType() == null || service.getServiceType().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "service_type is required");
            response.put("field", "serviceType");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置默认值
            if (service.getStatus() == null) {
                service.setStatus(1); // 默认启用
            }
            if (service.getSortOrder() == null) {
                service.setSortOrder(0); // 默认排序
            }
            
            // 保存服务配置
            internalServiceConfigService.save(service);
            
            response.put("success", true);
            response.put("message", "Service created successfully");
            response.put("data", service);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to create service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新内部服务配置
     */
    @PutMapping("/services/{id}")
    public ResponseEntity<Map<String, Object>> updateService(@PathVariable Long id, @RequestBody InternalServiceConfig service) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证服务是否存在
        InternalServiceConfig existingService = internalServiceConfigService.getById(id);
        if (existingService == null) {
            response.put("success", false);
            response.put("error", "NOT_FOUND");
            response.put("message", "Service not found with id: " + id);
            return ResponseEntity.notFound().build();
        }
        
        // 验证必需字段
        if (service.getServiceCode() == null || service.getServiceCode().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "service_code is required");
            response.put("field", "serviceCode");
            return ResponseEntity.badRequest().body(response);
        }
        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "service_name is required");
            response.put("field", "serviceName");
            return ResponseEntity.badRequest().body(response);
        }
        if (service.getServiceType() == null || service.getServiceType().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "service_type is required");
            response.put("field", "serviceType");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置ID和默认值
            service.setId(id);
            if (service.getStatus() == null) {
                service.setStatus(existingService.getStatus());
            }
            if (service.getSortOrder() == null) {
                service.setSortOrder(existingService.getSortOrder());
            }
            
            // 更新服务配置
            internalServiceConfigService.updateById(service);
            
            response.put("success", true);
            response.put("message", "Service updated successfully");
            response.put("data", service);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to update service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除内部服务配置
     */
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        internalServiceConfigService.removeById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== 验证规则配置管理 ====================

    /**
     * 获取验证规则配置列表
     */
    @GetMapping("/rules")
    public ResponseEntity<Map<String, Object>> getRules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) Integer status) {
        
        Page<ValidationRuleConfig> pageParam = new Page<>(page, size);
        QueryWrapper<ValidationRuleConfig> queryWrapper = new QueryWrapper<>();
        
        if (ruleType != null && !ruleType.isEmpty()) {
            queryWrapper.eq("rule_type", ruleType);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByAsc("sort_order", "id");
        
        Page<ValidationRuleConfig> result = validationRuleConfigService.page(pageParam, queryWrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取验证规则配置详情
     */
    @GetMapping("/rules/{id}")
    public ResponseEntity<ValidationRuleConfig> getRule(@PathVariable Long id) {
        ValidationRuleConfig rule = validationRuleConfigService.getById(id);
        if (rule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rule);
    }

    /**
     * 创建验证规则配置
     */
    @PostMapping("/rules")
    public ResponseEntity<Map<String, Object>> createRule(@RequestBody ValidationRuleConfig rule) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证必需字段
        if (rule.getRuleCode() == null || rule.getRuleCode().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "rule_code is required");
            response.put("field", "ruleCode");
            return ResponseEntity.badRequest().body(response);
        }
        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "rule_name is required");
            response.put("field", "ruleName");
            return ResponseEntity.badRequest().body(response);
        }
        if (rule.getRuleType() == null || rule.getRuleType().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "rule_type is required");
            response.put("field", "ruleType");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置默认值
            if (rule.getEnabled() == null) {
                rule.setEnabled(1); // 默认启用
            }
            if (rule.getStrictMode() == null) {
                rule.setStrictMode(0); // 默认非严格模式
            }
            if (rule.getTimeoutMs() == null) {
                rule.setTimeoutMs(5000); // 默认5秒超时
            }
            if (rule.getRetryCount() == null) {
                rule.setRetryCount(3); // 默认重试3次
            }
            if (rule.getRetryIntervalMs() == null) {
                rule.setRetryIntervalMs(1000); // 默认1秒重试间隔
            }
            if (rule.getStatus() == null) {
                rule.setStatus(1); // 默认启用
            }
            if (rule.getSortOrder() == null) {
                rule.setSortOrder(0); // 默认排序
            }
            
            // 保存规则配置
            validationRuleConfigService.save(rule);
            
            response.put("success", true);
            response.put("message", "Rule created successfully");
            response.put("data", rule);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to create rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新验证规则配置
     */
    @PutMapping("/rules/{id}")
    public ResponseEntity<Map<String, Object>> updateRule(@PathVariable Long id, @RequestBody ValidationRuleConfig rule) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证规则是否存在
        ValidationRuleConfig existingRule = validationRuleConfigService.getById(id);
        if (existingRule == null) {
            response.put("success", false);
            response.put("error", "NOT_FOUND");
            response.put("message", "Rule not found with id: " + id);
            return ResponseEntity.notFound().build();
        }
        
        // 验证必需字段
        if (rule.getRuleCode() == null || rule.getRuleCode().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "rule_code is required");
            response.put("field", "ruleCode");
            return ResponseEntity.badRequest().body(response);
        }
        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "rule_name is required");
            response.put("field", "ruleName");
            return ResponseEntity.badRequest().body(response);
        }
        if (rule.getRuleType() == null || rule.getRuleType().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "rule_type is required");
            response.put("field", "ruleType");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置ID和默认值
            rule.setId(id);
            if (rule.getEnabled() == null) {
                rule.setEnabled(existingRule.getEnabled());
            }
            if (rule.getStrictMode() == null) {
                rule.setStrictMode(existingRule.getStrictMode());
            }
            if (rule.getTimeoutMs() == null) {
                rule.setTimeoutMs(existingRule.getTimeoutMs());
            }
            if (rule.getRetryCount() == null) {
                rule.setRetryCount(existingRule.getRetryCount());
            }
            if (rule.getRetryIntervalMs() == null) {
                rule.setRetryIntervalMs(existingRule.getRetryIntervalMs());
            }
            if (rule.getStatus() == null) {
                rule.setStatus(existingRule.getStatus());
            }
            if (rule.getSortOrder() == null) {
                rule.setSortOrder(existingRule.getSortOrder());
            }
            
            // 更新规则配置
            validationRuleConfigService.updateById(rule);
            
            response.put("success", true);
            response.put("message", "Rule updated successfully");
            response.put("data", rule);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to update rule: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除验证规则配置
     */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        validationRuleConfigService.removeById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== 缓存配置管理 ====================

    /**
     * 获取缓存配置列表
     */
    @GetMapping("/caches")
    public ResponseEntity<Map<String, Object>> getCaches(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String cacheType,
            @RequestParam(required = false) Integer status) {
        
        Page<CacheConfig> pageParam = new Page<>(page, size);
        QueryWrapper<CacheConfig> queryWrapper = new QueryWrapper<>();
        
        if (cacheType != null && !cacheType.isEmpty()) {
            queryWrapper.eq("cache_type", cacheType);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByAsc("id");
        
        Page<CacheConfig> result = cacheConfigService.page(pageParam, queryWrapper);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getRecords());
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取缓存配置详情
     */
    @GetMapping("/caches/{id}")
    public ResponseEntity<CacheConfig> getCache(@PathVariable Long id) {
        CacheConfig cache = cacheConfigService.getById(id);
        if (cache == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cache);
    }

    /**
     * 创建缓存配置
     */
    @PostMapping("/caches")
    public ResponseEntity<Map<String, Object>> createCache(@RequestBody CacheConfig cache) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证必需字段
        if (cache.getCacheKey() == null || cache.getCacheKey().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "cache_key is required");
            response.put("field", "cacheKey");
            return ResponseEntity.badRequest().body(response);
        }
        if (cache.getCacheName() == null || cache.getCacheName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "cache_name is required");
            response.put("field", "cacheName");
            return ResponseEntity.badRequest().body(response);
        }
        if (cache.getCacheType() == null || cache.getCacheType().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "cache_type is required");
            response.put("field", "cacheType");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置默认值
            if (cache.getExpireSeconds() == null) {
                cache.setExpireSeconds(3600); // 默认1小时过期
            }
            if (cache.getMaxSize() == null) {
                cache.setMaxSize(1000); // 默认最大1000个缓存项
            }
            if (cache.getStatus() == null) {
                cache.setStatus(1); // 默认启用
            }
            
            // 保存缓存配置
            cacheConfigService.save(cache);
            
            response.put("success", true);
            response.put("message", "Cache created successfully");
            response.put("data", cache);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to create cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新缓存配置
     */
    @PutMapping("/caches/{id}")
    public ResponseEntity<Map<String, Object>> updateCache(@PathVariable Long id, @RequestBody CacheConfig cache) {
        Map<String, Object> response = new HashMap<>();
        
        // 验证缓存是否存在
        CacheConfig existingCache = cacheConfigService.getById(id);
        if (existingCache == null) {
            response.put("success", false);
            response.put("error", "NOT_FOUND");
            response.put("message", "Cache not found with id: " + id);
            return ResponseEntity.notFound().build();
        }
        
        // 验证必需字段
        if (cache.getCacheKey() == null || cache.getCacheKey().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "cache_key is required");
            response.put("field", "cacheKey");
            return ResponseEntity.badRequest().body(response);
        }
        if (cache.getCacheName() == null || cache.getCacheName().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "cache_name is required");
            response.put("field", "cacheName");
            return ResponseEntity.badRequest().body(response);
        }
        if (cache.getCacheType() == null || cache.getCacheType().trim().isEmpty()) {
            response.put("success", false);
            response.put("error", "VALIDATION_ERROR");
            response.put("message", "cache_type is required");
            response.put("field", "cacheType");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // 设置ID和默认值
            cache.setId(id);
            if (cache.getExpireSeconds() == null) {
                cache.setExpireSeconds(existingCache.getExpireSeconds());
            }
            if (cache.getMaxSize() == null) {
                cache.setMaxSize(existingCache.getMaxSize());
            }
            if (cache.getStatus() == null) {
                cache.setStatus(existingCache.getStatus());
            }
            
            // 更新缓存配置
            cacheConfigService.updateById(cache);
            
            response.put("success", true);
            response.put("message", "Cache updated successfully");
            response.put("data", cache);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "INTERNAL_ERROR");
            response.put("message", "Failed to update cache: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除缓存配置
     */
    @DeleteMapping("/caches/{id}")
    public ResponseEntity<Void> deleteCache(@PathVariable Long id) {
        cacheConfigService.removeById(id);
        return ResponseEntity.ok().build();
    }

    // ==================== 动态配置查询 ====================

    /**
     * 获取排除路径列表
     */
    @GetMapping("/dynamic/excluded-paths")
    public ResponseEntity<List<String>> getExcludedPaths() {
        return ResponseEntity.ok(dynamicConfigService.getExcludedPaths());
    }

    /**
     * 获取严格验证路径列表
     */
    @GetMapping("/dynamic/strict-paths")
    public ResponseEntity<List<String>> getStrictPaths() {
        return ResponseEntity.ok(dynamicConfigService.getStrictValidationPaths());
    }

    /**
     * 获取内部路径列表
     */
    @GetMapping("/dynamic/internal-paths")
    public ResponseEntity<List<String>> getInternalPaths() {
        return ResponseEntity.ok(dynamicConfigService.getInternalPaths());
    }

    /**
     * 获取内部服务列表
     */
    @GetMapping("/dynamic/services")
    public ResponseEntity<List<InternalServiceConfig>> getInternalServices() {
        return ResponseEntity.ok(dynamicConfigService.getInternalServices());
    }

    /**
     * 根据服务编码获取内部服务
     */
    @GetMapping("/dynamic/services/code/{serviceCode}")
    public ResponseEntity<InternalServiceConfig> getInternalServiceByCode(@PathVariable String serviceCode) {
        InternalServiceConfig service = dynamicConfigService.getInternalServiceByCode(serviceCode);
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service);
    }

    /**
     * 根据API密钥获取内部服务
     */
    @GetMapping("/dynamic/services/api-key/{apiKey}")
    public ResponseEntity<InternalServiceConfig> getInternalServiceByApiKey(@PathVariable String apiKey) {
        InternalServiceConfig service = dynamicConfigService.getInternalServiceByApiKey(apiKey);
        if (service == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service);
    }

    /**
     * 检查路径是否匹配指定类型
     */
    @GetMapping("/dynamic/check-path")
    public ResponseEntity<Map<String, Object>> checkPath(
            @RequestParam String path,
            @RequestParam String pathType) {
        
        boolean isMatch = dynamicConfigService.isPathMatchType(path, pathType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("path", path);
        response.put("pathType", pathType);
        response.put("isMatch", isMatch);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 检查是否为内部服务调用
     */
    @GetMapping("/dynamic/check-internal-service")
    public ResponseEntity<Map<String, Object>> checkInternalService(
            @RequestParam String apiKey,
            @RequestParam(required = false) String requestFrom) {
        
        boolean isInternal = dynamicConfigService.isInternalServiceCall(apiKey, requestFrom);
        
        Map<String, Object> response = new HashMap<>();
        response.put("apiKey", apiKey);
        response.put("requestFrom", requestFrom);
        response.put("isInternal", isInternal);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 刷新配置缓存
     */
    @PostMapping("/dynamic/refresh-cache")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        dynamicConfigService.refreshConfigCache();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "配置缓存刷新成功");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取配置统计信息
     */
    @GetMapping("/dynamic/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(dynamicConfigService.getConfigStatistics());
    }
}
