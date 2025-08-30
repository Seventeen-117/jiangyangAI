#!/bin/bash

# Nacos 配置中心环境变量配置示例
# 请根据实际环境修改这些变量值

# ========================================
# Nacos 配置中心配置
# ========================================
export NACOS_HOST=8.133.246.113
export NACOS_PORT=8848
export NACOS_NAMESPACE=d750d92e-152f-4055-a641-3bc9dda85a29
export NACOS_GROUP=DEFAULT_GROUP
export NACOS_DISCOVERY_ENABLED=true
export NACOS_CONFIG_ENABLED=true

# ========================================
# Spring 配置
# ========================================
export SPRING_PROFILES_ACTIVE=dev

# ========================================
# 数据库配置
# ========================================
export DB_HOST=8.133.246.113
export DB_PORT=3306
export DB_NAME=metersphere
export DB_USERNAME=root
export DB_PASSWORD=password
export DB_MAX_POOL_SIZE=50
export DB_MIN_IDLE=10

# ========================================
# Redis 配置
# ========================================
export REDIS_HOST=8.133.246.113
export REDIS_PORT=6379
export REDIS_PASSWORD=
export REDIS_DATABASE=0
export REDIS_MAX_ACTIVE=20
export REDIS_MAX_IDLE=10
export REDIS_MIN_IDLE=5
export REDIS_CLUSTER_NODES=8.133.246.113:6379,8.133.246.114:6379,8.133.246.115:6379

# ========================================
# MinIO 配置
# ========================================
export MINIO_ENDPOINT=http://8.133.246.113:9000
export MINIO_ACCESS_KEY=minioadmin
export MINIO_SECRET_KEY=minioadmin
export MINIO_BUCKET=metersphere

# ========================================
# Kafka 配置
# ========================================
export KAFKA_SERVERS=8.133.246.113:9092
export KAFKA_GROUP_ID=metersphere-group
export KAFKA_CLUSTER_SERVERS=8.133.246.113:9092,8.133.246.114:9092,8.133.246.115:9092

# ========================================
# JMeter 配置
# ========================================
export JMETER_HOME=/opt/apache-jmeter
export JMETER_REPORT_PATH=/opt/metersphere/reports
export JMETER_RETENTION=30
export JMETER_CONTROLLER_HOSTS=8.133.246.113:1099,8.133.246.114:1099

# ========================================
# 系统配置
# ========================================
export SERVER_PORT=8081
export UPLOAD_PATH=/opt/metersphere/upload
export UPLOAD_MAX_SIZE=100MB
export TEMP_PATH=/opt/metersphere/temp
export LOG_PATH=/opt/metersphere/logs
export LOG_MAX_SIZE=100MB
export LOG_MAX_HISTORY=30
export LOG_LEVEL=INFO
export SPRING_LOG_LEVEL=WARN
export NACOS_LOG_LEVEL=INFO

# ========================================
# 生产环境特定配置
# ========================================
if [ "$SPRING_PROFILES_ACTIVE" = "prod" ]; then
    # 生产环境数据库配置
    export DB_MAX_POOL_SIZE=200
    export DB_MIN_IDLE=20
    
    # 生产环境Redis配置
    export REDIS_MAX_ACTIVE=50
    export REDIS_MAX_IDLE=20
    export REDIS_MIN_IDLE=10
    
    # 生产环境文件上传配置
    export UPLOAD_MAX_SIZE=2048MB
    export LOG_MAX_SIZE=500MB
    export LOG_MAX_HISTORY=90
    export LOG_LEVEL=INFO
    export SPRING_LOG_LEVEL=WARN
    export NACOS_LOG_LEVEL=WARN
    
    # 生产环境通知配置
    export PROD_SMTP_HOST=smtp.production.com
    export PROD_EMAIL_USERNAME=notify@production.com
    export PROD_EMAIL_PASSWORD=secure_password_here
    
    export PROD_DINGTALK_WEBHOOK_URL=https://oapi.dingtalk.com/robot/send?access_token=your_token
    export PROD_DINGTALK_SECRET=your_secret_here
    
    export PROD_WECHAT_CORP_ID=your_corp_id
    export PROD_WECHAT_AGENT_ID=your_agent_id
    export PROD_WECHAT_SECRET=your_secret_here
    
    # 生产环境短信配置
    export SMS_ACCESS_KEY=your_sms_access_key
    export SMS_SECRET_KEY=your_sms_secret_key
    export SMS_SIGN_NAME=your_sign_name
    
    # 生产环境云存储配置
    export PROD_CLOUD_STORAGE_ENDPOINT=https://oss-cn-hangzhou.aliyuncs.com
    export PROD_CLOUD_STORAGE_ACCESS_KEY=your_oss_access_key
    export PROD_CLOUD_STORAGE_SECRET_KEY=your_oss_secret_key
    export PROD_CLOUD_STORAGE_BUCKET=metersphere-prod-storage
    
    # 生产环境CDN配置
    export CDN_DOMAIN=cdn.production.com
    
    # 生产环境备份配置
    export BACKUP_BUCKET=metersphere-prod-backup
    
    # 生产环境APM配置
    export APM_ENDPOINT=http://skywalking.production.com:11800
    
    # 生产环境安全配置
    export LOGIN_IP_WHITELIST=192.168.1.0/24,10.0.0.0/8
fi

# ========================================
# 开发环境特定配置
# ========================================
if [ "$SPRING_PROFILES_ACTIVE" = "dev" ]; then
    # 开发环境数据库配置
    export DB_MAX_POOL_SIZE=20
    export DB_MIN_IDLE=5
    
    # 开发环境Redis配置
    export REDIS_MAX_ACTIVE=8
    export REDIS_MAX_IDLE=8
    export REDIS_MIN_IDLE=0
    
    # 开发环境文件上传配置
    export UPLOAD_MAX_SIZE=100MB
    export LOG_MAX_SIZE=100MB
    export LOG_MAX_HISTORY=30
    export LOG_LEVEL=DEBUG
    export SPRING_LOG_LEVEL=INFO
    export NACOS_LOG_LEVEL=DEBUG
fi

# ========================================
# 测试环境特定配置
# ========================================
if [ "$SPRING_PROFILES_ACTIVE" = "test" ]; then
    # 测试环境数据库配置
    export DB_MAX_POOL_SIZE=50
    export DB_MIN_IDLE=10
    
    # 测试环境Redis配置
    export REDIS_MAX_ACTIVE=15
    export REDIS_MAX_IDLE=10
    export REDIS_MIN_IDLE=5
    
    # 测试环境文件上传配置
    export UPLOAD_MAX_SIZE=500MB
    export LOG_MAX_SIZE=200MB
    export LOG_MAX_HISTORY=60
    export LOG_LEVEL=INFO
    export SPRING_LOG_LEVEL=WARN
    export NACOS_LOG_LEVEL=INFO
fi

# ========================================
# 显示当前配置
# ========================================
echo "当前环境配置:"
echo "SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE"
echo "NACOS_HOST: $NACOS_HOST:$NACOS_PORT"
echo "NACOS_NAMESPACE: $NACOS_NAMESPACE"
echo "DB_HOST: $DB_HOST:$DB_PORT"
echo "REDIS_HOST: $REDIS_HOST:$REDIS_PORT"
echo "MINIO_ENDPOINT: $MINIO_ENDPOINT"
echo "KAFKA_SERVERS: $KAFKA_SERVERS"
echo "JMETER_HOME: $JMETER_HOME"
echo "LOG_LEVEL: $LOG_LEVEL"

# ========================================
# 验证必要配置
# ========================================
echo ""
echo "配置验证:"

# 检查Nacos配置
if [ -z "$NACOS_HOST" ] || [ -z "$NACOS_PORT" ]; then
    echo "❌ Nacos配置不完整"
else
    echo "✅ Nacos配置完整"
fi

# 检查数据库配置
if [ -z "$DB_HOST" ] || [ -z "$DB_USERNAME" ] || [ -z "$DB_PASSWORD" ]; then
    echo "❌ 数据库配置不完整"
else
    echo "✅ 数据库配置完整"
fi

# 检查Redis配置
if [ -z "$REDIS_HOST" ] || [ -z "$REDIS_PORT" ]; then
    echo "❌ Redis配置不完整"
else
    echo "✅ Redis配置完整"
fi

# 检查MinIO配置
if [ -z "$MINIO_ENDPOINT" ] || [ -z "$MINIO_ACCESS_KEY" ] || [ -z "$MINIO_SECRET_KEY" ]; then
    echo "❌ MinIO配置不完整"
else
    echo "✅ MinIO配置完整"
fi

echo ""
echo "配置加载完成！"
echo "请确保在启动应用前已正确设置这些环境变量。"
