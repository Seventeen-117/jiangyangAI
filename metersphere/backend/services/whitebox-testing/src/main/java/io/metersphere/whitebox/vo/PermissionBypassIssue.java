package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 权限绕过问题
 */
@Data
public class PermissionBypassIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String permissionType;
    private String codeSnippet;
}
