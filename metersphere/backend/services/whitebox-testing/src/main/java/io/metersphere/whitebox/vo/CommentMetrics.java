package io.metersphere.whitebox.vo;

import lombok.Data;

@Data
public class CommentMetrics {
    private String serviceName;
    private double commentCoverage;
    private int totalLines;
    private int commentedLines;
    private int complexMethodsWithoutComments;
    private boolean meetsStandard;
}