package io.metersphere.whitebox.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List; /**
 * 修复建议
 */
@Data
public class FixRecommendation {
    private String priority;
    private String category;
    private String description;
    private String estimatedEffort;
    private List<String> specificActions = new ArrayList<>();
}
