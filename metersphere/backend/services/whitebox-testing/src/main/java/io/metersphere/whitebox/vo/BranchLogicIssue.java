package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

/**
 * 分支逻辑问题
 */
@Data
@Builder
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