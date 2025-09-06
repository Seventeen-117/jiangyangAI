# Dubbo API Dockerfile
# Dubbo API接口定义

FROM openjdk:24-jdk-slim

# 设置工作目录
WORKDIR /app

# 设置环境变量 - 性能优化配置
ENV JAVA_OPTS="-server -XX:+UseZGC -XX:+UseLargePages -XX:+UseTransparentHugePages -Xms512m -Xmx1g -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseNUMAInterleaving -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+OptimizeStringConcat -XX:+UseAdaptiveNUMAChunkSizing"
ENV TZ=Asia/Shanghai
ENV SERVER_TOMCAT_THREADS_MAX=100
ENV SERVER_TOMCAT_THREADS_MIN_SPARE=5
ENV SERVER_TOMCAT_MAX_CONNECTIONS=4096
ENV SERVER_TOMCAT_ACCEPT_COUNT=50
ENV SERVER_TOMCAT_CONNECTION_TIMEOUT=10000
ENV SERVER_TOMCAT_KEEP_ALIVE_TIMEOUT=10000
ENV SERVER_TOMCAT_MAX_KEEP_ALIVE_REQUESTS=50
ENV SERVER_TOMCAT_COMPRESSION_ENABLED=true
ENV SERVER_TOMCAT_COMPRESSION_MIN_RESPONSE_SIZE=1024
ENV SERVER_TOMCAT_HTTP2_ENABLED=true

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 复制JAR文件
COPY target/dubbo-api-*.jar app.jar

# 创建非root用户
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# 暴露端口（如果需要）
EXPOSE 8086

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8086/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]