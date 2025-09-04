package io.metersphere.whitebox.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class FaultToleranceResult {
    private String serviceName;
    private String testType; // circuit-breaker, fallback
    private int totalTests;
    private int passedTests;
    private int failedTests;
    private boolean overallPassed;
    private List<String> testFailures = new ArrayList<>();
    
    public void addTestFailure(String failure) {
        this.testFailures.add(failure);
        this.overallPassed = false;
    }
}