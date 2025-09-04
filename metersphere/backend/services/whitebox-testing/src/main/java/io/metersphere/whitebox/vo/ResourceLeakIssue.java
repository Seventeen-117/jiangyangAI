package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 资源泄漏问题
 */
@Data
public class ResourceLeakIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String resourceType;
    private String codeSnippet;
}
