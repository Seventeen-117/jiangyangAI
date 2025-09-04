package io.metersphere.whitebox.tester;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "whitebox.services")
public class ServiceUrlConfig {
    
    private Map<String, String> urls = new HashMap<>();
    private Map<String, String> sourcePaths = new HashMap<>();
    private Map<String, String> classPaths = new HashMap<>();
    
    public Map<String, String> getUrls() {
        return urls;
    }
    
    public void setUrls(Map<String, String> urls) {
        this.urls = urls;
    }
    
    public String getServiceUrl(String serviceName) {
        return urls.get(serviceName.toLowerCase());
    }
    
    public String getServiceUrlOrDefault(String serviceName, String defaultUrl) {
        return urls.getOrDefault(serviceName.toLowerCase(), defaultUrl);
    }
    
    public Map<String, String> getSourcePaths() {
        return sourcePaths;
    }
    
    public void setSourcePaths(Map<String, String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }
    
    public String getSourcePath(String serviceName) {
        return sourcePaths.get(serviceName.toLowerCase());
    }
    
    public String getSourcePathOrDefault(String serviceName, String defaultPath) {
        return sourcePaths.getOrDefault(serviceName.toLowerCase(), defaultPath);
    }
    
    public Map<String, String> getClassPaths() {
        return classPaths;
    }
    
    public void setClassPaths(Map<String, String> classPaths) {
        this.classPaths = classPaths;
    }
    
    public String getClassPath(String serviceName) {
        return classPaths.get(serviceName.toLowerCase());
    }
    
    public String getClassPathOrDefault(String serviceName, String defaultPath) {
        return classPaths.getOrDefault(serviceName.toLowerCase(), defaultPath);
    }
}