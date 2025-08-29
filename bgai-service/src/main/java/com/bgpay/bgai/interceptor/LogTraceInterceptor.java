package com.bgpay.bgai.interceptor;

import com.bgpay.bgai.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 日志追踪过滤器，用于记录请求的追踪信息（WebFlux）
 */
@Component
public class LogTraceInterceptor implements WebFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(LogTraceInterceptor.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 为每个请求创建一个新的追踪ID
        LogUtils.startTrace();
        
        // 记录请求的基本信息
        String requestURI = request.getURI().getPath();
        String method = request.getMethod().name();
        String remoteAddr = request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        
        // 记录请求详情
        logger.debug("Processing request: {} {} from {}", method, requestURI, remoteAddr);
        
        // 从请求属性中获取用户ID (通常由认证过滤器设置)
        Object userId = exchange.getAttribute("userId");
        if (userId != null) {
            LogUtils.setUserId(userId.toString());
            logger.debug("Request from user: {}", userId);
        }
        
        // 继续处理请求
        return chain.filter(exchange)
            .doOnSuccess(v -> {
                // 请求处理完成后，记录响应状态
                logger.debug("Completed request with status: {}", response.getStatusCode());
            })
            .doOnError(ex -> {
                // 如果有异常发生，记录异常信息
                logger.error("Request processing failed", ex);
            })
            .doFinally(signalType -> {
                // 请求完全处理完毕，清理追踪上下文
                LogUtils.clearTrace();
            });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100; // 确保在其他过滤器之后执行
    }
} 