package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 数据验证问题
 */
@Data
public class DataValidationIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String validationType;
}