package com.jiangyang.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局日志过滤器
 * 记录所有请求和响应的日志信息
 *
 * @author jiangyang
 * @since 2024-01-01
 */
@Component
public class GlobalLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(GlobalLogFilter.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = generateRequestId();

        // 记录请求开始时间
        long startTime = System.currentTimeMillis();

        // 记录请求日志，使用try-catch保护
        try {
            log.info("=== 网关请求开始 ===");
            log.info("请求ID: {}", requestId);
            log.info("请求时间: {}", LocalDateTime.now().format(FORMATTER));
            log.info("请求方法: {}", request.getMethod());
            log.info("请求路径: {}", request.getPath());
            log.info("请求URI: {}", request.getURI());
            log.info("请求头: {}", safeHeadersToString(request.getHeaders()));
            log.info("客户端IP: {}", getClientIp(request));
        } catch (Exception e) {
            log.warn("记录请求日志时发生异常: {}", e.getMessage());
        }

        // 添加请求ID到请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-Request-ID", requestId)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                .doFinally(signalType -> {
                    // 记录响应日志，使用try-catch保护，确保日志错误不影响请求处理
                    try {
                        long endTime = System.currentTimeMillis();
                        long duration = endTime - startTime;

                        log.info("=== 网关请求结束 ===");
                        log.info("请求ID: {}", requestId);
                        log.info("响应时间: {}", LocalDateTime.now().format(FORMATTER));
                        log.info("处理耗时: {}ms", duration);
                        log.info("响应状态: {}", exchange.getResponse().getStatusCode());
                        log.info("响应头: {}", safeHeadersToString(exchange.getResponse().getHeaders()));
                    } catch (Exception e) {
                        log.warn("记录响应日志时发生异常: {}", e.getMessage());
                    }
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "GW-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    /**
     * 安全地将HTTP头转换为字符串，避免Tomcat头适配器的空指针异常
     */
    private String safeHeadersToString(HttpHeaders headers) {
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
                                    return String.join(",", entry.getValue());
                                } catch (Exception e) {
                                    return "[解析异常]";
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
     * 获取客户端IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        try {
            String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeaders().getFirst("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddress() != null ?
                   request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        } catch (Exception e) {
            log.debug("获取客户端IP时发生异常: {}", e.getMessage());
            return "unknown";
        }
    }
}