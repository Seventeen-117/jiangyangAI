package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 循环处理问题
 */
@Data
public class LoopHandlingIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String loopType;
}