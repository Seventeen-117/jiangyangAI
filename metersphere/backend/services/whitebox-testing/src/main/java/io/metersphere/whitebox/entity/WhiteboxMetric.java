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
    
    @Column(name = "service_name")
    private String serviceName;          // 服务名称
    
    @Column(name = "metric_type")
    private String metricType;           // 指标类型（覆盖率、复杂度等）
    
    @Column(name = "metric_name")
    private String metricName;           // 指标名称
    
    @Column(name = "current_value")
    private Double currentValue;         // 当前值
    
    @Column(name = "threshold_value")
    private Double thresholdValue;       // 阈值
    
    @Column(name = "status")
    private String status;               // 状态（PASS/FAIL）
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;   // 最后更新时间
    
    // getters and setters
}