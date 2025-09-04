package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 性能瓶颈问题
 */
@Data
public class PerformanceBottleneckIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String bottleneckType;
}