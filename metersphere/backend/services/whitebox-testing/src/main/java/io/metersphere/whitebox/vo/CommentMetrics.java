package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class CommentMetrics {
    private String serviceName;
    private double commentCoverage;
    private int totalLines;
    private int commentedLines;
    private int complexMethodsWithoutComments;
    private boolean meetsStandard;
}