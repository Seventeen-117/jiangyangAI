package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 长函数问题
 */
@Data
public class LongFunctionIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String functionType;
}