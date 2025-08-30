@echo off
REM Nacos 配置中心环境变量配置示例 (Windows版本)
REM 请根据实际环境修改这些变量值

REM ========================================
REM Nacos 配置中心配置
REM ========================================
set NACOS_HOST=8.133.246.113
set NACOS_PORT=8848
set NACOS_NAMESPACE=d750d92e-152f-4055-a641-3bc9dda85a29
set NACOS_GROUP=DEFAULT_GROUP
set NACOS_DISCOVERY_ENABLED=true
set NACOS_CONFIG_ENABLED=true

REM ========================================
REM Spring 配置
REM ========================================
set SPRING_PROFILES_ACTIVE=dev

REM ========================================
REM 数据库配置
REM ========================================
set DB_HOST=8.133.246.113
set DB_PORT=3306
set DB_NAME=metersphere
set DB_USERNAME=root
set DB_PASSWORD=password
set DB_MAX_POOL_SIZE=50
set DB_MIN_IDLE=10

REM ========================================
REM Redis 配置
REM ========================================
set REDIS_HOST=8.133.246.113
set REDIS_PORT=6379
set REDIS_PASSWORD=
set REDIS_DATABASE=0
set REDIS_MAX_ACTIVE=20
set REDIS_MAX_IDLE=10
set REDIS_MIN_IDLE=5
set REDIS_CLUSTER_NODES=8.133.246.113:6379,8.133.246.114:6379,8.133.246.115:6379

REM ========================================
REM MinIO 配置
REM ========================================
set MINIO_ENDPOINT=http://8.133.246.113:9000
set MINIO_ACCESS_KEY=minioadmin
set MINIO_SECRET_KEY=minioadmin
set MINIO_BUCKET=metersphere

REM ========================================
REM Kafka 配置
REM ========================================
set KAFKA_SERVERS=8.133.246.113:9092
set KAFKA_GROUP_ID=metersphere-group
set KAFKA_CLUSTER_SERVERS=8.133.246.113:9092,8.133.246.114:9092,8.133.246.115:9092

REM ========================================
REM JMeter 配置
REM ========================================
set JMETER_HOME=C:\apache-jmeter
set JMETER_REPORT_PATH=C:\metersphere\reports
set JMETER_RETENTION=30
set JMETER_CONTROLLER_HOSTS=8.133.246.113:1099,8.133.246.114:1099

REM ========================================
REM 系统配置
REM ========================================
set SERVER_PORT=8081
set UPLOAD_PATH=C:\metersphere\upload
set UPLOAD_MAX_SIZE=100MB
set TEMP_PATH=C:\metersphere\temp
set LOG_PATH=C:\metersphere\logs
set LOG_MAX_SIZE=100MB
set LOG_MAX_HISTORY=30
set LOG_LEVEL=INFO
set SPRING_LOG_LEVEL=WARN
set NACOS_LOG_LEVEL=INFO

REM ========================================
REM 生产环境特定配置
REM ========================================
if "%SPRING_PROFILES_ACTIVE%"=="prod" (
    REM 生产环境数据库配置
    set DB_MAX_POOL_SIZE=200
    set DB_MIN_IDLE=20
    
    REM 生产环境Redis配置
    set REDIS_MAX_ACTIVE=50
    set REDIS_MAX_IDLE=20
    set REDIS_MIN_IDLE=10
    
    REM 生产环境文件上传配置
    set UPLOAD_MAX_SIZE=2048MB
    set LOG_MAX_SIZE=500MB
    set LOG_MAX_HISTORY=90
    set LOG_LEVEL=INFO
    set SPRING_LOG_LEVEL=WARN
    set NACOS_LOG_LEVEL=WARN
    
    REM 生产环境通知配置
    set PROD_SMTP_HOST=smtp.production.com
    set PROD_EMAIL_USERNAME=notify@production.com
    set PROD_EMAIL_PASSWORD=secure_password_here
    
    set PROD_DINGTALK_WEBHOOK_URL=https://oapi.dingtalk.com/robot/send?access_token=your_token
    set PROD_DINGTALK_SECRET=your_secret_here
    
    set PROD_WECHAT_CORP_ID=your_corp_id
    set PROD_WECHAT_AGENT_ID=your_agent_id
    set PROD_WECHAT_SECRET=your_secret_here
    
    REM 生产环境短信配置
    set SMS_ACCESS_KEY=your_sms_access_key
    set SMS_SECRET_KEY=your_sms_secret_key
    set SMS_SIGN_NAME=your_sign_name
    
    REM 生产环境云存储配置
    set PROD_CLOUD_STORAGE_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
    set PROD_CLOUD_STORAGE_ACCESS_KEY=your_oss_access_key
    set PROD_CLOUD_STORAGE_SECRET_KEY=your_oss_secret_key
    set PROD_CLOUD_STORAGE_BUCKET=metersphere-prod-storage
    
    REM 生产环境CDN配置
    set CDN_DOMAIN=cdn.production.com
    
    REM 生产环境备份配置
    set BACKUP_BUCKET=metersphere-prod-backup
    
    REM 生产环境APM配置
    set APM_ENDPOINT=http://skywalking.production.com:11800
    
    REM 生产环境安全配置
    set LOGIN_IP_WHITELIST=192.168.1.0/24,10.0.0.0/8
)

REM ========================================
REM 开发环境特定配置
REM ========================================
if "%SPRING_PROFILES_ACTIVE%"=="dev" (
    REM 开发环境数据库配置
    set DB_MAX_POOL_SIZE=20
    set DB_MIN_IDLE=5
    
    REM 开发环境Redis配置
    set REDIS_MAX_ACTIVE=8
    set REDIS_MAX_IDLE=8
    set REDIS_MIN_IDLE=0
    
    REM 开发环境文件上传配置
    set UPLOAD_MAX_SIZE=100MB
    set LOG_MAX_SIZE=100MB
    set LOG_MAX_HISTORY=30
    set LOG_LEVEL=DEBUG
    set SPRING_LOG_LEVEL=INFO
    set NACOS_LOG_LEVEL=DEBUG
)

REM ========================================
REM 测试环境特定配置
REM ========================================
if "%SPRING_PROFILES_ACTIVE%"=="test" (
    REM 测试环境数据库配置
    set DB_MAX_POOL_SIZE=50
    set DB_MIN_IDLE=10
    
    REM 测试环境Redis配置
    set REDIS_MAX_ACTIVE=15
    set REDIS_MAX_IDLE=10
    set REDIS_MIN_IDLE=5
    
    REM 测试环境文件上传配置
    set UPLOAD_MAX_SIZE=500MB
    set LOG_MAX_SIZE=200MB
    set LOG_MAX_HISTORY=60
    set LOG_LEVEL=INFO
    set SPRING_LOG_LEVEL=WARN
    set NACOS_LOG_LEVEL=INFO
)

REM ========================================
REM 显示当前配置
REM ========================================
echo 当前环境配置:
echo SPRING_PROFILES_ACTIVE: %SPRING_PROFILES_ACTIVE%
echo NACOS_HOST: %NACOS_HOST%:%NACOS_PORT%
echo NACOS_NAMESPACE: %NACOS_NAMESPACE%
echo DB_HOST: %DB_HOST%:%DB_PORT%
echo REDIS_HOST: %REDIS_HOST%:%REDIS_PORT%
echo MINIO_ENDPOINT: %MINIO_ENDPOINT%
echo KAFKA_SERVERS: %KAFKA_SERVERS%
echo JMETER_HOME: %JMETER_HOME%
echo LOG_LEVEL: %LOG_LEVEL%

REM ========================================
REM 验证必要配置
REM ========================================
echo.
echo 配置验证:

REM 检查Nacos配置
if "%NACOS_HOST%"=="" (
    echo ❌ Nacos配置不完整
) else (
    echo ✅ Nacos配置完整
)

REM 检查数据库配置
if "%DB_HOST%"=="" (
    echo ❌ 数据库配置不完整
) else (
    echo ✅ 数据库配置完整
)

REM 检查Redis配置
if "%REDIS_HOST%"=="" (
    echo ❌ Redis配置不完整
) else (
    echo ✅ Redis配置完整
)

REM 检查MinIO配置
if "%MINIO_ENDPOINT%"=="" (
    echo ❌ MinIO配置不完整
) else (
    echo ✅ MinIO配置完整
)

echo.
echo 配置加载完成！
echo 请确保在启动应用前已正确设置这些环境变量。

REM 保持窗口打开
pause
