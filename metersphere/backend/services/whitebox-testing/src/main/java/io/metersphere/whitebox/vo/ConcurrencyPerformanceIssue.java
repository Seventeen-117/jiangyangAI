package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

 /**
 * 并发性能问题
 */
@Data
@Builder
public class ConcurrencyPerformanceIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String concurrencyType;
    private String codeSnippet;
}
