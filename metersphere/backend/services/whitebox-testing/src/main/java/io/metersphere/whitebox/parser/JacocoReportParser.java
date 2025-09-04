package io.metersphere.whitebox.parser;

import io.metersphere.whitebox.entity.WhiteboxMetric;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class JacocoReportParser {
    
    public List<WhiteboxMetric> parseReport(String reportPath) {
        List<WhiteboxMetric> metrics = new ArrayList<>();
        
        try {
            File xmlFile = new File(reportPath);
            if (!xmlFile.exists()) {
                return metrics;
            }
            
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();
            
            // 解析counter元素获取覆盖率数据
            NodeList counterNodes = doc.getElementsByTagName("counter");
            for (int i = 0; i < counterNodes.getLength(); i++) {
                Element counterElement = (Element) counterNodes.item(i);
                String type = counterElement.getAttribute("type");
                String missed = counterElement.getAttribute("missed");
                String covered = counterElement.getAttribute("covered");
                
                WhiteboxMetric metric = new WhiteboxMetric();
                metric.setMetricType("coverage");
                metric.setLastUpdated(LocalDateTime.now());
                
                switch (type) {
                    case "LINE":
                        metric.setMetricName("行覆盖率");
                        break;
                    case "BRANCH":
                        metric.setMetricName("分支覆盖率");
                        break;
                    case "COMPLEXITY":
                        metric.setMetricName("圈复杂度");
                        break;
                    case "METHOD":
                        metric.setMetricName("方法覆盖率");
                        break;
                    case "CLASS":
                        metric.setMetricName("类覆盖率");
                        break;
                    default:
                        continue;
                }
                
                int missedCount = Integer.parseInt(missed);
                int coveredCount = Integer.parseInt(covered);
                int totalCount = missedCount + coveredCount;
                
                if (totalCount > 0) {
                    double coverage = (double) coveredCount / totalCount * 100;
                    metric.setCurrentValue(coverage);
                    metric.setThresholdValue(getThresholdForMetric(type));
                    metric.setStatus(coverage >= metric.getThresholdValue() ? "PASS" : "FAIL");
                }
                
                metrics.add(metric);
            }
            
            // 解析包级别覆盖率数据
            NodeList packageNodes = doc.getElementsByTagName("package");
            for (int i = 0; i < packageNodes.getLength(); i++) {
                Element packageElement = (Element) packageNodes.item(i);
                String packageName = packageElement.getAttribute("name");
                
                NodeList packageCounterNodes = packageElement.getElementsByTagName("counter");
                for (int j = 0; j < packageCounterNodes.getLength(); j++) {
                    Element counterElement = (Element) packageCounterNodes.item(j);
                    String type = counterElement.getAttribute("type");
                    String missed = counterElement.getAttribute("missed");
                    String covered = counterElement.getAttribute("covered");
                    
                    if ("LINE".equals(type) || "BRANCH".equals(type)) {
                        WhiteboxMetric metric = new WhiteboxMetric();
                        metric.setMetricType("coverage-package");
                        metric.setMetricName(packageName + " - " + ("LINE".equals(type) ? "行覆盖率" : "分支覆盖率"));
                        metric.setLastUpdated(LocalDateTime.now());
                        
                        int missedCount = Integer.parseInt(missed);
                        int coveredCount = Integer.parseInt(covered);
                        int totalCount = missedCount + coveredCount;
                        
                        if (totalCount > 0) {
                            double coverage = (double) coveredCount / totalCount * 100;
                            metric.setCurrentValue(coverage);
                            metric.setThresholdValue(getThresholdForMetric(type));
                            metric.setStatus(coverage >= metric.getThresholdValue() ? "PASS" : "FAIL");
                        }
                        
                        metrics.add(metric);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return metrics;
    }
    
    private double getThresholdForMetric(String type) {
        switch (type) {
            case "LINE":
                return 80.0; // 行覆盖率阈值
            case "BRANCH":
                return 75.0; // 分支覆盖率阈值
            case "METHOD":
                return 85.0; // 方法覆盖率阈值
            case "CLASS":
                return 90.0; // 类覆盖率阈值
            default:
                return 70.0; // 默认阈值
        }
    }
}