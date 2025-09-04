package io.metersphere.whitebox.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import java.lang.reflect.Field;

@Component
public class DependencyInjectionValidator extends BaseValidator {
    
    public DependencyValidationResult validateDependencies(String serviceName) {
        return executeValidation(serviceName, DependencyValidationResult::new, result -> {
            // 获取Spring应用上下文中的Bean工厂
            ConfigurableListableBeanFactory beanFactory = 
                ((ConfigurableApplicationContext) applicationContext).getBeanFactory();
            
            // 获取所有Bean定义
            String[] beanNames = beanFactory.getBeanDefinitionNames();
            
            // 遍历所有Bean
            for (String beanName : beanNames) {
                // 获取Bean定义
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
                String beanClassName = beanDefinition.getBeanClassName();
                
                // 如果Bean类名包含服务名称
                if (beanClassName != null && isServiceRelated(beanClassName, serviceName)) {
                    // 检查该Bean的依赖注入情况
                    Class<?> beanClass = Class.forName(beanClassName);
                    Field[] fields = beanClass.getDeclaredFields();
                    
                    for (Field field : fields) {
                        // 检查是否有@Autowired注解
                        if (field.isAnnotationPresent(Autowired.class)) {
                            result.setTotalChecks(result.getTotalChecks() + 1);
                            
                            try {
                                // 尝试获取该依赖的Bean
                                Object dependencyBean = applicationContext.getBean(field.getType());
                                if (dependencyBean != null) {
                                    result.setPassedChecks(result.getPassedChecks() + 1);
                                } else {
                                    result.setFailedChecks(result.getFailedChecks() + 1);
                                    result.addValidationError("Unsatisfied dependency: " + beanClassName + "." + field.getName());
                                }
                            } catch (NoSuchBeanDefinitionException e) {
                                result.setFailedChecks(result.getFailedChecks() + 1);
                                result.addValidationError("Unsatisfied dependency: " + beanClassName + "." + field.getName());
                            }
                        }
                    }
                }
            }
        });
    }
}