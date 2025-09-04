package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.ArrayBoundsIssue;
import io.metersphere.whitebox.vo.DataValidationIssue;
import io.metersphere.whitebox.vo.NullPointerExceptionIssue;
import io.metersphere.whitebox.vo.TypeConversionIssue;
import lombok.Data;
import java.util.*;

/**
 * 数据处理分析结果
 */
@Data
public class DataProcessingAnalysisResult {
    private String serviceName;
    private Date analysisTime;
    private List<ArrayBoundsIssue> arrayBoundsIssues = new ArrayList<>();
    private List<TypeConversionIssue> typeConversionIssues = new ArrayList<>();
    private List<NullPointerExceptionIssue> nullPointerIssues = new ArrayList<>();
    private List<DataValidationIssue> dataValidationIssues = new ArrayList<>();
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
    public void setArrayBoundsIssues(List<ArrayBoundsIssue> arrayBoundsIssues) {
        this.arrayBoundsIssues = arrayBoundsIssues;
    }
    
    public void setTypeConversionIssues(List<TypeConversionIssue> typeConversionIssues) {
        this.typeConversionIssues = typeConversionIssues;
    }
    
    public void setNullPointerIssues(List<NullPointerExceptionIssue> nullPointerIssues) {
        this.nullPointerIssues = nullPointerIssues;
    }
    
    public void setDataValidationIssues(List<DataValidationIssue> dataValidationIssues) {
        this.dataValidationIssues = dataValidationIssues;
    }
    
    public void calculateStatistics() {
        totalIssues = arrayBoundsIssues.size() + typeConversionIssues.size() + 
                     nullPointerIssues.size() + dataValidationIssues.size();
        
        criticalIssues = (int) arrayBoundsIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) typeConversionIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) nullPointerIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count() +
                        (int) dataValidationIssues.stream().filter(i -> "CRITICAL".equals(i.getSeverity())).count();
        
        highIssues = (int) arrayBoundsIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) typeConversionIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) nullPointerIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count() +
                    (int) dataValidationIssues.stream().filter(i -> "HIGH".equals(i.getSeverity())).count();
        
        mediumIssues = (int) arrayBoundsIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) typeConversionIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) nullPointerIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count() +
                      (int) dataValidationIssues.stream().filter(i -> "MEDIUM".equals(i.getSeverity())).count();
        
        lowIssues = (int) arrayBoundsIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) typeConversionIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) nullPointerIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count() +
                   (int) dataValidationIssues.stream().filter(i -> "LOW".equals(i.getSeverity())).count();
    }
}

