# DeepSearch Service Dockerfile
# 深度搜索服务

FROM openjdk:24-jdk-slim

# 设置工作目录
WORKDIR /app

# 设置环境变量 - 性能优化配置
ENV JAVA_OPTS="-server -XX:+UseZGC -XX:+UseLargePages -XX:+UseTransparentHugePages -Xms1g -Xmx2g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=512m -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:+UseCompressedOops -XX:+UseCompressedClassPointers -Djava.net.preferIPv4Stack=true -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -XX:+UseStringDeduplication -XX:+UseNUMA -XX:+UseNUMAInterleaving -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+OptimizeStringConcat -XX:+UseAdaptiveNUMAChunkSizing"
ENV SPRING_PROFILES_ACTIVE=dev
ENV TZ=Asia/Shanghai
ENV NACOS_HOST=8.133.246.113
ENV NACOS_PORT=8848
ENV SERVER_PORT=8085
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
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 复制JAR文件
COPY target/deepSearch-service-*.jar app.jar

# 创建非root用户并设置home目录
RUN groupadd -r appuser && useradd -r -g appuser -m -d /home/appuser appuser
RUN chown -R appuser:appuser /app /home/appuser
USER appuser

# 暴露端口
EXPOSE 8085

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8085/deepSearch/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]