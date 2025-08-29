# 依赖冲突解决方案

## 问题描述

```
[ERROR] 无法访问javax.servlet.ServletException
找不到javax.servlet.ServletException的类文件
```

## 问题分析

**Spring Boot 3.x 兼容性问题**：
- Spring Boot 3.x 使用 Jakarta EE 9+
- `javax.servlet.*` 包已更名为 `jakarta.servlet.*`
- 某些依赖可能仍在使用旧的 `javax.servlet` 包

## 解决方案

### 1. 添加 Jakarta Servlet 依赖

已在 `pom.xml` 中添加：
```xml
<!-- Jakarta Servlet API (for Spring Boot 3.x compatibility) -->
<dependency>
    <groupId>jakarta.servlet</groupId>
    <artifactId>jakarta.servlet-api</artifactId>
    <version>6.0.0</version>
    <scope>provided</scope>
</dependency>

<!-- Spring Boot Starter Web (if you need servlet support) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 2. 排除冲突的依赖

如果仍有问题，可以在相关依赖中排除 `javax.servlet`：

```xml
<dependency>
    <groupId>problematic-dependency</groupId>
    <artifactId>problematic-artifact</artifactId>
    <version>x.y.z</version>
    <exclusions>
        <exclusion>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 3. 强制使用 Jakarta 版本

在 `pom.xml` 的 `<dependencyManagement>` 部分添加：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>jakarta.servlet</groupId>
            <artifactId>jakarta.servlet-api</artifactId>
            <version>6.0.0</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 验证修复

1. 清理项目：`mvn clean`
2. 重新编译：`mvn compile`
3. 检查是否还有 `javax.servlet` 相关错误

## 注意事项

1. **不要同时使用** `javax.servlet` 和 `jakarta.servlet`
2. **确保所有依赖** 都兼容 Spring Boot 3.x
3. **检查传递依赖** 是否引入了旧的 servlet 包
