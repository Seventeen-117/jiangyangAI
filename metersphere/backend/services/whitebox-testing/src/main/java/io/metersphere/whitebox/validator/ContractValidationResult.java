package io.metersphere.whitebox.validator;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * API契约验证结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContractValidationResult extends BaseValidationResult {
    private int totalContracts;
    private int validContracts;
    private int invalidContracts;
    private boolean overallValid;
    
    @Override
    public void calculateStatistics() {
        super.calculateStatistics();
        this.totalContracts = this.totalChecks;
        this.validContracts = this.passedChecks;
        this.invalidContracts = this.failedChecks;
        this.overallValid = this.overallPassed;
    }
}