package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 加密实现问题
 */
@Data
public class EncryptionImplementationIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String encryptionType;
    private String codeSnippet;
}
