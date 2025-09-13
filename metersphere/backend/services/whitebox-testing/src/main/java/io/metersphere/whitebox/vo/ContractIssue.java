package io.metersphere.whitebox.vo;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

 /**
 * 契约问题
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContractIssue {
    private String issueType;
    private String className;
    private String methodName;
    private int lineNumber;
    private String description;
    private String severity;
    private String suggestion;
    private String codeSnippet;
}