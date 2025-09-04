package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 内存使用问题
 */
@Data
public class MemoryUsageIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String memoryType;
    private String codeSnippet;
}
