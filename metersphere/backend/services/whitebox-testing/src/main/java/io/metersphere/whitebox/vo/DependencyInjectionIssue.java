package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 依赖注入问题
 */
@Data
public class DependencyInjectionIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String injectionType;
    private String codeSnippet;
}
