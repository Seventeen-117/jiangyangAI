package io.metersphere.whitebox.service;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class ValidationResult {
    private Map<String, Boolean> ruleResults = new HashMap<>();
    private boolean overallResult = true;
    
    public void addRuleResult(String ruleName, boolean isValid) {
        ruleResults.put(ruleName, isValid);
        if (!isValid) {
            overallResult = false;
        }
    }
    
    public int getFailedRules() {
        return (int) ruleResults.values().stream().filter(result -> !result).count();
    }
}