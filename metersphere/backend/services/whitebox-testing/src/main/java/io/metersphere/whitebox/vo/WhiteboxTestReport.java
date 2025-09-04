package io.metersphere.whitebox.vo;

import io.metersphere.whitebox.entity.WhiteboxMetric;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WhiteboxTestReport {
    private String serviceName;
    private LocalDateTime generatedTime;
    private List<WhiteboxMetric> coverageMetrics;
    private List<WhiteboxMetric> qualityMetrics;
    private List<WhiteboxMetric> securityMetrics;
    private List<WhiteboxMetric> maintainabilityMetrics;
    
    public WhiteboxTestReport() {
        this.generatedTime = LocalDateTime.now();
    }
}