package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 分支逻辑问题
 */
@Data
public class BranchLogicIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String branchType;
}