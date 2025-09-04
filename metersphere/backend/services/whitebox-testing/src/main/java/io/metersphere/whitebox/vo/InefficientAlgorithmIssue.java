package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 低效算法问题
 */
@Data
public class InefficientAlgorithmIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String algorithmType;
    private String codeSnippet;
}
