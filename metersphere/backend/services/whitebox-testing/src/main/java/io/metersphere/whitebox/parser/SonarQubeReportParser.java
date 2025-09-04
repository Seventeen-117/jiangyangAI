package io.metersphere.whitebox.parser;

import io.metersphere.whitebox.entity.WhiteboxMetric;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
public class SonarQubeReportParser {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public List<WhiteboxMetric> parseMetrics(String sonarProjectKey, String sonarUrl, String sonarToken) {
        List<WhiteboxMetric> metrics = new ArrayList<>();
        
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            if (sonarToken != null && !sonarToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + sonarToken);
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 调用SonarQube API获取项目指标
            String url = sonarUrl + "/api/measures/component?component=" + sonarProjectKey + 
                        "&metricKeys=ncloc,complexity,comment_lines_density,duplicated_lines_density,coverage,bugs,vulnerabilities,code_smells";
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("component")) {
                Map<String, Object> component = (Map<String, Object>) responseBody.get("component");
                if (component.containsKey("measures")) {
                    List<Map<String, Object>> measures = (List<Map<String, Object>>) component.get("measures");
                    
                    for (Map<String, Object> measure : measures) {
                        String metricKey = (String) measure.get("metric");
                        String valueStr = (String) measure.get("value");
                        
                        if (valueStr != null) {
                            double value = Double.parseDouble(valueStr);
                            WhiteboxMetric metric = createMetricFromSonarData(metricKey, value);
                            if (metric != null) {
                                metrics.add(metric);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return metrics;
    }
    
    private WhiteboxMetric createMetricFromSonarData(String metricKey, double value) {
        WhiteboxMetric metric = new WhiteboxMetric();
        metric.setLastUpdated(LocalDateTime.now());
        
        switch (metricKey) {
            case "ncloc":
                metric.setMetricType("size");
                metric.setMetricName("代码行数");
                metric.setCurrentValue(value);
                metric.setThresholdValue(10000.0); // 阈值根据项目实际情况调整
                metric.setStatus(value <= metric.getThresholdValue() ? "PASS" : "FAIL");
                break;
            case "complexity":
                metric.setMetricType("complexity");
                metric.setMetricName("圈复杂度");
                metric.setCurrentValue(value);
                metric.setThresholdValue(500.0); // 阈值根据项目实际情况调整
                metric.setStatus(value <= metric.getThresholdValue() ? "PASS" : "FAIL");
                break;
            case "comment_lines_density":
                metric.setMetricType("maintainability");
                metric.setMetricName("注释行密度");
                metric.setCurrentValue(value);
                metric.setThresholdValue(25.0); // 阈值根据项目实际情况调整
                metric.setStatus(value >= metric.getThresholdValue() ? "PASS" : "FAIL");
                break;
            case "duplicated_lines_density":
                metric.setMetricType("quality");
                metric.setMetricName("重复代码密度");
                metric.setCurrentValue(value);
                metric.setThresholdValue(3.0);
                metric.setStatus(value <= metric.getThresholdValue() ? "PASS" : "FAIL");
                break;
            case "coverage":
                metric.setMetricType("coverage");
                metric.setMetricName("代码覆盖率");
                metric.setCurrentValue(value);
                metric.setThresholdValue(80.0);
                metric.setStatus(value >= metric.getThresholdValue() ? "PASS" : "FAIL");
                break;
            case "bugs":
                metric.setMetricType("quality");
                metric.setMetricName("Bug数量");
                metric.setCurrentValue(value);
                metric.setThresholdValue(10.0);
                metric.setStatus(value <= metric.getThresholdValue() ? "PASS" : "FAIL");
                break;
            case "vulnerabilities":
                metric.setMetricType("security");
                metric.setMetricName("漏洞数量");
                metric.setCurrentValue(value);
                metric.setThresholdValue(5.0);
                metric.setStatus(value <= metric.getThresholdValue() ? "PASS" : "FAIL");
                break;
            case "code_smells":
                metric.setMetricType("maintainability");
                metric.setMetricName("代码异味");
                metric.setCurrentValue(value);
                metric.setThresholdValue(20.0);
                metric.setStatus(value <= metric.getThresholdValue() ? "PASS" : "FAIL");
                break;
            default:
                return null;
        }
        
        return metric;
    }
    
    // 为了向后兼容，保留原有的方法
    public List<WhiteboxMetric> parseMetrics(String sonarProjectKey) {
        // 使用默认的SonarQube配置
        return parseMetrics(sonarProjectKey, "http://localhost:9000", null);
    }
}