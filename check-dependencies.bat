@echo off
echo ========================================
echo    检查依赖冲突
echo ========================================
echo.

echo 正在检查 signature-service 的依赖树...
echo.

REM 进入signature-service目录
cd signature-service

echo 步骤1: 检查依赖树...
echo 查找 Spring Boot 相关依赖的版本冲突...
echo.

REM 使用 Maven 检查依赖树，过滤 Spring Boot 相关依赖
call mvn dependency:tree -Dverbose | findstr -i "spring-boot"

echo.
echo 步骤2: 检查传递依赖...
echo 查找可能引入旧版本 Spring Boot 的依赖...
echo.

REM 检查是否有 javax.servlet 等旧版本依赖
call mvn dependency:tree -Dverbose | findstr -i "javax"

echo.
echo 步骤3: 检查当前类路径...
echo.

if exist "target\classes" (
    echo 编译输出目录存在
) else (
    echo 编译输出目录不存在，需要先编译
)

if exist "target\*.jar" (
    echo JAR 文件存在
    dir target\*.jar
) else (
    echo JAR 文件不存在，需要先打包
)

echo.
echo 按任意键退出...
pause >nul
