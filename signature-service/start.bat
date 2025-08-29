@echo off
echo Starting Signature Service with Java 17+ compatibility...

REM 检查JAR文件是否存在
if not exist "target\signature-service-1.0.0-Final.jar" (
    echo Error: JAR file not found. Please build the project first.
    echo Run: mvn clean package
    pause
    exit /b 1
)

REM 设置JVM参数（Windows批处理格式）
set JAVA_OPTS=--add-opens=java.base/java.math=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.base/java.time=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.security=ALL-UNNAMED -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai

REM 启动应用
echo Starting with JVM options...
java %JAVA_OPTS% -jar target/signature-service-1.0.0-Final.jar

pause
