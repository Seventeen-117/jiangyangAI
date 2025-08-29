package com.signature.controller;

import com.signature.service.ValidationCacheService;
import com.signature.service.ValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 缓存管理控制器
 * 提供缓存管理相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/cache")
public class CacheManagementController {

    @Autowired
    private ValidationCacheService cacheService;

    @Autowired
    @Qualifier("cachedValidationServiceImpl")
    private ValidationService validationService;

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        try {
            Map<String, Object> stats = cacheService.getCacheStatistics();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting cache statistics", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error getting cache statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 刷新所有验证缓存
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        try {
            validationService.refreshValidationCache();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Cache refreshed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error refreshing cache", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error refreshing cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 清理过期缓存
     */
    @PostMapping("/clean")
    public ResponseEntity<Map<String, Object>> cleanExpiredCache() {
        try {
            cacheService.cleanExpiredCache();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Expired cache cleaned successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error cleaning expired cache", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error cleaning expired cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 预热缓存
     */
    @PostMapping("/warmup")
    public ResponseEntity<Map<String, Object>> warmUpCache() {
        try {
            if (validationService instanceof com.signature.service.impl.CachedValidationServiceImpl) {
                ((com.signature.service.impl.CachedValidationServiceImpl) validationService).warmUpCache();
            }
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Cache warm-up completed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error warming up cache", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error warming up cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 清除特定类型的缓存
     */
    @DeleteMapping("/{cacheType}")
    public ResponseEntity<Map<String, Object>> clearCacheByType(@PathVariable String cacheType) {
        try {
            switch (cacheType.toLowerCase()) {
                case "apikey":
                    // 清除API Key缓存
                    break;
                case "signature":
                    // 清除签名验证缓存
                    break;
                case "permission":
                    // 清除权限验证缓存
                    break;
                case "jwt":
                    // 清除JWT缓存
                    break;
                case "excludedpaths":
                    cacheService.evictExcludedPaths();
                    break;
                default:
                    Map<String, Object> response = new HashMap<>();
                    response.put("code", 400);
                    response.put("message", "Invalid cache type: " + cacheType);
                    return ResponseEntity.badRequest().body(response);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Cache cleared successfully for type: " + cacheType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing cache for type: {}", cacheType, e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error clearing cache: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取缓存健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getCacheHealth() {
        try {
            Map<String, Object> stats = cacheService.getCacheStatistics();
            Map<String, Object> health = new HashMap<>();
            
            // 检查缓存是否正常工作
            boolean isHealthy = stats.containsKey("apiKeyCount") && 
                              stats.containsKey("signatureCount") && 
                              stats.containsKey("permissionCount");
            
            health.put("status", isHealthy ? "HEALTHY" : "UNHEALTHY");
            health.put("timestamp", System.currentTimeMillis());
            health.put("statistics", stats);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", health);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting cache health", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "Error getting cache health: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
