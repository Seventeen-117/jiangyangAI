package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

/**
 * 空指针异常问题
 */
@Data
@Builder
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