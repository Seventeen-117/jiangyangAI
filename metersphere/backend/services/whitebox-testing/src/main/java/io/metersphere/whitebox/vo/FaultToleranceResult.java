package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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