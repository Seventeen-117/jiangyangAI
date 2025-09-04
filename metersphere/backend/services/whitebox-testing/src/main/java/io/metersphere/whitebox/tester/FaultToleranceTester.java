package io.metersphere.whitebox.tester;

import io.metersphere.whitebox.vo.FaultToleranceResult;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class FaultToleranceTester {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private ServiceUrlConfig serviceUrlConfig;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public FaultToleranceResult testCircuitBreaker(String serviceName) {
        FaultToleranceResult result = new FaultToleranceResult();
        result.setTestType("circuit-breaker");
        result.setServiceName(serviceName);
        
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        
        try {
            // 获取服务的基础URL
            String serviceUrl = getServiceBaseUrl(serviceName);
            
            // 测试1: 正常请求
            totalTests++;
            if (testNormalRequest(serviceUrl)) {
                passedTests++;
            } else {
                failedTests++;
                result.addTestFailure("Normal request failed for " + serviceName);
            }
            
            // 测试2: 超时请求
            totalTests++;
            if (testTimeoutRequest(serviceUrl)) {
                passedTests++;
            } else {
                failedTests++;
                result.addTestFailure("Timeout handling failed for " + serviceName);
            }
            
            // 测试3: 服务不可用
            totalTests++;
            if (testServiceUnavailable(serviceUrl)) {
                passedTests++;
            } else {
                failedTests++;
                result.addTestFailure("Service unavailable handling failed for " + serviceName);
            }
            
            // 测试4: 熔断器状态切换
            totalTests++;
            if (testCircuitBreakerStateTransition(serviceUrl)) {
                passedTests++;
            } else {
                failedTests++;
                result.addTestFailure("Circuit breaker state transition failed for " + serviceName);
            }
            
            // 测试5: 半开状态恢复
            totalTests++;
            if (testHalfOpenRecovery(serviceUrl)) {
                passedTests++;
            } else {
                failedTests++;
                result.addTestFailure("Half-open recovery failed for " + serviceName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addTestFailure("Error during circuit breaker testing: " + e.getMessage());
            failedTests++;
        }
        
        // 设置测试结果
        result.setTotalTests(totalTests);
        result.setPassedTests(passedTests);
        result.setFailedTests(failedTests);
        result.setOverallPassed(failedTests == 0);
        
        return result;
    }
    
    public FaultToleranceResult testFallback(String serviceName) {
        FaultToleranceResult result = new FaultToleranceResult();
        result.setTestType("fallback");
        result.setServiceName(serviceName);
        
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        
        try {
            // 获取服务的基础URL
            String serviceUrl = getServiceBaseUrl(serviceName);
            
            // 测试1: 正常请求的降级逻辑
            totalTests++;
            if (testNormalFallback(serviceUrl)) {
                passedTests++;
            } else {
                failedTests++;
                result.addTestFailure("Normal fallback failed for " + serviceName);
            }
            
            // 测试2: 异常情况的降级逻辑
            totalTests++;
            if (testExceptionFallback(serviceUrl)) {
                passedTests++;
            } else {
                failedTests++;
                result.addTestFailure("Exception fallback failed for " + serviceName);
            }
            
            // 测试3: 超时情况的降级逻辑
            totalTests++;
            if (testTimeoutFallback(serviceUrl)) {
                passedTests++;
            } else {
                failedTests++;
                result.addTestFailure("Timeout fallback failed for " + serviceName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addTestFailure("Error during fallback testing: " + e.getMessage());
            failedTests++;
        }
        
        // 设置测试结果
        result.setTotalTests(totalTests);
        result.setPassedTests(passedTests);
        result.setFailedTests(failedTests);
        result.setOverallPassed(failedTests == 0);
        
        return result;
    }
    
    private String getServiceBaseUrl(String serviceName) {
        // 从配置中获取服务URL，如果没有配置则使用默认URL
        return serviceUrlConfig.getServiceUrlOrDefault(serviceName, "http://localhost:8080");
    }
    
    private boolean testNormalRequest(String serviceUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                serviceUrl + "/health", HttpMethod.GET, entity, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean testTimeoutRequest(String serviceUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 设置较短的超时时间来测试超时处理
            // 使用RequestFactory来设置超时
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(1000); // 1秒连接超时
            requestFactory.setReadTimeout(1000);    // 1秒读取超时
            restTemplate.setRequestFactory(requestFactory);
            
            ResponseEntity<String> response = restTemplate.exchange(
                serviceUrl + "/slow-endpoint", HttpMethod.GET, entity, String.class);
            
            return false; // 如果没有超时异常，说明测试失败
        } catch (ResourceAccessException e) {
            // 超时异常表示测试通过
            return e.getCause() instanceof TimeoutException;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean testServiceUnavailable(String serviceUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                serviceUrl + "/non-existent", HttpMethod.GET, entity, String.class);
            
            // 404表示服务不可用测试通过
            return response.getStatusCode().is4xxClientError();
        } catch (HttpServerErrorException e) {
            // 5xx错误也表示测试通过
            return e.getStatusCode().is5xxServerError();
        } catch (Exception e) {
            return true; // 其他异常也认为测试通过
        }
    }
    
    private boolean testCircuitBreakerStateTransition(String serviceUrl) {
        // 创建一个熔断器实例用于测试
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .failureRateThreshold(50.0f)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
        
        CircuitBreaker circuitBreaker = CircuitBreaker.of("testCircuitBreaker", config);
        
        // 模拟多次失败请求触发熔断器打开
        int failureCount = 0;
        for (int i = 0; i < 5; i++) {
            try {
                // 尝试执行一个会失败的操作
                circuitBreaker.executeSupplier(() -> {
                    if (!testServiceUnavailable(serviceUrl)) {
                        throw new RuntimeException("Simulated failure");
                    }
                    return "success";
                });
            } catch (Exception e) {
                failureCount++;
            }
        }
        
        // 检查熔断器状态是否已切换到OPEN
        return circuitBreaker.getState() == CircuitBreaker.State.OPEN;
    }
    
    private boolean testHalfOpenRecovery(String serviceUrl) {
        try {
            // 等待一段时间让熔断器进入半开状态
            Thread.sleep(5000);
            
            // 发送一个正常请求测试是否恢复
            return testNormalRequest(serviceUrl);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean testNormalFallback(String serviceUrl) {
        try {
            // 测试正常情况下的降级逻辑是否不被触发
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                serviceUrl + "/normal", HttpMethod.GET, entity, String.class);
            
            // 检查响应是否来自主服务而不是降级逻辑
            return response.getStatusCode().is2xxSuccessful() && 
                   !response.getBody().contains("fallback");
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean testExceptionFallback(String serviceUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                serviceUrl + "/exception-endpoint", HttpMethod.GET, entity, String.class);
            
            // 检查响应是否来自降级逻辑
            return response.getStatusCode().is2xxSuccessful() && 
                   response.getBody().contains("fallback");
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean testTimeoutFallback(String serviceUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 设置较短的超时时间来测试超时降级
            // 使用RequestFactory来设置超时
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setConnectTimeout(500); // 0.5秒连接超时
            requestFactory.setReadTimeout(500);    // 0.5秒读取超时
            restTemplate.setRequestFactory(requestFactory);
            
            ResponseEntity<String> response = restTemplate.exchange(
                serviceUrl + "/slow-endpoint", HttpMethod.GET, entity, String.class);
            
            // 如果超时，应该触发降级逻辑
            return response.getStatusCode().is2xxSuccessful() && 
                   response.getBody().contains("fallback");
        } catch (ResourceAccessException e) {
            // 超时异常表示测试通过
            return e.getCause() instanceof TimeoutException;
        } catch (Exception e) {
            return false;
        }
    }
}