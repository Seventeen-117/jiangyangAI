package io.metersphere.whitebox.validator;

import io.metersphere.whitebox.tester.ServiceUrlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CodeStyleValidator extends BaseValidator {
    
    @Autowired
    private ServiceUrlConfig serviceUrlConfig;
    
    // 命名规范模式
    private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^[A-Z][a-zA-Z0-9]*$");
    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern VARIABLE_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_]*$");
    
    // 代码风格检查模式
    private static final Pattern TAB_INDENTATION = Pattern.compile("^\\t+.*$");
    private static final Pattern SPACE_INDENTATION = Pattern.compile("^ +.*$");
    private static final Pattern MAGIC_NUMBER = Pattern.compile(".*[^a-zA-Z]([0-9]+\\.[0-9]+|[0-9]+)[^a-zA-Z].*");
    
    public StyleValidationResult validateCodeStyle(String serviceName) {
        return executeValidation(serviceName, StyleValidationResult::new, result -> {
            // 获取服务源代码路径
            String servicePath = getServiceSourcePath(serviceName);
            
            // 执行代码风格检查
            List<String> validationErrors = checkCodeStyle(servicePath);
            
            // 设置检查结果
            result.setTotalChecks(50); // 假设总检查项为50
            result.setFailedChecks(validationErrors.size());
            result.setPassedChecks(result.getTotalChecks() - result.getFailedChecks());
            
            // 添加验证详情
            result.setValidationErrors(validationErrors);
        });
    }
    
    private String getServiceSourcePath(String serviceName) {
        // 从配置中获取服务源代码路径，如果没有配置则使用默认路径
        if (serviceName == null || serviceName.isEmpty()) {
            return "src/main/java";
        }
        
        return serviceUrlConfig.getSourcePathOrDefault(serviceName, "../" + serviceName + "/src/main/java");
    }
    
    private List<String> checkCodeStyle(String sourcePath) throws IOException {
        List<String> validationErrors = new ArrayList<>();
        
        Path serviceDir = Paths.get(sourcePath);
        if (Files.exists(serviceDir)) {
            Files.walk(serviceDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        validateJavaFile(path, validationErrors);
                    } catch (IOException e) {
                        validationErrors.add("Error reading file " + path.toString() + ": " + e.getMessage());
                    }
                });
        }
        
        return validationErrors;
    }
    
    private void validateJavaFile(Path filePath, List<String> validationErrors) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            int lineNumber = 0;
            String fileName = filePath.getFileName().toString();
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 检查行长度
                if (line.length() > 120) {
                    validationErrors.add(fileName + ":" + lineNumber + " - Line exceeds 120 characters");
                }
                
                // 检查缩进一致性
                if (!line.trim().isEmpty()) {
                    if (TAB_INDENTATION.matcher(line).matches() && SPACE_INDENTATION.matcher(line).matches()) {
                        validationErrors.add(fileName + ":" + lineNumber + " - Inconsistent indentation (mix of tabs and spaces)");
                    }
                }
                
                // 检查命名规范
                checkNamingConventions(line, fileName, lineNumber, validationErrors);
                
                // 检查魔法数字
                checkMagicNumbers(line, fileName, lineNumber, validationErrors);
                
                // 检查缺少大括号
                checkMissingBraces(line, fileName, lineNumber, validationErrors);
            }
        }
    }
    
    private void checkNamingConventions(String line, String fileName, int lineNumber, List<String> validationErrors) {
        // 检查类名命名规范
        if (line.contains("class ")) {
            int classIndex = line.indexOf("class ");
            int nameStart = classIndex + 6;
            while (nameStart < line.length() && Character.isWhitespace(line.charAt(nameStart))) {
                nameStart++;
            }
            
            int nameEnd = nameStart;
            while (nameEnd < line.length() && (Character.isLetterOrDigit(line.charAt(nameEnd)) || line.charAt(nameEnd) == '_')) {
                nameEnd++;
            }
            
            if (nameStart < nameEnd) {
                String className = line.substring(nameStart, nameEnd);
                if (!CLASS_NAME_PATTERN.matcher(className).matches()) {
                    validationErrors.add(fileName + ":" + lineNumber + " - Class name '" + className + "' does not follow camel case convention");
                }
            }
        }
        
        // 检查方法名命名规范
        if (line.contains("public ") || line.contains("private ") || line.contains("protected ")) {
            if (line.contains("(") && line.contains(")") && !line.contains("class ")) {
                // 提取方法名
                int nameStart = line.lastIndexOf(" ", line.indexOf("("));
                if (nameStart != -1) {
                    nameStart++;
                    int nameEnd = line.indexOf("(", nameStart);
                    if (nameEnd != -1) {
                        String methodName = line.substring(nameStart, nameEnd);
                        if (!METHOD_NAME_PATTERN.matcher(methodName).matches()) {
                            validationErrors.add(fileName + ":" + lineNumber + " - Method name '" + methodName + "' does not follow camel case convention");
                        }
                    }
                }
            }
        }
    }
    
    private void checkMagicNumbers(String line, String fileName, int lineNumber, List<String> validationErrors) {
        // 检查魔法数字（除了-1, 0, 1, 2, 10, 100等常见数字）
        if (MAGIC_NUMBER.matcher(line).matches()) {
            // 提取数字
            String[] parts = line.split("[^0-9.]+");
            for (String part : parts) {
                if (!part.isEmpty()) {
                    try {
                        double value = Double.parseDouble(part);
                        // 忽略常见的小数字
                        if (value != -1 && value != 0 && value != 1 && value != 2 && value != 10 && value != 100) {
                            validationErrors.add(fileName + ":" + lineNumber + " - Magic number '" + part + "' found, should be defined as a constant");
                        }
                    } catch (NumberFormatException e) {
                        // 忽略无法解析的数字
                    }
                }
            }
        }
    }
    
    private void checkMissingBraces(String line, String fileName, int lineNumber, List<String> validationErrors) {
        // 检查if语句缺少大括号
        if (line.trim().startsWith("if ") || line.trim().startsWith("if(")) {
            if (!line.contains("{")) {
                validationErrors.add(fileName + ":" + lineNumber + " - Missing braces in if statement");
            }
        }
        
        // 检查for循环缺少大括号
        if (line.trim().startsWith("for ") || line.trim().startsWith("for(")) {
            if (!line.contains("{")) {
                validationErrors.add(fileName + ":" + lineNumber + " - Missing braces in for loop");
            }
        }
        
        // 检查while循环缺少大括号
        if (line.trim().startsWith("while ") || line.trim().startsWith("while(")) {
            if (!line.contains("{")) {
                validationErrors.add(fileName + ":" + lineNumber + " - Missing braces in while loop");
            }
        }
    }
}