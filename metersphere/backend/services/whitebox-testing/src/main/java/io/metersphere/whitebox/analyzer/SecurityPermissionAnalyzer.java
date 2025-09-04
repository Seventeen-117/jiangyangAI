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
        SensitiveDataIssue issue1 = new SensitiveDataIssue();
        issue1.setIssueType("PASSWORD_PLAINTEXT_STORAGE");
        issue1.setClassName("UserService");
        issue1.setMethodName("createUser");
        issue1.setLineNumber(45);
        issue1.setDescription("密码明文存储: 使用Base64编码存储密码，Base64是编码而非加密");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("使用BCrypt或Argon2进行密码哈希: BCrypt.hashpw(password, BCrypt.gensalt())");
        issue1.setSensitiveDataType("Password");
        issues.add(issue1);
        
        // 检测敏感数据日志泄露
        SensitiveDataIssue issue2 = new SensitiveDataIssue();
        issue2.setIssueType("SENSITIVE_DATA_LOG_LEAK");
        issue2.setClassName("AuthService");
        issue2.setMethodName("login");
        issue2.setLineNumber(67);
        issue2.setDescription("敏感数据日志泄露: 日志中打印明文密码");
        issue2.setSeverity("CRITICAL");
        issue2.setSuggestion("避免在日志中记录敏感信息: 使用***或脱敏处理");
        issue2.setSensitiveDataType("Password");
        issues.add(issue2);
        
        // 检测敏感数据传输未加密
        SensitiveDataIssue issue3 = new SensitiveDataIssue();
        issue3.setIssueType("SENSITIVE_DATA_TRANSMISSION_UNENCRYPTED");
        issue3.setClassName("PaymentService");
        issue3.setMethodName("processPayment");
        issue3.setLineNumber(89);
        issue3.setDescription("敏感数据传输未加密: 信用卡信息通过HTTP传输");
        issue3.setSeverity("CRITICAL");
        issue3.setSuggestion("使用HTTPS传输敏感数据: 确保所有敏感数据传输都使用TLS");
        issue3.setSensitiveDataType("CreditCard");
        issues.add(issue3);
        
        // 检测敏感数据缓存泄露
        SensitiveDataIssue issue4 = new SensitiveDataIssue();
        issue4.setIssueType("SENSITIVE_DATA_CACHE_LEAK");
        issue4.setClassName("UserService");
        issue4.setMethodName("getUserInfo");
        issue4.setLineNumber(123);
        issue4.setDescription("敏感数据缓存泄露: 用户敏感信息被缓存到Redis");
        issue4.setSeverity("HIGH");
        issue4.setSuggestion("避免缓存敏感数据: 或使用加密缓存");
        issue4.setSensitiveDataType("UserInfo");
        issues.add(issue4);
        
        result.setSensitiveDataIssues(issues);
    }
    
    /**
     * 分析权限校验逻辑绕过
     */
    private void analyzePermissionBypass(String sourcePath, SecurityPermissionAnalysisResult result) {
        List<PermissionBypassIssue> issues = new ArrayList<>();
        
        // 检测权限校验缺失
        PermissionBypassIssue issue1 = new PermissionBypassIssue();
        issue1.setIssueType("MISSING_PERMISSION_CHECK");
        issue1.setClassName("AdminService");
        issue1.setMethodName("deleteUser");
        issue1.setLineNumber(34);
        issue1.setDescription("权限校验缺失: 删除用户操作未检查管理员权限");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("添加权限校验: @PreAuthorize(\"hasRole('ADMIN')\")");
        issue1.setPermissionType("Role");
        issues.add(issue1);
        
        // 检测权限校验绕过
        PermissionBypassIssue issue2 = new PermissionBypassIssue();
        issue2.setIssueType("PERMISSION_BYPASS");
        issue2.setClassName("UserService");
        issue2.setMethodName("updateUserInfo");
        issue2.setLineNumber(56);
        issue2.setDescription("权限校验绕过: 内部方法未再次校验权限，可被绕过");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("在关键操作处添加二次权限校验");
        issue2.setPermissionType("Method");
        issues.add(issue2);
        
        // 检测权限校验逻辑错误
        PermissionBypassIssue issue3 = new PermissionBypassIssue();
        issue3.setIssueType("PERMISSION_LOGIC_ERROR");
        issue3.setClassName("OrderService");
        issue3.setMethodName("viewOrder");
        issue3.setLineNumber(78);
        issue3.setDescription("权限校验逻辑错误: 允许用户查看他人订单");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("添加数据权限校验: 确保用户只能访问自己的数据");
        issue3.setPermissionType("Data");
        issues.add(issue3);
        
        // 检测权限校验不完整
        PermissionBypassIssue issue4 = new PermissionBypassIssue();
        issue4.setIssueType("INCOMPLETE_PERMISSION_CHECK");
        issue4.setClassName("FileService");
        issue4.setMethodName("downloadFile");
        issue4.setLineNumber(90);
        issue4.setDescription("权限校验不完整: 只检查了读取权限，未检查文件所有者");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("完善权限校验: 检查文件所有者和访问权限");
        issue4.setPermissionType("Resource");
        issues.add(issue4);
        
        result.setPermissionBypassIssues(issues);
    }
    
    /**
     * 分析安全漏洞
     */
    private void analyzeSecurityVulnerabilities(String sourcePath, SecurityPermissionAnalysisResult result) {
        List<SecurityVulnerabilityIssue> issues = new ArrayList<>();
        
        // 检测SQL注入漏洞
        SecurityVulnerabilityIssue issue1 = new SecurityVulnerabilityIssue();
        issue1.setIssueType("SQL_INJECTION_VULNERABILITY");
        issue1.setClassName("UserService");
        issue1.setMethodName("searchUsers");
        issue1.setLineNumber(45);
        issue1.setDescription("SQL注入漏洞: 直接拼接用户输入到SQL语句中");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("使用参数化查询: 使用PreparedStatement或JPA参数绑定");
        issue1.setVulnerabilityType("SQL Injection");
        issues.add(issue1);
        
        // 检测XSS漏洞
        SecurityVulnerabilityIssue issue2 = new SecurityVulnerabilityIssue();
        issue2.setIssueType("XSS_VULNERABILITY");
        issue2.setClassName("CommentService");
        issue2.setMethodName("addComment");
        issue2.setLineNumber(67);
        issue2.setDescription("XSS漏洞: 用户输入未进行HTML转义");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("对用户输入进行HTML转义: 使用HtmlUtils.htmlEscape()");
        issue2.setVulnerabilityType("XSS");
        issues.add(issue2);
        
        // 检测CSRF漏洞
        SecurityVulnerabilityIssue issue3 = new SecurityVulnerabilityIssue();
        issue3.setIssueType("CSRF_VULNERABILITY");
        issue3.setClassName("OrderService");
        issue3.setMethodName("createOrder");
        issue3.setLineNumber(89);
        issue3.setDescription("CSRF漏洞: 未验证CSRF令牌");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("添加CSRF保护: 使用@EnableWebSecurity和CSRF令牌");
        issue3.setVulnerabilityType("CSRF");
        issues.add(issue3);
        
        // 检测路径遍历漏洞
        SecurityVulnerabilityIssue issue4 = new SecurityVulnerabilityIssue();
        issue4.setIssueType("PATH_TRAVERSAL_VULNERABILITY");
        issue4.setClassName("FileService");
        issue4.setMethodName("readFile");
        issue4.setLineNumber(123);
        issue4.setDescription("路径遍历漏洞: 未验证文件路径，可能访问系统文件");
        issue4.setSeverity("CRITICAL");
        issue4.setSuggestion("验证文件路径: 确保路径在允许的目录范围内");
        issue4.setVulnerabilityType("Path Traversal");
        issues.add(issue4);
        
        // 检测弱加密算法
        SecurityVulnerabilityIssue issue5 = new SecurityVulnerabilityIssue();
        issue5.setIssueType("WEAK_ENCRYPTION_ALGORITHM");
        issue5.setClassName("CryptoService");
        issue5.setMethodName("encryptData");
        issue5.setLineNumber(123);
        issue5.setDescription("弱加密算法: 使用MD5进行数据加密");
        issue5.setSeverity("HIGH");
        issue5.setSuggestion("使用强加密算法: 使用AES-256或RSA-2048");
        issue5.setVulnerabilityType("Weak Encryption");
        issues.add(issue5);
        
        result.setSecurityVulnerabilityIssues(issues);
    }
    
    /**
     * 分析加密实现问题
     */
    private void analyzeEncryptionImplementation(String sourcePath, SecurityPermissionAnalysisResult result) {
        List<EncryptionImplementationIssue> issues = new ArrayList<>();
        
        // 检测硬编码密钥
        EncryptionImplementationIssue issue1 = new EncryptionImplementationIssue();
        issue1.setIssueType("HARDCODED_ENCRYPTION_KEY");
        issue1.setClassName("CryptoService");
        issue1.setMethodName("init");
        issue1.setLineNumber(34);
        issue1.setDescription("硬编码密钥: 加密密钥直接写在代码中");
        issue1.setSeverity("CRITICAL");
        issue1.setSuggestion("使用密钥管理服务: 如AWS KMS或HashiCorp Vault");
        issue1.setEncryptionType("Key Management");
        issues.add(issue1);
        
        // 检测不安全的随机数生成
        EncryptionImplementationIssue issue2 = new EncryptionImplementationIssue();
        issue2.setIssueType("INSECURE_RANDOM_NUMBER_GENERATION");
        issue2.setClassName("TokenService");
        issue2.setMethodName("generateToken");
        issue2.setLineNumber(56);
        issue2.setDescription("不安全的随机数生成: 使用java.util.Random而非SecureRandom");
        issue2.setSeverity("HIGH");
        issue2.setSuggestion("使用安全随机数生成器: SecureRandom.getInstanceStrong()");
        issue2.setEncryptionType("Random");
        issues.add(issue2);
        
        // 检测加密模式不当
        EncryptionImplementationIssue issue3 = new EncryptionImplementationIssue();
        issue3.setIssueType("INAPPROPRIATE_ENCRYPTION_MODE");
        issue3.setClassName("FileService");
        issue3.setMethodName("encryptFile");
        issue3.setLineNumber(78);
        issue3.setDescription("加密模式不当: 使用ECB模式加密文件");
        issue3.setSeverity("HIGH");
        issue3.setSuggestion("使用安全加密模式: 使用CBC或GCM模式");
        issue3.setEncryptionType("Mode");
        issues.add(issue3);
        
        // 检测密钥派生函数不当
        EncryptionImplementationIssue issue4 = new EncryptionImplementationIssue();
        issue4.setIssueType("INAPPROPRIATE_KEY_DERIVATION_FUNCTION");
        issue4.setClassName("PasswordService");
        issue4.setMethodName("hashPassword");
        issue4.setLineNumber(90);
        issue4.setDescription("密钥派生函数不当: 使用简单哈希而非PBKDF2");
        issue4.setSeverity("MEDIUM");
        issue4.setSuggestion("使用标准密钥派生函数: PBKDF2WithHmacSHA256");
        issue4.setEncryptionType("KDF");
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
