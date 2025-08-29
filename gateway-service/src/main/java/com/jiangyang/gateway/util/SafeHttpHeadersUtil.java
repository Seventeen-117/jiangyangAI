package com.jiangyang.gateway.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HTTP头部安全处理工具类
 * 用于安全地将HTTP头部转换为字符串，避免Tomcat头部适配器的空指针异常
 * 
 * @author jiangyang
 * @since 2024-01-01
 */
@Slf4j
public class SafeHttpHeadersUtil {

    private static final String NULL_HEADER_PLACEHOLDER = "[NULL]";
    private static final String PARSE_ERROR_PLACEHOLDER = "[解析异常]";
    private static final String UNKNOWN_IP = "unknown";

    /**
     * 安全地将HTTP头转换为字符串，避免Tomcat头适配器的空指针异常
     * 
     * @param headers HTTP头部对象
     * @return 安全的头部字符串表示
     */
    public static String safeHeadersToString(HttpHeaders headers) {
        if (headers == null) {
            return "{}";
        }
        
        try {
            // 使用安全的方式遍历头部信息
            Map<String, String> safeHeaders = headers.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                try {
                                    List<String> values = entry.getValue();
                                    if (values == null || values.isEmpty()) {
                                        return NULL_HEADER_PLACEHOLDER;
                                    }
                                    return String.join(",", values);
                                } catch (Exception e) {
                                    log.debug("解析头部值时发生异常: {} - {}", entry.getKey(), e.getMessage());
                                    return PARSE_ERROR_PLACEHOLDER;
                                }
                            },
                            (existing, replacement) -> existing
                    ));
            return safeHeaders.toString();
        } catch (Exception e) {
            log.debug("解析HTTP头部时发生异常: {}", e.getMessage());
            return "{头部解析异常: " + e.getClass().getSimpleName() + "}";
        }
    }

    /**
     * 安全地获取客户端IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIp(ServerHttpRequest request) {
        try {
            // 优先使用X-Forwarded-For头（代理场景）
            String xForwardedFor = safeGetHeader(request, "X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            // 使用X-Real-IP头
            String xRealIp = safeGetHeader(request, "X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            // 使用远程地址
            if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
                return request.getRemoteAddress().getAddress().getHostAddress();
            }
            
            return UNKNOWN_IP;
        } catch (Exception e) {
            log.debug("获取客户端IP时发生异常: {}", e.getMessage());
            return UNKNOWN_IP;
        }
    }

    /**
     * 安全地获取指定名称的头部值
     * 
     * @param request HTTP请求对象
     * @param headerName 头部名称
     * @return 头部值，如果不存在或发生异常则返回null
     */
    public static String safeGetHeader(ServerHttpRequest request, String headerName) {
        try {
            if (request == null || request.getHeaders() == null || headerName == null) {
                return null;
            }
            return request.getHeaders().getFirst(headerName);
        } catch (Exception e) {
            log.debug("获取头部 {} 时发生异常: {}", headerName, e.getMessage());
            return null;
        }
    }

    /**
     * 安全地获取User-Agent头部
     * 
     * @param request HTTP请求对象
     * @return User-Agent值，如果不存在则返回空字符串
     */
    public static String getUserAgent(ServerHttpRequest request) {
        String userAgent = safeGetHeader(request, "User-Agent");
        return userAgent != null ? userAgent : "";
    }

    /**
     * 获取请求的基本信息，用于日志记录
     * 
     * @param request HTTP请求对象
     * @return 包含基本请求信息的字符串
     */
    public static String getRequestBasicInfo(ServerHttpRequest request) {
        try {
            StringBuilder info = new StringBuilder();
            info.append("Method=").append(request.getMethod())
                .append(", Path=").append(request.getPath())
                .append(", IP=").append(getClientIp(request));
            
            String userAgent = getUserAgent(request);
            if (!userAgent.isEmpty()) {
                info.append(", UserAgent=").append(userAgent.length() > 50 ? 
                    userAgent.substring(0, 50) + "..." : userAgent);
            }
            
            return info.toString();
        } catch (Exception e) {
            log.debug("获取请求基本信息时发生异常: {}", e.getMessage());
            return "RequestInfo=[解析异常]";
        }
    }

    /**
     * 检查是否为敏感头部，需要在日志中隐藏
     * 
     * @param headerName 头部名称
     * @return 如果是敏感头部返回true
     */
    public static boolean isSensitiveHeader(String headerName) {
        if (headerName == null) {
            return false;
        }
        
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") ||
               lowerName.contains("token") ||
               lowerName.contains("password") ||
               lowerName.contains("secret") ||
               lowerName.contains("key");
    }

    /**
     * 安全地将HTTP头转换为字符串，隐藏敏感信息
     * 
     * @param headers HTTP头部对象
     * @return 隐藏敏感信息的头部字符串表示
     */
    public static String safeHeadersToStringWithSensitiveFilter(HttpHeaders headers) {
        if (headers == null) {
            return "{}";
        }
        
        try {
            Map<String, String> filteredHeaders = headers.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> {
                                try {
                                    if (isSensitiveHeader(entry.getKey())) {
                                        return "[HIDDEN]";
                                    }
                                    
                                    List<String> values = entry.getValue();
                                    if (values == null || values.isEmpty()) {
                                        return NULL_HEADER_PLACEHOLDER;
                                    }
                                    return String.join(",", values);
                                } catch (Exception e) {
                                    log.debug("解析头部值时发生异常: {} - {}", entry.getKey(), e.getMessage());
                                    return PARSE_ERROR_PLACEHOLDER;
                                }
                            },
                            (existing, replacement) -> existing
                    ));
            return filteredHeaders.toString();
        } catch (Exception e) {
            log.debug("解析HTTP头部时发生异常: {}", e.getMessage());
            return "{头部解析异常: " + e.getClass().getSimpleName() + "}";
        }
    }
}