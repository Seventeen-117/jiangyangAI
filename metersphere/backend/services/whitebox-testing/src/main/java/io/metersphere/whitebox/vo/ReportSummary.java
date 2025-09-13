package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

import java.util.Date;

/**
 * 报告摘要
 */
@Data
@Builder
public class ReportSummary {
    private String serviceId;
    private String serviceName;
    private int totalIssues;
    private int criticalIssues;
    private int highIssues;
    private int mediumIssues;
    private int lowIssues;
    private String overallRiskLevel;
    private String generatedDate;
    private Date generatedTime;
    private String riskLevel;
    private String riskDescription;
}