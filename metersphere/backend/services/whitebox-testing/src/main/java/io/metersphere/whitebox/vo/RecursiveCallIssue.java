package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 递归调用问题
 */
@Data
public class RecursiveCallIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private int recursiveDepth;
    private String codeSnippet;
}
