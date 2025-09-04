package io.metersphere.whitebox.analyzer;

import io.metersphere.whitebox.vo.CommentMetrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.metersphere.whitebox.tester.ServiceUrlConfig;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

@Component
public class CommentAnalyzer {
    
    @Autowired
    private ServiceUrlConfig serviceUrlConfig;
    
    // 注释模式
    private static final Pattern SINGLE_LINE_COMMENT = Pattern.compile("^\\s*//.*$");
    private static final Pattern MULTI_LINE_COMMENT_START = Pattern.compile("^\\s*/\\*.*$");
    private static final Pattern MULTI_LINE_COMMENT_END = Pattern.compile(".*\\*/\\s*$");
    private static final Pattern JAVADOC_COMMENT_START = Pattern.compile("^\\s*/\\*\\*.*$");
    
    public CommentMetrics analyzeComments(String serviceName) {
        CommentMetrics metrics = new CommentMetrics();
        metrics.setServiceName(serviceName);
        
        try {
            // 获取服务源代码路径
            String servicePath = getServiceSourcePath(serviceName);
            
            // 统计注释和代码行数
            CommentStatistics stats = countCommentsAndLines(servicePath);
            
            // 设置统计结果
            metrics.setTotalLines(stats.totalLines);
            metrics.setCommentedLines(stats.commentedLines);
            metrics.setComplexMethodsWithoutComments(stats.complexMethodsWithoutComments);
            
            if (metrics.getTotalLines() > 0) {
                double coverage = (double) metrics.getCommentedLines() / metrics.getTotalLines() * 100;
                metrics.setCommentCoverage(coverage);
            }
            
            // 设置是否符合标准（注释覆盖率>=80%）
            metrics.setMeetsStandard(metrics.getCommentCoverage() >= 80.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return metrics;
    }
    
    private String getServiceSourcePath(String serviceName) {
        // 从配置中获取服务源代码路径，如果没有配置则使用默认路径
        if (serviceName == null || serviceName.isEmpty()) {
            return "src/main/java";
        }
        
        return serviceUrlConfig.getSourcePathOrDefault(serviceName, "../" + serviceName + "/src/main/java");
    }
    
    private CommentStatistics countCommentsAndLines(String sourcePath) throws IOException {
        CommentStatistics stats = new CommentStatistics();
        
        Path serviceDir = Paths.get(sourcePath);
        if (Files.exists(serviceDir)) {
            Files.walk(serviceDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    try {
                        analyzeJavaFile(path, stats);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
        
        return stats;
    }
    
    private void analyzeJavaFile(Path filePath, CommentStatistics stats) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            boolean inMultiLineComment = false;
            int currentMethodComplexity = 0;
            boolean inMethod = false;
            
            while ((line = reader.readLine()) != null) {
                stats.totalLines++;
                
                // 检查是否在多行注释中
                if (inMultiLineComment) {
                    stats.commentedLines++;
                    if (MULTI_LINE_COMMENT_END.matcher(line).matches()) {
                        inMultiLineComment = false;
                    }
                    continue;
                }
                
                // 检查单行注释
                if (SINGLE_LINE_COMMENT.matcher(line).matches()) {
                    stats.commentedLines++;
                    continue;
                }
                
                // 检查多行注释开始
                if (MULTI_LINE_COMMENT_START.matcher(line).matches() || 
                    JAVADOC_COMMENT_START.matcher(line).matches()) {
                    stats.commentedLines++;
                    if (!MULTI_LINE_COMMENT_END.matcher(line).matches()) {
                        inMultiLineComment = true;
                    }
                    continue;
                }
                
                // 检查方法定义
                if (line.contains("public ") || line.contains("private ") || line.contains("protected ")) {
                    if (line.contains("(") && line.contains(")") && line.contains("{")) {
                        inMethod = true;
                        currentMethodComplexity = calculateMethodComplexity(line);
                    }
                }
                
                // 检查方法结束
                if (inMethod && line.contains("}")) {
                    // 如果方法复杂度大于阈值且没有足够的注释，则计为复杂方法缺少注释
                    if (currentMethodComplexity > 10) {
                        stats.complexMethodsWithoutComments++;
                    }
                    inMethod = false;
                    currentMethodComplexity = 0;
                }
                
                // 计算方法复杂度
                if (inMethod) {
                    currentMethodComplexity += calculateLineComplexity(line);
                }
            }
        }
    }
    
    private int calculateMethodComplexity(String methodSignature) {
        // 根据方法签名计算基础复杂度
        int complexity = 1; // 基础复杂度
        
        // 检查参数数量
        int paramCount = 0;
        int start = methodSignature.indexOf('(');
        int end = methodSignature.indexOf(')');
        if (start != -1 && end != -1 && end > start) {
            String params = methodSignature.substring(start + 1, end);
            if (!params.trim().isEmpty()) {
                paramCount = params.split(",").length;
            }
        }
        
        // 参数越多，复杂度越高
        complexity += paramCount / 2;
        
        return complexity;
    }
    
    private int calculateLineComplexity(String line) {
        int complexity = 0;
        
        // 检查条件语句
        if (line.contains("if ") || line.contains("if(")) complexity++;
        if (line.contains("else ")) complexity++;
        if (line.contains("for ") || line.contains("for(")) complexity++;
        if (line.contains("while ") || line.contains("while(")) complexity++;
        if (line.contains("switch ") || line.contains("switch(")) complexity++;
        if (line.contains("catch ") || line.contains("catch(")) complexity++;
        if (line.contains("&&") || line.contains("||")) complexity++;
        
        // 检查三元运算符
        if (line.contains("?") && line.contains(":")) complexity++;
        
        return complexity;
    }
    
    // 内部类用于统计注释信息
    private static class CommentStatistics {
        int totalLines = 0;
        int commentedLines = 0;
        int complexMethodsWithoutComments = 0;
    }
}