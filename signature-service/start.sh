#!/bin/bash

echo "Starting Signature Service with Java 17+ compatibility..."

# 检查JAR文件是否存在
if [ ! -f "target/signature-service-1.0.0-Final.jar" ]; then
    echo "Error: JAR file not found. Please build the project first."
    echo "Run: mvn clean package"
    exit 1
fi

# 使用配置文件中的JVM参数
echo "Starting with JVM options from jvm-options.conf..."
java @jvm-options.conf -jar target/signature-service-1.0.0-Final.jar
