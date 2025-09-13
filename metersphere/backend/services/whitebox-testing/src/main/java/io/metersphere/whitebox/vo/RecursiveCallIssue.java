package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

 /**
 * 递归调用问题
 */
@Data
@Builder
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
