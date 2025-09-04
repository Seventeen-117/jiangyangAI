package io.metersphere.whitebox.validator;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 依赖注入验证结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DependencyValidationResult extends BaseValidationResult {
    private int totalDependencies;
    private int satisfiedDependencies;
    private int unsatisfiedDependencies;
    private boolean valid;
    
    @Override
    public void calculateStatistics() {
        super.calculateStatistics();
        this.totalDependencies = this.totalChecks;
        this.satisfiedDependencies = this.passedChecks;
        this.unsatisfiedDependencies = this.failedChecks;
        this.valid = this.overallPassed;
    }
}