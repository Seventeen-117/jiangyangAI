package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

 /**
 * 契约警告
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractWarning {
    private String warningType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String suggestion;
    private String codeSnippet;
}