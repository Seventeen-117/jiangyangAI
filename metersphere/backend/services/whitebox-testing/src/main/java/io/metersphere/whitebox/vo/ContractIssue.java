package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 契约问题
 */
@Data
public class ContractIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String codeSnippet;
}
