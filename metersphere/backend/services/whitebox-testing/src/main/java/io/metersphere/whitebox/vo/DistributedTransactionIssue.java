package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 分布式事务问题
 */
@Data
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
