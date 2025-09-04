package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 外部依赖问题
 */
@Data
public class ExternalDependencyIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String dependencyType;
    private String codeSnippet;
}
