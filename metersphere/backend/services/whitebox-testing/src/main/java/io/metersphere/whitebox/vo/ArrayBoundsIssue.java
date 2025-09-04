package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 数组越界问题
 */
@Data
public class ArrayBoundsIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String arrayType;
    private String codeSnippet;
}
