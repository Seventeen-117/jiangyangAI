package io.metersphere.whitebox.validator;

import io.metersphere.whitebox.vo.ContractInfo;
import io.metersphere.whitebox.vo.ContractIssue;
import io.metersphere.whitebox.vo.ContractWarning;
import lombok.Data;
import java.util.*;

/**
 * API契约验证结果
 */
@Data
public class ApiContractValidationResult {
    private String serviceName;
    private Date validationTime;
    private List<ContractIssue> contractIssues = new ArrayList<>();
    private List<ContractWarning> contractWarnings = new ArrayList<>();
    private List<ContractInfo> contractInfos = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    
    // 统计信息
    private int totalIssues;
    private int totalWarnings;
    private int totalInfos;
    private boolean overallValid;
    
    public void addIssue(ContractIssue issue) {
        this.contractIssues.add(issue);
        this.overallValid = false;
    }
    
    public void addWarning(ContractWarning warning) {
        this.contractWarnings.add(warning);
    }
    
    public void addInfo(ContractInfo info) {
        this.contractInfos.add(info);
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.overallValid = false;
    }
    
    public void calculateStatistics() {
        totalIssues = contractIssues.size();
        totalWarnings = contractWarnings.size();
        totalInfos = contractInfos.size();
        
        // 如果没有问题，则认为验证通过
        overallValid = totalIssues == 0;
    }
}

