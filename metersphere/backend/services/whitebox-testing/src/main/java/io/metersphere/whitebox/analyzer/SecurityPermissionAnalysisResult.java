package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.EncryptionImplementationIssue;
import io.metersphere.whitebox.vo.PermissionBypassIssue;
import io.metersphere.whitebox.vo.SecurityVulnerabilityIssue;
import io.metersphere.whitebox.vo.SensitiveDataIssue;
import lombok.Data;
import java.util.*;

/**
 * 安全权限分析结果
 */
@Data
public class SecurityPermissionAnalysisResult {
    private String serviceName;
    private Date analysisTime;
    private List<SensitiveDataIssue> sensitiveDataIssues = new ArrayList<>();
    private List<PermissionBypassIssue> permissionBypassIssues = new ArrayList<>();
    private List<SecurityVulnerabilityIssue> securityVulnerabilityIssues = new ArrayList<>();
    private List<EncryptionImplementationIssue> encryptionImplementationIssues = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    
    // 统计信息
    private int totalIssues;
    private int criticalIssues;
    private int highIssues;
    private int mediumIssues;
    private int lowIssues;
    
    public void addError(String error) {
        this.errors.add(error);
    }
    
    public void calculateStatistics() {
        totalIssues = sensitiveDataIssues.size() + permissionBypassIssues.size() + 
                     securityVulnerabilityIssues.size() + encryptionImplementationIssues.size();
        
        criticalIssues = (int) sensitiveDataIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) permissionBypassIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) securityVulnerabilityIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) encryptionImplementationIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count();
        
        highIssues = (int) sensitiveDataIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) permissionBypassIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) securityVulnerabilityIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) encryptionImplementationIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();
        
        mediumIssues = (int) sensitiveDataIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) permissionBypassIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) securityVulnerabilityIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) encryptionImplementationIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count();
        
        lowIssues = (int) sensitiveDataIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) permissionBypassIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) securityVulnerabilityIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) encryptionImplementationIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count();
    }
}

