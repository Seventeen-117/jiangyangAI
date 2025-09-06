#!/bin/bash
# 微服务性能优化脚本
# 目标：将响应时间优化到10毫秒以内

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Java版本
check_java_version() {
    log_info "检查Java版本..."
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 17 ]; then
            log_success "Java版本: $JAVA_VERSION (支持ZGC)"
        else
            log_warning "Java版本: $JAVA_VERSION (建议升级到17+以使用ZGC)"
        fi
    else
        log_error "未找到Java，请先安装Java"
        exit 1
    fi
}

# 检查Docker
check_docker() {
    log_info "检查Docker..."
    if command -v docker &> /dev/null; then
        log_success "Docker已安装"
    else
        log_error "未找到Docker，请先安装Docker"
        exit 1
    fi
}

# 优化JVM参数
optimize_jvm() {
    log_info "优化JVM参数..."
    
    # 创建JVM优化配置
    cat > jvm-optimization.conf << EOF
# JVM性能优化配置
# 目标：将响应时间优化到10毫秒以内

# 生产环境JVM参数
JAVA_OPTS_PRODUCTION="-server -XX:+UseZGC -XX:+UseLargePages -XX:+UseTransparentHugePages -Xms1g -Xmx2g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseNUMAInterleaving -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+OptimizeStringConcat -XX:+UseAdaptiveNUMAChunkSizing"

# 开发环境JVM参数
JAVA_OPTS_DEVELOPMENT="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=10 -Xms512m -Xmx1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8"

# 低延迟JVM参数
JAVA_OPTS_LOW_LATENCY="-server -XX:+UseZGC -XX:+UseLargePages -XX:+UseTransparentHugePages -Xms1g -Xmx2g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseNUMAInterleaving -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+OptimizeStringConcat -XX:+UseAdaptiveNUMAChunkSizing -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+OptimizeStringConcat -XX:+UseAdaptiveNUMAChunkSizing"
EOF

    log_success "JVM参数优化配置已创建"
}

# 优化Spring Boot配置
optimize_spring_boot() {
    log_info "优化Spring Boot配置..."
    
    # 创建Spring Boot优化配置
    cat > spring-boot-optimization.conf << EOF
# Spring Boot性能优化配置
# 目标：将响应时间优化到10毫秒以内

# 服务器配置优化
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
server.tomcat.max-connections=8192
server.tomcat.accept-count=100
server.tomcat.connection-timeout=20000
server.tomcat.keep-alive-timeout=15000
server.tomcat.max-keep-alive-requests=100
server.tomcat.compression.enabled=true
server.tomcat.compression.min-response-size=1024
server.tomcat.http2.enabled=true

# Spring缓存配置
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=10000,expireAfterWrite=300s,expireAfterAccess=60s,refreshAfterWrite=60s

# 数据库连接池优化
spring.datasource.type=com.zaxxer.hikari.HikariDataSource
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.validation-timeout=3000
spring.datasource.hikari.leak-detection-threshold=60000
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.read-only=false
spring.datasource.hikari.pool-name=HikariCP-Pool

# 任务调度优化
spring.task.execution.pool.core-size=8
spring.task.execution.pool.max-size=16
spring.task.execution.pool.queue-capacity=100
spring.task.execution.pool.keep-alive=60s
spring.task.scheduling.pool.size=8

# 管理端点优化
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.endpoint.metrics.enabled=true
management.health.redis.enabled=false
management.health.db.enabled=true
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
management.metrics.distribution.percentiles.http.server.requests=0.5,0.95,0.99
management.metrics.distribution.slo.http.server.requests=10ms,50ms,100ms,200ms,500ms,1s,2s,5s

# 日志配置优化
logging.level.root=INFO
logging.level.org.springframework=WARN
logging.level.org.springframework.web=WARN
logging.level.org.springframework.security=WARN
logging.level.org.apache.tomcat=WARN
logging.level.org.apache.catalina=WARN
logging.level.org.apache.coyote=WARN
logging.level.org.apache.juli=WARN
logging.level.com.zaxxer.hikari=WARN
logging.level.com.alibaba.druid=WARN
logging.level.com.jiangyang=DEBUG
EOF

    log_success "Spring Boot优化配置已创建"
}

# 优化Docker配置
optimize_docker() {
    log_info "优化Docker配置..."
    
    # 创建Docker优化配置
    cat > docker-optimization.conf << EOF
# Docker性能优化配置
# 目标：将响应时间优化到10毫秒以内

# 基础镜像优化
FROM openjdk:24-jdk-slim

# 设置工作目录
WORKDIR /app

# 设置环境变量 - 性能优化配置
ENV JAVA_OPTS="-server -XX:+UseZGC -XX:+UseLargePages -XX:+UseTransparentHugePages -Xms1g -Xmx2g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseNUMAInterleaving -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+OptimizeStringConcat -XX:+UseAdaptiveNUMAChunkSizing"
ENV SPRING_PROFILES_ACTIVE=dev
ENV TZ=Asia/Shanghai
ENV SERVER_TOMCAT_THREADS_MAX=200
ENV SERVER_TOMCAT_THREADS_MIN_SPARE=10
ENV SERVER_TOMCAT_MAX_CONNECTIONS=8192
ENV SERVER_TOMCAT_ACCEPT_COUNT=100
ENV SERVER_TOMCAT_CONNECTION_TIMEOUT=20000
ENV SERVER_TOMCAT_KEEP_ALIVE_TIMEOUT=15000
ENV SERVER_TOMCAT_MAX_KEEP_ALIVE_REQUESTS=100
ENV SERVER_TOMCAT_COMPRESSION_ENABLED=true
ENV SERVER_TOMCAT_COMPRESSION_MIN_RESPONSE_SIZE=1024
ENV SERVER_TOMCAT_HTTP2_ENABLED=true
ENV SPRING_CACHE_TYPE=caffeine
ENV SPRING_CACHE_CAFFEINE_SPEC="maximumSize=10000,expireAfterWrite=300s,expireAfterAccess=60s"

# 安装必要的工具
RUN apt-get update && apt-get install -y \\
    curl \\
    && rm -rf /var/lib/apt/lists/*

# 复制JAR文件
COPY target/*.jar app.jar

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \\
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java \$JAVA_OPTS -jar app.jar"]
EOF

    log_success "Docker优化配置已创建"
}

# 创建性能测试脚本
create_performance_test() {
    log_info "创建性能测试脚本..."
    
    cat > performance-test.sh << 'EOF'
#!/bin/bash
# 性能测试脚本
# 目标：验证响应时间是否在10毫秒以内

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 测试参数
TARGET_URL="${1:-http://localhost:8080/actuator/health}"
TARGET_RESPONSE_TIME="${2:-10}"  # 目标响应时间（毫秒）
TEST_ITERATIONS="${3:-100}"      # 测试迭代次数

log_info "开始性能测试..."
log_info "目标URL: $TARGET_URL"
log_info "目标响应时间: ${TARGET_RESPONSE_TIME}ms"
log_info "测试迭代次数: $TEST_ITERATIONS"

# 检查curl是否可用
if ! command -v curl &> /dev/null; then
    log_error "curl未安装，请先安装curl"
    exit 1
fi

# 执行性能测试
log_info "执行性能测试..."
TOTAL_TIME=0
SUCCESS_COUNT=0
FAIL_COUNT=0
MAX_TIME=0
MIN_TIME=999999

for i in $(seq 1 $TEST_ITERATIONS); do
    # 执行HTTP请求并测量时间
    RESPONSE_TIME=$(curl -o /dev/null -s -w '%{time_total}' "$TARGET_URL" | awk '{print $1 * 1000}')
    
    if [ $? -eq 0 ]; then
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))
        TOTAL_TIME=$(echo "$TOTAL_TIME + $RESPONSE_TIME" | bc -l)
        
        # 更新最大和最小时间
        if (( $(echo "$RESPONSE_TIME > $MAX_TIME" | bc -l) )); then
            MAX_TIME=$RESPONSE_TIME
        fi
        if (( $(echo "$RESPONSE_TIME < $MIN_TIME" | bc -l) )); then
            MIN_TIME=$RESPONSE_TIME
        fi
        
        # 检查是否达到目标响应时间
        if (( $(echo "$RESPONSE_TIME <= $TARGET_RESPONSE_TIME" | bc -l) )); then
            log_success "请求 $i: ${RESPONSE_TIME}ms ✓"
        else
            log_warning "请求 $i: ${RESPONSE_TIME}ms ✗ (超过目标)"
        fi
    else
        FAIL_COUNT=$((FAIL_COUNT + 1))
        log_error "请求 $i: 失败"
    fi
    
    # 短暂延迟
    sleep 0.1
done

# 计算统计信息
if [ $SUCCESS_COUNT -gt 0 ]; then
    AVG_TIME=$(echo "scale=2; $TOTAL_TIME / $SUCCESS_COUNT" | bc -l)
    SUCCESS_RATE=$(echo "scale=2; $SUCCESS_COUNT * 100 / $TEST_ITERATIONS" | bc -l)
    
    log_info "=== 性能测试结果 ==="
    log_info "总请求数: $TEST_ITERATIONS"
    log_info "成功请求数: $SUCCESS_COUNT"
    log_info "失败请求数: $FAIL_COUNT"
    log_info "成功率: ${SUCCESS_RATE}%"
    log_info "平均响应时间: ${AVG_TIME}ms"
    log_info "最大响应时间: ${MAX_TIME}ms"
    log_info "最小响应时间: ${MIN_TIME}ms"
    
    # 检查是否达到目标
    if (( $(echo "$AVG_TIME <= $TARGET_RESPONSE_TIME" | bc -l) )); then
        log_success "✓ 平均响应时间 ${AVG_TIME}ms 达到目标 ${TARGET_RESPONSE_TIME}ms"
    else
        log_warning "✗ 平均响应时间 ${AVG_TIME}ms 未达到目标 ${TARGET_RESPONSE_TIME}ms"
    fi
else
    log_error "所有请求都失败了"
    exit 1
fi
EOF

    chmod +x performance-test.sh
    log_success "性能测试脚本已创建"
}

# 创建监控脚本
create_monitoring_script() {
    log_info "创建监控脚本..."
    
    cat > monitor-performance.sh << 'EOF'
#!/bin/bash
# 性能监控脚本
# 实时监控服务性能指标

set -euo pipefail

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 监控参数
SERVICE_URL="${1:-http://localhost:8080}"
MONITOR_INTERVAL="${2:-5}"  # 监控间隔（秒）

log_info "开始监控服务性能..."
log_info "服务URL: $SERVICE_URL"
log_info "监控间隔: ${MONITOR_INTERVAL}秒"

# 检查curl是否可用
if ! command -v curl &> /dev/null; then
    log_error "curl未安装，请先安装curl"
    exit 1
fi

# 监控循环
while true; do
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    
    # 检查服务健康状态
    if curl -s -f "$SERVICE_URL/actuator/health" > /dev/null 2>&1; then
        # 获取响应时间
        RESPONSE_TIME=$(curl -o /dev/null -s -w '%{time_total}' "$SERVICE_URL/actuator/health" | awk '{print $1 * 1000}')
        
        # 获取JVM内存使用情况
        MEMORY_INFO=$(curl -s "$SERVICE_URL/actuator/metrics/jvm.memory.used" 2>/dev/null | jq -r '.measurements[0].value' 2>/dev/null || echo "N/A")
        
        # 获取线程数
        THREAD_COUNT=$(curl -s "$SERVICE_URL/actuator/metrics/jvm.threads.live" 2>/dev/null | jq -r '.measurements[0].value' 2>/dev/null || echo "N/A")
        
        # 显示监控信息
        if (( $(echo "$RESPONSE_TIME <= 10" | bc -l) )); then
            log_success "[$TIMESTAMP] 响应时间: ${RESPONSE_TIME}ms ✓ | 内存: ${MEMORY_INFO} | 线程: ${THREAD_COUNT}"
        else
            log_warning "[$TIMESTAMP] 响应时间: ${RESPONSE_TIME}ms ✗ | 内存: ${MEMORY_INFO} | 线程: ${THREAD_COUNT}"
        fi
    else
        log_error "[$TIMESTAMP] 服务不可用"
    fi
    
    sleep $MONITOR_INTERVAL
done
EOF

    chmod +x monitor-performance.sh
    log_success "监控脚本已创建"
}

# 主函数
main() {
    log_info "开始微服务性能优化..."
    
    # 检查环境
    check_java_version
    check_docker
    
    # 执行优化
    optimize_jvm
    optimize_spring_boot
    optimize_docker
    create_performance_test
    create_monitoring_script
    
    log_success "性能优化配置完成！"
    log_info "使用方法："
    log_info "1. 将配置文件应用到各服务"
    log_info "2. 重新构建和部署服务"
    log_info "3. 运行性能测试: ./performance-test.sh"
    log_info "4. 运行性能监控: ./monitor-performance.sh"
}

# 执行主函数
main "$@"
