package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 契约警告
 */
@Data
public class ContractWarning {
    private String warningType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String suggestion;
    private String codeSnippet;
}
