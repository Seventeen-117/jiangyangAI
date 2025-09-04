package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 服务通信问题
 */
@Data
public class ServiceCommunicationIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String communicationType;
    private String codeSnippet;
}
