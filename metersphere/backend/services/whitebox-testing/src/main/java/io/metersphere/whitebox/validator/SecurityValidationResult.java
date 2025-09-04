package io.metersphere.whitebox.validator;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 安全验证结果
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityValidationResult extends BaseValidationResult {
    private String validationType; // authorization, data-encryption
}