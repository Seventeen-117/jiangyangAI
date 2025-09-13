package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

/**
 * 修复建议
 */
@Data
@Builder
public class FixRecommendation {
    private String issueType;
    private String recommendation;
    private String priority;
    private String implementationGuide;
    private String category;
    private String description;
    private String estimatedEffort;
}