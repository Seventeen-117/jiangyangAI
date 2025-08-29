package com.jiangyang.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 全局请求日志过滤器：打印每次请求的请求头与请求体
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final int MAX_LOG_BODY = 10 * 1024; // 最多记录 10KB

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();

        // 打印请求行与请求头
        if (log.isInfoEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("REQ ").append(method).append(" ").append(request.getURI());
            HttpHeaders headers = request.getHeaders();
            headers.forEach((k, v) -> sb.append("\n  ").append(k).append(": ").append(String.join(",", v)));
            log.info(sb.toString());
        }

        // 仅对有请求体的方法尝试记录 Body（JSON/文本类）
        boolean mayHaveBody = method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
        MediaType contentType = request.getHeaders().getContentType();
        boolean loggableContent = contentType != null && (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
                || MediaType.TEXT_PLAIN.isCompatibleWith(contentType)
                || MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType));

        if (!mayHaveBody || !loggableContent) {
            return chain.filter(exchange);
        }

        return DataBufferUtils.join(request.getBody())
                .flatMap(dataBuffer -> {
                    if (dataBuffer == null) {
                        return chain.filter(exchange);
                    }
                    
                    try {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        String bodyStr = new String(bytes, StandardCharsets.UTF_8);
                        if (bodyStr.length() > MAX_LOG_BODY) {
                            bodyStr = bodyStr.substring(0, MAX_LOG_BODY) + "... (truncated)";
                        }
                        log.info("REQ BODY: {}", bodyStr);

                        // 重新包装请求体，继续向下游传递
                        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                        Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                            DataBuffer buffer = bufferFactory.wrap(bytes);
                            return Mono.just(buffer);
                        });

                        ServerHttpRequest decorated = new ServerHttpRequestDecorator(request) {
                            @Override
                            public Flux<DataBuffer> getBody() {
                                return cachedFlux;
                            }
                        };
                        return chain.filter(exchange.mutate().request(decorated).build());
                    } catch (Exception e) {
                        log.warn("处理请求体时发生错误: {}", e.getMessage());
                        DataBufferUtils.release(dataBuffer);
                        return chain.filter(exchange);
                    }
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // 在DefensiveFilter之前执行，避免影响响应头
        return -50;
    }
}


