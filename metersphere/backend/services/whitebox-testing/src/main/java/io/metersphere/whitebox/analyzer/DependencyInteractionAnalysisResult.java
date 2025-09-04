package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.DependencyInjectionIssue;
import io.metersphere.whitebox.vo.DistributedTransactionIssue;
import io.metersphere.whitebox.vo.ExternalDependencyIssue;
import io.metersphere.whitebox.vo.ServiceCommunicationIssue;
import lombok.Data;
import java.util.*;

/**
 * 依赖交互分析结果
 */
@Data
public class DependencyInteractionAnalysisResult {
    private String serviceName;
    private Date analysisTime;
    private List<ExternalDependencyIssue> externalDependencyIssues = new ArrayList<>();
    private List<DistributedTransactionIssue> distributedTransactionIssues = new ArrayList<>();
    private List<DependencyInjectionIssue> dependencyInjectionIssues = new ArrayList<>();
    private List<ServiceCommunicationIssue> serviceCommunicationIssues = new ArrayList<>();
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
        totalIssues = externalDependencyIssues.size() + distributedTransactionIssues.size() + 
                     dependencyInjectionIssues.size() + serviceCommunicationIssues.size();
        
        criticalIssues = (int) externalDependencyIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) distributedTransactionIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) dependencyInjectionIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) serviceCommunicationIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count();
        
        highIssues = (int) externalDependencyIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) distributedTransactionIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) dependencyInjectionIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) serviceCommunicationIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();
        
        mediumIssues = (int) externalDependencyIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) distributedTransactionIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) dependencyInjectionIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) serviceCommunicationIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count();
        
        lowIssues = (int) externalDependencyIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) distributedTransactionIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) dependencyInjectionIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) serviceCommunicationIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count();
    }
}

