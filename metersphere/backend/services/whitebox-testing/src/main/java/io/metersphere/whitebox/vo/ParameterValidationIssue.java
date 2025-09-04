package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 参数校验问题
 */
@Data
public class ParameterValidationIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String parameterName;
    private String parameterType;
    private String codeSnippet;
}
