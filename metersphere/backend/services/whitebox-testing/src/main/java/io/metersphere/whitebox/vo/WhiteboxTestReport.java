package io.metersphere.whitebox.vo;

import io.metersphere.whitebox.entity.WhiteboxMetric;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WhiteboxTestReport {
    private String serviceName;
    private LocalDateTime generatedTime;
    private List<WhiteboxMetric> coverageMetrics;
    private List<WhiteboxMetric> qualityMetrics;
    private List<WhiteboxMetric> securityMetrics;
    private List<WhiteboxMetric> maintainabilityMetrics;
}