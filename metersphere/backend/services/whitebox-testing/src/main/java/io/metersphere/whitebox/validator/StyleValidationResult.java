package io.metersphere.whitebox.validator;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 代码风格验证结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StyleValidationResult extends BaseValidationResult {
    private double complianceRate;
    
    @Override
    public void calculateStatistics() {
        super.calculateStatistics();
        this.complianceRate = getPassRate();
    }
}