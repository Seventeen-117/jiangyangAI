package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

 /**
 * 敏感数据问题
 */
@Data
@Builder
public class SensitiveDataIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String sensitiveDataType;
    private String codeSnippet;
}
