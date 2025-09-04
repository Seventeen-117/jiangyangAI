package io.metersphere.whitebox.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "whitebox_metrics")
public class WhiteboxMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String serviceName;          // 服务名称
    private String metricType;           // 指标类型（覆盖率、复杂度等）
    private String metricName;           // 指标名称
    private Double currentValue;         // 当前值
    private Double thresholdValue;       // 阈值
    private String status;               // 状态（PASS/FAIL）
    private LocalDateTime lastUpdated;   // 最后更新时间
    
    // getters and setters
}