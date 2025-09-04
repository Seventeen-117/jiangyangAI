package io.metersphere.whitebox.repository;

import io.metersphere.whitebox.entity.WhiteboxMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WhiteboxMetricRepository extends JpaRepository<WhiteboxMetric, Long> {
    List<WhiteboxMetric> findByServiceName(String serviceName);
    List<WhiteboxMetric> findByMetricType(String metricType);
}