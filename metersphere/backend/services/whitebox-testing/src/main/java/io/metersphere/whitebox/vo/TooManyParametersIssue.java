package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 参数过多问题
 */
@Data
public class TooManyParametersIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String parameterType;
}