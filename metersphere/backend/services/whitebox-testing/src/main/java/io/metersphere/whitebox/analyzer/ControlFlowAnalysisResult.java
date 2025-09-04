package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.BranchLogicIssue;
import io.metersphere.whitebox.vo.ControlFlowIssue;
import io.metersphere.whitebox.vo.ExceptionHandlingIssue;
import io.metersphere.whitebox.vo.LoopHandlingIssue;
import lombok.Data;
import java.util.*;

/**
 * 控制流分析结果
 */
@Data
public class ControlFlowAnalysisResult {
    private String serviceName;
    private Date analysisTime;
    private List<ControlFlowIssue> controlFlowIssues = new ArrayList<>();
    private List<BranchLogicIssue> branchLogicIssues = new ArrayList<>();
    private List<LoopHandlingIssue> loopHandlingIssues = new ArrayList<>();
    private List<ExceptionHandlingIssue> exceptionHandlingIssues = new ArrayList<>();
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
    
    // 添加缺失的setter方法
    public void setControlFlowIssues(List<ControlFlowIssue> controlFlowIssues) {
        this.controlFlowIssues = controlFlowIssues;
    }
    
    public void setBranchLogicIssues(List<BranchLogicIssue> branchLogicIssues) {
        this.branchLogicIssues = branchLogicIssues;
    }
    
    public void setLoopHandlingIssues(List<LoopHandlingIssue> loopHandlingIssues) {
        this.loopHandlingIssues = loopHandlingIssues;
    }
    
    public void setExceptionHandlingIssues(List<ExceptionHandlingIssue> exceptionHandlingIssues) {
        this.exceptionHandlingIssues = exceptionHandlingIssues;
    }
    
    public void calculateStatistics() {
        totalIssues = controlFlowIssues.size() + branchLogicIssues.size() + 
                     loopHandlingIssues.size() + exceptionHandlingIssues.size();
        
        criticalIssues = (int) controlFlowIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) branchLogicIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) loopHandlingIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) exceptionHandlingIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count();
        
        highIssues = (int) controlFlowIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) branchLogicIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) loopHandlingIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) exceptionHandlingIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();
        
        mediumIssues = (int) controlFlowIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) branchLogicIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) loopHandlingIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) exceptionHandlingIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count();
        
        lowIssues = (int) controlFlowIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) branchLogicIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) loopHandlingIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) exceptionHandlingIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count();
    }
}
