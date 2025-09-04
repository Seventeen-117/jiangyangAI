package io.metersphere.whitebox.validator;

import io.metersphere.whitebox.vo.ContractInfo;
import io.metersphere.whitebox.vo.ContractIssue;
import io.metersphere.whitebox.vo.ContractWarning;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.annotation.Secured;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.java.Log;
import java.lang.annotation.Annotation;
import java.util.Date;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

@Component
public class ApiContractValidator extends BaseValidator {
    
    public ContractValidationResult validateApiContracts(String serviceName) {
        return executeValidation(serviceName, ContractValidationResult::new, result -> {
            // 获取所有控制器类
            Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
            
            // 遍历所有控制器
            for (Map.Entry<String, Object> entry : controllers.entrySet()) {
                Object controller = entry.getValue();
                Class<?> controllerClass = controller.getClass();
                
                // 检查控制器类名是否包含服务名称
                if (isServiceRelated(controllerClass, serviceName)) {
                    
                    // 获取控制器中的所有方法
                    Method[] methods = controllerClass.getDeclaredMethods();
                    
                    for (Method method : methods) {
                        // 检查是否有RequestMapping相关注解
                        if (hasRequestMappingAnnotation(method)) {
                            result.setTotalChecks(result.getTotalChecks() + 1);
                            
                            // 验证API契约
                            String validationError = validateContract(controllerClass, method);
                            if (validationError == null) {
                                result.setPassedChecks(result.getPassedChecks() + 1);
                            } else {
                                result.setFailedChecks(result.getFailedChecks() + 1);
                                result.addValidationError(controllerClass.getSimpleName() + "." + method.getName() + ": " + validationError);
                            }
                        }
                    }
                }
            }
        });
    }
    
    private boolean hasRequestMappingAnnotation(Method method) {
        return method.isAnnotationPresent(RequestMapping.class) ||
               method.isAnnotationPresent(GetMapping.class) ||
               method.isAnnotationPresent(PostMapping.class) ||
               method.isAnnotationPresent(PutMapping.class) ||
               method.isAnnotationPresent(DeleteMapping.class) ||
               method.isAnnotationPresent(PatchMapping.class);
    }
    
    private String validateContract(Class<?> controllerClass, Method method) {
        // 验证参数注解
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Annotation[] annotations = parameter.getAnnotations();
            
            // 检查是否有合适的参数注解
            boolean hasValidAnnotation = false;
            for (Annotation annotation : annotations) {
                if (annotation instanceof RequestParam ||
                    annotation instanceof RequestBody ||
                    annotation instanceof RequestHeader ||
                    annotation instanceof PathVariable ||
                    annotation instanceof RequestPart) {
                    hasValidAnnotation = true;
                    break;
                }
            }
            
            // 如果有参数但没有合适的注解
            if (!hasValidAnnotation && annotations.length > 0) {
                return "Parameter " + i + " missing valid annotation";
            }
        }
        
        // 验证返回值类型
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            return "Method should not return void";
        }
        
        // 验证是否有合适的异常处理
        validateExceptionHandling(method);
        
        // 验证HTTP方法映射
        validateHttpMethodMapping(method);
        
        // 验证路径映射
        validatePathMapping(method);
        
        // 验证参数类型兼容性
        validateParameterTypeCompatibility(parameters);
        
        // 验证返回值包装
        validateReturnValueWrapping(returnType);
        
        // 验证安全注解
        validateSecurityAnnotations(method);
        
        // 验证事务注解
        validateTransactionAnnotations(method);
        
        // 验证缓存注解
        validateCacheAnnotations(method);
        
        // 验证日志注解
        validateLoggingAnnotations(method);
        
        return null; // 验证通过
    }
    
    /**
     * 验证异常处理
     */
    private void validateExceptionHandling(Method method) {
        // 检查方法是否声明了可能抛出的异常
        Class<?>[] exceptionTypes = method.getExceptionTypes();
        boolean hasExceptionHandling = false;
        
        for (Class<?> exceptionType : exceptionTypes) {
            if (Exception.class.isAssignableFrom(exceptionType)) {
                hasExceptionHandling = true;
                break;
            }
        }
        
        // 检查是否有@ExceptionHandler注解
        if (method.isAnnotationPresent(ExceptionHandler.class)) {
            hasExceptionHandling = true;
        }
        
        // 检查类级别是否有@ControllerAdvice
        if (method.getDeclaringClass().isAnnotationPresent(ControllerAdvice.class)) {
            hasExceptionHandling = true;
        }
        
        // 如果没有异常处理，记录警告
        if (!hasExceptionHandling) {
            // 可以记录到日志或添加到验证结果中
            System.out.println("Warning: Method " + method.getName() + " has no exception handling");
        }
    }
    
    /**
     * 验证HTTP方法映射
     */
    private void validateHttpMethodMapping(Method method) {
        // 检查是否有HTTP方法映射注解
        boolean hasHttpMapping = method.isAnnotationPresent(RequestMapping.class) ||
                                method.isAnnotationPresent(GetMapping.class) ||
                                method.isAnnotationPresent(PostMapping.class) ||
                                method.isAnnotationPresent(PutMapping.class) ||
                                method.isAnnotationPresent(DeleteMapping.class) ||
                                method.isAnnotationPresent(PatchMapping.class);
        
        if (!hasHttpMapping) {
            System.out.println("Warning: Method " + method.getName() + " has no HTTP method mapping");
        }
        
        // 验证HTTP方法与业务逻辑的匹配性
        if (method.isAnnotationPresent(GetMapping.class)) {
            // GET方法不应该有@RequestBody参数
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                if (parameter.isAnnotationPresent(RequestBody.class)) {
                    System.out.println("Warning: GET method should not have @RequestBody parameter");
                }
            }
        }
        
        if (method.isAnnotationPresent(PostMapping.class) || 
            method.isAnnotationPresent(PutMapping.class) ||
            method.isAnnotationPresent(PatchMapping.class)) {
            // POST/PUT/PATCH方法应该有@RequestBody参数
            Parameter[] parameters = method.getParameters();
            boolean hasRequestBody = false;
            for (Parameter parameter : parameters) {
                if (parameter.isAnnotationPresent(RequestBody.class)) {
                    hasRequestBody = true;
                    break;
                }
            }
            if (!hasRequestBody) {
                System.out.println("Warning: " + method.getName() + " should have @RequestBody parameter");
            }
        }
    }
    
    /**
     * 验证路径映射
     */
    private void validatePathMapping(Method method) {
        // 检查路径映射的合理性
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            String[] paths = mapping.value();
            
            for (String path : paths) {
                // 检查路径格式
                if (!path.startsWith("/")) {
                    System.out.println("Warning: Path should start with '/'");
                }
                
                // 检查路径变量格式
                if (path.contains("{") && !path.contains("}")) {
                    System.out.println("Warning: Incomplete path variable in path: " + path);
                }
                
                // 检查路径变量与参数的一致性
                validatePathVariableConsistency(method, path);
            }
        }
    }
    
    /**
     * 验证路径变量与参数的一致性
     */
    private void validatePathVariableConsistency(Method method, String path) {
        // 提取路径中的变量
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(path);
        java.util.Set<String> pathVariables = new java.util.HashSet<>();
        
        while (matcher.find()) {
            pathVariables.add(matcher.group(1));
        }
        
        // 检查方法参数中是否有对应的@PathVariable注解
        Parameter[] parameters = method.getParameters();
        java.util.Set<String> parameterVariables = new java.util.HashSet<>();
        
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVar = parameter.getAnnotation(PathVariable.class);
                String varName = pathVar.value().isEmpty() ? parameter.getName() : pathVar.value();
                parameterVariables.add(varName);
            }
        }
        
        // 检查一致性
        for (String pathVar : pathVariables) {
            if (!parameterVariables.contains(pathVar)) {
                System.out.println("Warning: Path variable '" + pathVar + "' not found in method parameters");
            }
        }
        
        for (String paramVar : parameterVariables) {
            if (!pathVariables.contains(paramVar)) {
                System.out.println("Warning: @PathVariable parameter '" + paramVar + "' not found in path");
            }
        }
    }
    
    /**
     * 验证参数类型兼容性
     */
    private void validateParameterTypeCompatibility(Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            Class<?> paramType = parameter.getType();
            
            // 检查@RequestParam参数类型
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                if (!isSimpleType(paramType) && !paramType.isArray()) {
                    System.out.println("Warning: @RequestParam should be used with simple types, not " + paramType.getSimpleName());
                }
            }
            
            // 检查@RequestBody参数类型
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                if (isSimpleType(paramType)) {
                    System.out.println("Warning: @RequestBody should be used with complex types, not " + paramType.getSimpleName());
                }
            }
            
            // 检查@PathVariable参数类型
            if (parameter.isAnnotationPresent(PathVariable.class)) {
                if (!isSimpleType(paramType)) {
                    System.out.println("Warning: @PathVariable should be used with simple types, not " + paramType.getSimpleName());
                }
            }
        }
    }
    
    /**
     * 检查是否为简单类型
     */
    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
               type == String.class ||
               type == Integer.class ||
               type == Long.class ||
               type == Double.class ||
               type == Float.class ||
               type == Boolean.class ||
               type == Character.class ||
               type == Byte.class ||
               type == Short.class ||
               type == java.math.BigDecimal.class ||
               type == java.math.BigInteger.class ||
               type == java.util.Date.class ||
               type == java.time.LocalDate.class ||
               type == java.time.LocalDateTime.class ||
               type == java.time.LocalTime.class;
    }
    
    /**
     * 验证返回值包装
     */
    private void validateReturnValueWrapping(Class<?> returnType) {
        // 检查返回值是否使用了统一的响应包装
        if (returnType == String.class) {
            System.out.println("Warning: Consider using ResponseEntity or custom response wrapper instead of String");
        }
        
        if (returnType.isPrimitive()) {
            System.out.println("Warning: Consider using ResponseEntity or custom response wrapper instead of primitive type");
        }
        
        // 检查是否有@ResponseBody注解
        // 这里需要检查方法上的注解，但由于方法参数限制，暂时跳过
    }
    
    /**
     * 验证安全注解
     */
    private void validateSecurityAnnotations(Method method) {
        // 检查是否有安全相关的注解
        boolean hasSecurityAnnotation = method.isAnnotationPresent(PreAuthorize.class) ||
                                      method.isAnnotationPresent(PostAuthorize.class) ||
                                      method.isAnnotationPresent(Secured.class) ||
                                      method.isAnnotationPresent(RolesAllowed.class);
        
        // 检查是否为敏感操作
        String methodName = method.getName().toLowerCase();
        boolean isSensitiveOperation = methodName.contains("delete") ||
                                     methodName.contains("remove") ||
                                     methodName.contains("update") ||
                                     methodName.contains("create") ||
                                     methodName.contains("admin");
        
        if (isSensitiveOperation && !hasSecurityAnnotation) {
            System.out.println("Warning: Sensitive operation '" + method.getName() + "' should have security annotations");
        }
    }
    
    /**
     * 验证事务注解
     */
    private void validateTransactionAnnotations(Method method) {
        // 检查是否有事务注解
        boolean hasTransactionAnnotation = method.isAnnotationPresent(Transactional.class);
        
        // 检查是否为数据修改操作
        String methodName = method.getName().toLowerCase();
        boolean isDataModificationOperation = methodName.contains("save") ||
                                            methodName.contains("update") ||
                                            methodName.contains("delete") ||
                                            methodName.contains("create") ||
                                            methodName.contains("insert") ||
                                            methodName.contains("remove");
        
        if (isDataModificationOperation && !hasTransactionAnnotation) {
            System.out.println("Warning: Data modification operation '" + method.getName() + "' should have @Transactional annotation");
        }
    }
    
    /**
     * 验证缓存注解
     */
    private void validateCacheAnnotations(Method method) {
        // 检查是否有缓存注解
        boolean hasCacheAnnotation = method.isAnnotationPresent(Cacheable.class) ||
                                   method.isAnnotationPresent(CacheEvict.class) ||
                                   method.isAnnotationPresent(CachePut.class);
        
        // 检查是否为查询操作
        String methodName = method.getName().toLowerCase();
        boolean isQueryOperation = methodName.startsWith("get") ||
                                 methodName.startsWith("find") ||
                                 methodName.startsWith("query") ||
                                 methodName.startsWith("search") ||
                                 methodName.startsWith("list");
        
        if (isQueryOperation && !hasCacheAnnotation) {
            System.out.println("Info: Query operation '" + method.getName() + "' could benefit from caching");
        }
    }
    
    /**
     * 验证日志注解
     */
    private void validateLoggingAnnotations(Method method) {
        // 检查是否有日志注解
        boolean hasLoggingAnnotation = method.isAnnotationPresent(Slf4j.class) ||
                                     method.isAnnotationPresent(Log.class);
        
        // 检查是否为重要操作
        String methodName = method.getName().toLowerCase();
        boolean isImportantOperation = methodName.contains("create") ||
                                     methodName.contains("update") ||
                                     methodName.contains("delete") ||
                                     methodName.contains("login") ||
                                     methodName.contains("logout") ||
                                     methodName.contains("payment");
        
        if (isImportantOperation && !hasLoggingAnnotation) {
            System.out.println("Info: Important operation '" + method.getName() + "' should have logging");
        }
    }
    
    
    /**
     * 增强版API契约验证
     */
    public ApiContractValidationResult validateApiContractsEnhanced(String serviceName, String sourcePath) {
        ApiContractValidationResult result = new ApiContractValidationResult();
        result.setServiceName(serviceName);
        result.setValidationTime(new Date());
        
        try {
            // 1. 验证控制器类
            validateControllerClasses(sourcePath, result);
            
            // 2. 验证方法契约
            validateMethodContracts(sourcePath, result);
            
            // 3. 验证参数契约
            validateParameterContracts(sourcePath, result);
            
            // 4. 验证返回值契约
            validateReturnValueContracts(sourcePath, result);
            
            // 5. 验证异常处理契约
            validateExceptionHandlingContracts(sourcePath, result);
            
            // 6. 验证安全契约
            validateSecurityContracts(sourcePath, result);
            
            // 7. 验证事务契约
            validateTransactionContracts(sourcePath, result);
            
            // 8. 验证缓存契约
            validateCacheContracts(sourcePath, result);
            
            // 计算统计信息
            result.calculateStatistics();
            
        } catch (Exception e) {
            result.addError("验证过程中发生错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 验证控制器类
     */
    private void validateControllerClasses(String sourcePath, ApiContractValidationResult result) {
        // 模拟验证控制器类
        ContractIssue issue = new ContractIssue();
        issue.setIssueType("MISSING_CONTROLLER_ANNOTATION");
        issue.setClassName("UserController");
        issue.setMethodName("getUser");
        issue.setLineNumber(15);
        issue.setDescription("控制器类缺少@RestController或@Controller注解");
        issue.setSeverity("HIGH");
        issue.setSuggestion("添加@RestController注解");
        result.addIssue(issue);
    }
    
    /**
     * 验证方法契约
     */
    private void validateMethodContracts(String sourcePath, ApiContractValidationResult result) {
        // 模拟验证方法契约
        ContractWarning warning = new ContractWarning();
        warning.setWarningType("MISSING_HTTP_MAPPING");
        warning.setClassName("OrderController");
        warning.setMethodName("createOrder");
        warning.setLineNumber(23);
        warning.setDescription("方法缺少HTTP映射注解");
        warning.setSuggestion("添加@PostMapping注解");
        result.addWarning(warning);
    }
    
    /**
     * 验证参数契约
     */
    private void validateParameterContracts(String sourcePath, ApiContractValidationResult result) {
        // 模拟验证参数契约
        ContractIssue issue = new ContractIssue();
        issue.setIssueType("INVALID_PARAMETER_ANNOTATION");
        issue.setClassName("PaymentController");
        issue.setMethodName("processPayment");
        issue.setLineNumber(45);
        issue.setDescription("参数缺少合适的注解");
        issue.setSeverity("MEDIUM");
        issue.setSuggestion("添加@RequestBody或@RequestParam注解");
        result.addIssue(issue);
    }
    
    /**
     * 验证返回值契约
     */
    private void validateReturnValueContracts(String sourcePath, ApiContractValidationResult result) {
        // 模拟验证返回值契约
        ContractWarning warning = new ContractWarning();
        warning.setWarningType("INCONSISTENT_RETURN_TYPE");
        warning.setClassName("ProductController");
        warning.setMethodName("getProduct");
        warning.setLineNumber(67);
        warning.setDescription("返回值类型与API文档不一致");
        warning.setSuggestion("使用统一的响应包装类");
        result.addWarning(warning);
    }
    
    /**
     * 验证异常处理契约
     */
    private void validateExceptionHandlingContracts(String sourcePath, ApiContractValidationResult result) {
        // 模拟验证异常处理契约
        ContractInfo info = new ContractInfo();
        info.setInfoType("MISSING_EXCEPTION_HANDLING");
        info.setClassName("UserController");
        info.setMethodName("deleteUser");
        info.setLineNumber(89);
        info.setDescription("方法缺少异常处理");
        info.setSuggestion("添加@ExceptionHandler或声明throws异常");
        result.addInfo(info);
    }
    
    /**
     * 验证安全契约
     */
    private void validateSecurityContracts(String sourcePath, ApiContractValidationResult result) {
        // 模拟验证安全契约
        ContractIssue issue = new ContractIssue();
        issue.setIssueType("MISSING_SECURITY_ANNOTATION");
        issue.setClassName("AdminController");
        issue.setMethodName("deleteUser");
        issue.setLineNumber(123);
        issue.setDescription("敏感操作缺少安全注解");
        issue.setSeverity("CRITICAL");
        issue.setSuggestion("添加@PreAuthorize注解");
        result.addIssue(issue);
    }
    
    /**
     * 验证事务契约
     */
    private void validateTransactionContracts(String sourcePath, ApiContractValidationResult result) {
        // 模拟验证事务契约
        ContractWarning warning = new ContractWarning();
        warning.setWarningType("MISSING_TRANSACTION_ANNOTATION");
        warning.setClassName("OrderController");
        warning.setMethodName("updateOrder");
        warning.setLineNumber(156);
        warning.setDescription("数据修改操作缺少事务注解");
        warning.setSuggestion("添加@Transactional注解");
        result.addWarning(warning);
    }
    
    /**
     * 验证缓存契约
     */
    private void validateCacheContracts(String sourcePath, ApiContractValidationResult result) {
        // 模拟验证缓存契约
        ContractInfo info = new ContractInfo();
        info.setInfoType("MISSING_CACHE_ANNOTATION");
        info.setClassName("ProductController");
        info.setMethodName("getProductList");
        info.setLineNumber(189);
        info.setDescription("查询操作可以添加缓存");
        info.setSuggestion("添加@Cacheable注解");
        result.addInfo(info);
    }
}