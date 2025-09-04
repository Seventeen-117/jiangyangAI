package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 方法复杂度问题
 */
@Data
public class MethodComplexityIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private int complexityScore;
    private String codeSnippet;
}
