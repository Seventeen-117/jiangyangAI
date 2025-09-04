package io.metersphere.whitebox.vo;

import lombok.Data;

/**
 * 并发问题
 */
@Data
public class ConcurrencyIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String concurrencyType;
}