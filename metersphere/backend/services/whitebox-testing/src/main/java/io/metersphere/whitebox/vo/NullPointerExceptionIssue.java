package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 空指针异常问题
 */
@Data
public class NullPointerExceptionIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String nullType;
}