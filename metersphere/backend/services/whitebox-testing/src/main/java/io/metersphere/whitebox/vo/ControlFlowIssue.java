package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 控制流问题
 */
@Data
public class ControlFlowIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String controlFlowType;
}