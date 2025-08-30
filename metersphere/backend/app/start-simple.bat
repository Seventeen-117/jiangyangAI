@echo off
echo ========================================
echo MeterSphere 启动脚本 - 简化版本
echo ========================================
echo.
echo 已应用的修复：
echo 1. 修复了Spring Cloud Bootstrap版本不匹配问题
echo 2. 降级Spring Cloud Alibaba版本到2022.0.0.0
echo 3. 在Application.java中强制设置HTTP传输
echo 4. 注释掉@EnableDiscoveryClient注解
echo 5. 禁用Nacos服务发现和配置中心
echo 6. 完全使用本地配置
echo 7. 解决Java 21与gRPC的兼容性问题
echo 8. 禁用Spring AI的JdbcChatMemoryRepository自动配置
echo 9. 修复bootstrap.yml中的YAML配置结构问题
echo 10. 修复application-dev.yml中的MySQL 8.0+兼容性配置
echo.
echo 启动应用...
echo.

REM 设置环境变量强制HTTP传输
set NACOS_TRANSPORT_TYPE=http
set NACOS_CLIENT_TRANSPORT_TYPE=http
set NACOS_CLIENT_NAMING_TRANSPORT_TYPE=http
set NACOS_CLIENT_CONFIG_TRANSPORT_TYPE=http

REM 设置JVM参数强制HTTP传输
set JAVA_OPTS=-Dspring.profiles.active=dev -Dfile.encoding=UTF-8 -Dnacos.client.transport.type=http -Dnacos.client.naming.transport.type=http -Dnacos.client.config.transport.type=http -Dnacos.transport.type=http -Dnacos.naming.transport.type=http -Dnacos.config.transport.type=http -Dnacos.client.grpc.enabled=false -Dnacos.client.grpc.transport.enabled=false

echo 启动命令: java %JAVA_OPTS% -jar target/metersphere-1.0.0-Final.jar
echo.

java %JAVA_OPTS% -jar target/metersphere-1.0.0-Final.jar

pause
