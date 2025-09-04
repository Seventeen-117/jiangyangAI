package io.metersphere.whitebox.vo;

import lombok.Data;

import java.util.Date; /**
 * 报告摘要
 */
@Data
public class ReportSummary {
    private String serviceName;
    private Date generatedTime;
    private int totalIssues;
    private int criticalIssues;
    private int highIssues;
    private int mediumIssues;
    private int lowIssues;
    private String riskLevel;
    private String riskDescription;
}
