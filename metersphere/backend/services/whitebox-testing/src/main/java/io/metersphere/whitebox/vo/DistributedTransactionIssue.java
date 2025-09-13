package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

 /**
 * 分布式事务问题
 */
@Data
@Builder
public class DistributedTransactionIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String transactionType;
    private String codeSnippet;
}
