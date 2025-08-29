package com.signature.controller;

import com.signature.entity.ExcludedPathConfig;
import com.signature.service.ExcludedPathService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排除路径配置管理控制器
 * 提供排除路径配置的CRUD操作
 */
@Slf4j
@RestController
@RequestMapping("/api/excluded-paths")
public class ExcludedPathController {

    @Autowired
    private ExcludedPathService excludedPathService;

    /**
     * 获取所有启用的排除路径配置
     * @return 排除路径配置列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllConfigs() {
        try {
            List<ExcludedPathConfig> configs = excludedPathService.getAllEnabledConfigs();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", configs);
            response.put("total", configs.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting all excluded path configs", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 分页获取排除路径配置
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getConfigsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<ExcludedPathConfig> configs = excludedPathService.getConfigsByPage(page, size);
            int total = excludedPathService.getConfigCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", configs);
            response.put("total", total);
            response.put("page", page);
            response.put("size", size);
            response.put("pages", (total + size - 1) / size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting excluded path configs by page", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据ID获取排除路径配置
     * @param id 配置ID
     * @return 排除路径配置
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getConfigById(@PathVariable Long id) {
        try {
            ExcludedPathConfig config = excludedPathService.getById(id);
            if (config == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "Excluded path config not found");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", config);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting excluded path config by id: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 添加排除路径配置
     * @param config 排除路径配置
     * @return 操作结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addConfig(@RequestBody ExcludedPathConfig config) {
        try {
            // 设置默认值
            if (config.getStatus() == null) {
                config.setStatus(1);
            }
            if (config.getSortOrder() == null) {
                config.setSortOrder(0);
            }
            if (config.getPathType() == null) {
                config.setPathType("PREFIX");
            }
            
            boolean success = excludedPathService.addConfig(config);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("message", "Excluded path config added successfully");
                response.put("data", config);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "Failed to add excluded path config");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error adding excluded path config", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 更新排除路径配置
     * @param id 配置ID
     * @param config 排除路径配置
     * @return 操作结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable Long id, 
            @RequestBody ExcludedPathConfig config) {
        try {
            config.setId(id);
            boolean success = excludedPathService.updateConfig(config);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("message", "Excluded path config updated successfully");
                response.put("data", config);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 400);
                response.put("message", "Failed to update excluded path config");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error updating excluded path config: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除排除路径配置
     * @param id 配置ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteConfig(@PathVariable Long id) {
        try {
            boolean success = excludedPathService.deleteConfig(id);
            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("message", "Excluded path config deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 404);
                response.put("message", "Excluded path config not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting excluded path config: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 刷新排除路径缓存
     * @return 操作结果
     */
    @PostMapping("/refresh-cache")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        try {
            excludedPathService.refreshCache();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Cache refreshed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error refreshing cache", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据路径模式查询配置
     * @param pathPattern 路径模式
     * @return 排除路径配置列表
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchByPathPattern(
            @RequestParam String pathPattern) {
        try {
            List<ExcludedPathConfig> configs = excludedPathService.getConfigsByPathPattern(pathPattern);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", configs);
            response.put("total", configs.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching excluded path configs by pattern: {}", pathPattern, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 根据状态查询配置
     * @param status 状态
     * @return 排除路径配置列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getConfigsByStatus(@PathVariable Integer status) {
        try {
            List<ExcludedPathConfig> configs = excludedPathService.getConfigsByStatus(status);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", configs);
            response.put("total", configs.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting excluded path configs by status: {}", status, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Internal server error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
