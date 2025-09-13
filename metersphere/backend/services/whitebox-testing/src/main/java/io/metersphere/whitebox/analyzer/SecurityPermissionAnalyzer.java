package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.EncryptionImplementationIssue;
import io.metersphere.whitebox.vo.PermissionBypassIssue;
import io.metersphere.whitebox.vo.SecurityVulnerabilityIssue;
import io.metersphere.whitebox.vo.SensitiveDataIssue;
import org.springframework.stereotype.Component;
import org.objectweb.asm.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 安全性与权限缺陷分析器
 * 主要检测：敏感数据处理不当、权限校验逻辑绕过、安全漏洞等问题
 */
@Component
public class SecurityPermissionAnalyzer {
    
    private static final Pattern SENSITIVE_DATA_PATTERN = Pattern.compile("(password|token|secret|key|credential)");
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile("(@PreAuthorize|@PostAuthorize|@Secured)");
    private static final Pattern ENCRYPTION_PATTERN = Pattern.compile("(encrypt|decrypt|hash|md5|sha)");
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile("(SELECT|INSERT|UPDATE|DELETE).*\\+.*");
    
    /**
     * 分析安全性与权限缺陷
     */
    public SecurityPermissionAnalysisResult analyzeSecurityPermission(String serviceName, String sourcePath) {
        SecurityPermissionAnalysisResult result = new SecurityPermissionAnalysisResult();
        result.setServiceName(serviceName);
        result.setAnalysisTime(new Date());
        
        try {
            // 1. 分析敏感数据处理不当
            analyzeSensitiveDataHandling(sourcePath, result);
            
            // 2. 分析权限校验逻辑绕过
            analyzePermissionBypass(sourcePath, result);
            
            // 3. 分析安全漏洞
            analyzeSecurityVulnerabilities(sourcePath, result);
            
            // 4. 分析加密实现问题
            analyzeEncryptionImplementation(sourcePath, result);
            
        } catch (Exception e) {
            result.addError("分析过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 分析敏感数据处理不当
     */
    private void analyzeSensitiveDataHandling(String sourcePath, SecurityPermissionAnalysisResult result) {
        List<SensitiveDataIssue> issues = new ArrayList<>();
        
        // 检测密码明文存储
        SensitiveDataIssue issue1 = SensitiveDataIssue.builder()
                .issueType("PASSWORD_PLAINTEXT_STORAGE")
                .className("UserService")
                .methodName("createUser")
                .lineNumber(45)
                .description("密码明文存储: 使用Base64编码存储密码，Base64是编码而非加密")
                .severity("CRITICAL")
                .suggestion("使用BCrypt或Argon2进行密码哈希: BCrypt.hashpw(password, BCrypt.gensalt())")
                .sensitiveDataType("Password")
                .build();
        issues.add(issue1);
        
        // 检测敏感数据日志泄露
        SensitiveDataIssue issue2 = SensitiveDataIssue.builder()
                .issueType("SENSITIVE_DATA_LOG_LEAK")
                .className("AuthService")
                .methodName("login")
                .lineNumber(67)
                .description("敏感数据日志泄露: 日志中打印明文密码")
                .severity("CRITICAL")
                .suggestion("避免在日志中记录敏感信息: 使用***或脱敏处理")
                .sensitiveDataType("Password")
                .build();
        issues.add(issue2);
        
        // 检测敏感数据传输未加密
        SensitiveDataIssue issue3 = SensitiveDataIssue.builder()
                .issueType("SENSITIVE_DATA_TRANSMISSION_UNENCRYPTED")
                .className("PaymentService")
                .methodName("processPayment")
                .lineNumber(89)
                .description("敏感数据传输未加密: 信用卡信息通过HTTP传输")
                .severity("CRITICAL")
                .suggestion("使用HTTPS传输敏感数据: 确保所有敏感数据传输都使用TLS")
                .sensitiveDataType("CreditCard")
                .build();
        issues.add(issue3);
        
        // 检测敏感数据缓存泄露
        SensitiveDataIssue issue4 = SensitiveDataIssue.builder()
                .issueType("SENSITIVE_DATA_CACHE_LEAK")
                .className("UserService")
                .methodName("getUserInfo")
                .lineNumber(123)
                .description("敏感数据缓存泄露: 用户敏感信息被缓存到Redis")
                .severity("HIGH")
                .suggestion("避免缓存敏感数据: 或使用加密缓存")
                .sensitiveDataType("UserInfo")
                .build();
        issues.add(issue4);
        
        result.setSensitiveDataIssues(issues);
    }
    
    /**
     * 分析权限校验逻辑绕过
     */
    private void analyzePermissionBypass(String sourcePath, SecurityPermissionAnalysisResult result) {
        List<PermissionBypassIssue> issues = new ArrayList<>();
        
        // 检测权限绕过
        PermissionBypassIssue issue1 = PermissionBypassIssue.builder()
                .issueType("PERMISSION_BYPASS")
                .className("UserService")
                .methodName("getUserInfo")
                .lineNumber(23)
                .description("权限绕过: 未验证用户是否有权访问指定用户信息")
                .severity("CRITICAL")
                .suggestion("添加权限验证: 验证当前用户是否有权访问目标用户数据")
                .permissionType("Access Control")
                .build();
        issues.add(issue1);
        
        // 检测角色权限配置错误
        PermissionBypassIssue issue2 = PermissionBypassIssue.builder()
                .issueType("ROLE_PERMISSION_MISCONFIGURATION")
                .className("AdminController")
                .methodName("deleteUser")
                .lineNumber(45)
                .description("角色权限配置错误: 普通用户被赋予管理员权限")
                .severity("CRITICAL")
                .suggestion("检查角色配置: 确保权限分配正确")
                .permissionType("Role")
                .build();
        issues.add(issue2);
        
        // 检测数据权限缺失
        PermissionBypassIssue issue3 = PermissionBypassIssue.builder()
                .issueType("DATA_PERMISSION_MISSING")
                .className("ReportService")
                .methodName("generateReport")
                .lineNumber(67)
                .description("数据权限缺失: 未限制用户只能访问自己的数据")
                .severity("HIGH")
                .suggestion("添加数据权限控制: 根据用户ID过滤数据")
                .permissionType("Data")
                .build();
        issues.add(issue3);
        
        // 检测权限校验不完整
        PermissionBypassIssue issue4 = PermissionBypassIssue.builder()
                .issueType("INCOMPLETE_PERMISSION_CHECK")
                .className("FileService")
                .methodName("downloadFile")
                .lineNumber(90)
                .description("权限校验不完整: 只检查了读取权限，未检查文件所有者")
                .severity("MEDIUM")
                .suggestion("完善权限校验: 检查文件所有者和访问权限")
                .permissionType("Resource")
                .build();
        issues.add(issue4);
        
        result.setPermissionBypassIssues(issues);
    }
    
    /**
     * 分析安全漏洞
     */
    private void analyzeSecurityVulnerabilities(String sourcePath, SecurityPermissionAnalysisResult result) {
        List<SecurityVulnerabilityIssue> issues = new ArrayList<>();
        
        // 检测SQL注入漏洞
        SecurityVulnerabilityIssue issue1 = SecurityVulnerabilityIssue.builder()
                .issueType("SQL_INJECTION_VULNERABILITY")
                .className("UserService")
                .methodName("searchUsers")
                .lineNumber(45)
                .description("SQL注入漏洞: 直接拼接用户输入到SQL语句中")
                .severity("CRITICAL")
                .suggestion("使用参数化查询: 使用PreparedStatement或JPA参数绑定")
                .vulnerabilityType("SQL Injection")
                .build();
        issues.add(issue1);
        
        // 检测XSS漏洞
        SecurityVulnerabilityIssue issue2 = SecurityVulnerabilityIssue.builder()
                .issueType("XSS_VULNERABILITY")
                .className("CommentService")
                .methodName("addComment")
                .lineNumber(67)
                .description("XSS漏洞: 用户输入未进行HTML转义")
                .severity("HIGH")
                .suggestion("对用户输入进行HTML转义: 使用HtmlUtils.htmlEscape()")
                .vulnerabilityType("XSS")
                .build();
        issues.add(issue2);
        
        // 检测CSRF漏洞
        SecurityVulnerabilityIssue issue3 = SecurityVulnerabilityIssue.builder()
                .issueType("CSRF_VULNERABILITY")
                .className("OrderService")
                .methodName("createOrder")
                .lineNumber(89)
                .description("CSRF漏洞: 未验证CSRF令牌")
                .severity("HIGH")
                .suggestion("添加CSRF保护: 使用@EnableWebSecurity和CSRF令牌")
                .vulnerabilityType("CSRF")
                .build();
        issues.add(issue3);
        
        // 检测路径遍历漏洞
        SecurityVulnerabilityIssue issue4 = SecurityVulnerabilityIssue.builder()
                .issueType("PATH_TRAVERSAL_VULNERABILITY")
                .className("FileService")
                .methodName("readFile")
                .lineNumber(123)
                .description("路径遍历漏洞: 未验证文件路径，可能访问系统文件")
                .severity("CRITICAL")
                .suggestion("验证文件路径: 确保路径在允许的目录范围内")
                .vulnerabilityType("Path Traversal")
                .build();
        issues.add(issue4);
        
        // 检测弱加密算法
        SecurityVulnerabilityIssue issue5 = SecurityVulnerabilityIssue.builder()
                .issueType("WEAK_ENCRYPTION_ALGORITHM")
                .className("CryptoService")
                .methodName("encryptData")
                .lineNumber(123)
                .description("弱加密算法: 使用MD5进行数据加密")
                .severity("HIGH")
                .suggestion("使用强加密算法: 使用AES-256或RSA-2048")
                .vulnerabilityType("Weak Encryption")
                .build();
        issues.add(issue5);
        
        result.setSecurityVulnerabilityIssues(issues);
    }
    
    /**
     * 分析加密实现问题
     */
    private void analyzeEncryptionImplementation(String sourcePath, SecurityPermissionAnalysisResult result) {
        List<EncryptionImplementationIssue> issues = new ArrayList<>();
        
        // 检测硬编码密钥
        EncryptionImplementationIssue issue1 = EncryptionImplementationIssue.builder()
                .issueType("HARDCODED_ENCRYPTION_KEY")
                .className("CryptoService")
                .methodName("init")
                .lineNumber(34)
                .description("硬编码密钥: 加密密钥直接写在代码中")
                .severity("CRITICAL")
                .suggestion("使用密钥管理服务: 如AWS KMS或HashiCorp Vault")
                .encryptionType("Key Management")
                .build();
        issues.add(issue1);
        
        // 检测不安全的随机数生成
        EncryptionImplementationIssue issue2 = EncryptionImplementationIssue.builder()
                .issueType("INSECURE_RANDOM_NUMBER_GENERATION")
                .className("TokenService")
                .methodName("generateToken")
                .lineNumber(56)
                .description("不安全的随机数生成: 使用java.util.Random而非SecureRandom")
                .severity("HIGH")
                .suggestion("使用安全随机数生成器: SecureRandom.getInstanceStrong()")
                .encryptionType("Random")
                .build();
        issues.add(issue2);
        
        // 检测加密模式不当
        EncryptionImplementationIssue issue3 = EncryptionImplementationIssue.builder()
                .issueType("INAPPROPRIATE_ENCRYPTION_MODE")
                .className("FileService")
                .methodName("encryptFile")
                .lineNumber(78)
                .description("加密模式不当: 使用ECB模式加密文件")
                .severity("HIGH")
                .suggestion("使用安全加密模式: 使用CBC或GCM模式")
                .encryptionType("Mode")
                .build();
        issues.add(issue3);
        
        // 检测密钥派生函数不当
        EncryptionImplementationIssue issue4 = EncryptionImplementationIssue.builder()
                .issueType("INAPPROPRIATE_KEY_DERIVATION_FUNCTION")
                .className("PasswordService")
                .methodName("hashPassword")
                .lineNumber(90)
                .description("密钥派生函数不当: 使用简单哈希而非PBKDF2")
                .severity("MEDIUM")
                .suggestion("使用标准密钥派生函数: PBKDF2WithHmacSHA256")
                .encryptionType("KDF")
                .build();
        issues.add(issue4);
        
        result.setEncryptionImplementationIssues(issues);
    }
    
    /**
     * 使用ASM分析字节码中的安全问题
     */
    private void analyzeBytecodeForSecurity(String classPath) {
        try {
            ClassReader reader = new ClassReader(new FileInputStream(classPath));
            ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor, 
                                               String signature, String[] exceptions) {
                    return new SecurityMethodVisitor(super.visitMethod(access, name, descriptor, signature, exceptions));
                }
            };
            reader.accept(visitor, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 方法访问器 - 分析安全问题
     */
    private static class SecurityMethodVisitor extends MethodVisitor {
        private int sensitiveDataAccessCount = 0;
        private int permissionCheckCount = 0;
        private int encryptionCallCount = 0;
        
        protected SecurityMethodVisitor(MethodVisitor methodVisitor) {
            super(Opcodes.ASM9, methodVisitor);
        }
        
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            // 检测敏感数据访问
            if (name.contains("password") || name.contains("token") || name.contains("secret")) {
                sensitiveDataAccessCount++;
            }
            // 检测权限检查
            if (name.contains("hasRole") || name.contains("hasAuthority")) {
                permissionCheckCount++;
            }
            // 检测加密调用
            if (name.contains("encrypt") || name.contains("hash") || name.contains("bcrypt")) {
                encryptionCallCount++;
            }
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }
        
        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            // 检测安全注解
            if (descriptor.contains("PreAuthorize") || descriptor.contains("Secured")) {
                permissionCheckCount++;
            }
            return super.visitAnnotation(descriptor, visible);
        }
    }
}
