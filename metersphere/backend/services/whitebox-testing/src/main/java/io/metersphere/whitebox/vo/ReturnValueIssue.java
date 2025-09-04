package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 返回值处理问题
 */
@Data
public class ReturnValueIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String returnType;
    private String codeSnippet;
}
