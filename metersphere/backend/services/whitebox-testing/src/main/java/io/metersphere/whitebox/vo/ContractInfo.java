package io.metersphere.whitebox.vo;

import lombok.Data; /**
 * 契约信息
 */
@Data
public class ContractInfo {
    private String infoType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String suggestion;
    private String codeSnippet;
}
