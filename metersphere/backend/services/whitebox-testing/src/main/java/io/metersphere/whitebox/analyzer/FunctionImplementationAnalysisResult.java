package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.MethodComplexityIssue;
import io.metersphere.whitebox.vo.ParameterValidationIssue;
import io.metersphere.whitebox.vo.RecursiveCallIssue;
import io.metersphere.whitebox.vo.ReturnValueIssue;
import lombok.Data;
import java.util.*;

/**
 * 函数实现分析结果
 */
@Data
public class FunctionImplementationAnalysisResult {
    private String serviceName;
    private Date analysisTime;
    private List<ParameterValidationIssue> parameterValidationIssues = new ArrayList<>();
    private List<ReturnValueIssue> returnValueIssues = new ArrayList<>();
    private List<RecursiveCallIssue> recursiveCallIssues = new ArrayList<>();
    private List<MethodComplexityIssue> methodComplexityIssues = new ArrayList<>();
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
        totalIssues = parameterValidationIssues.size() + returnValueIssues.size() + 
                     recursiveCallIssues.size() + methodComplexityIssues.size();
        
        criticalIssues = (int) parameterValidationIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) returnValueIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) recursiveCallIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) methodComplexityIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count();
        
        highIssues = (int) parameterValidationIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) returnValueIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) recursiveCallIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) methodComplexityIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();
        
        mediumIssues = (int) parameterValidationIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) returnValueIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) recursiveCallIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) methodComplexityIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count();
        
        lowIssues = (int) parameterValidationIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) returnValueIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) recursiveCallIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) methodComplexityIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count();
    }
}

