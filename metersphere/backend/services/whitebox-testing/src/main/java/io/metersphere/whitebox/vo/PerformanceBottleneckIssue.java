package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

/**
 * 性能瓶颈问题
 */
@Data
@Builder
public class PerformanceBottleneckIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String bottleneckType;
    private long executionTime;
    private String resourceUsage;
}