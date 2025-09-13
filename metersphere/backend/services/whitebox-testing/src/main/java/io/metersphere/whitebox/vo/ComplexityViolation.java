package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ComplexityViolation {
    private String className;
    private String methodName;
    private int complexity;
    private int threshold;
}