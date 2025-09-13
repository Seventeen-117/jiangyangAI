package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

 /**
 * 加密实现问题
 */
@Data
@Builder
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
