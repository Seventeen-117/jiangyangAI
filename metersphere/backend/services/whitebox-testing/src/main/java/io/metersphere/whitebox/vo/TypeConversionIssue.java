package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

/**
 * 类型转换问题
 */
@Data
@Builder
public class TypeConversionIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String conversionType;
}