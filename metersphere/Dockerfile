# MeterSphere Dockerfile
# 基于openjdk24镜像

FROM openjdk:24-jdk-slim

# 设置工作目录
WORKDIR /app

# 设置环境变量
ENV JAVA_OPTS="-Xms1g -Xmx2g -XX:+UseG1GC -XX:+UseStringDeduplication"
ENV SPRING_PROFILES_ACTIVE=dev
ENV TZ=Asia/Shanghai

# 安装必要的工具
RUN apk add --no-cache \
    curl \
    && rm -rf /var/cache/apk/*

# 复制JAR文件
COPY metersphere/backend/app/target/app-*.jar app.jar

# 创建非root用户
RUN addgroup -S appuser && adduser -S -G appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# 暴露端口
EXPOSE 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]