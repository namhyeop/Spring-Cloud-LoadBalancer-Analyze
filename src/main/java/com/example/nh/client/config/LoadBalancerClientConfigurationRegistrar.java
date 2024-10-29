package com.example.nh.client.config;

import com.example.nh.client.LoadBalancerClientSpecification;
import com.example.nh.client.annotation.LoadBalancerClient;
import com.example.nh.client.annotation.LoadBalancerClients;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

@Slf4j
public class LoadBalancerClientConfigurationRegistrar implements ImportBeanDefinitionRegistrar {

    private static final String CLIENTS_ANNOTATION_NAME = LoadBalancerClients.class.getName();
    private static final String CLIENT_ANNOTATION_NAME = LoadBalancerClient.class.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        registerLoadBalancerClients(metadata, registry);
        registerLoadBalancerClient(metadata, registry);
    }

    private void registerLoadBalancerClients(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (!metadata.hasAnnotation(CLIENTS_ANNOTATION_NAME)) {
            return;
        }

        Map<String, Object> attrs = getAnnotationAttributes(metadata, CLIENTS_ANNOTATION_NAME);
        if (attrs == null || !attrs.containsKey("value")) {
            return;
        }

        AnnotationAttributes[] clients = (AnnotationAttributes[]) attrs.get("value");
        for (AnnotationAttributes client : clients) {
            String clientName = client.getString("name");
            Class<?>[] configuration = client.getClassArray("configuration");
            registerClientConfiguration(registry, clientName, configuration);
        }
    }

    private void registerLoadBalancerClient(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (!metadata.hasAnnotation(CLIENT_ANNOTATION_NAME)) {
            return;
        }

        Map<String, Object> attrs = getAnnotationAttributes(metadata, CLIENT_ANNOTATION_NAME);
        if (attrs == null) {
            return;
        }

        String clientName = getClientName(attrs);
        Class<?>[] configuration = (Class<?>[]) attrs.get("configuration");
        registerClientConfiguration(registry, clientName, configuration);
    }

    private Map<String, Object> getAnnotationAttributes(AnnotationMetadata metadata, String annotationName) {
        return metadata.getAnnotationAttributes(annotationName);
    }

    private String getClientName(Map<String, Object> attrs) {
        String name = extractNameFromAttributes(attrs);
        validateClientName(name);
        return name;
    }

    private String extractNameFromAttributes(Map<String, Object> attrs) {
        String name = (String) attrs.get("name");
        if (!StringUtils.hasText(name)) {
            name = (String) attrs.get("value");
        }
        return name;
    }

    private void validateClientName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalStateException("@LoadBalanceClient 사용시 서비스 이름과 로드밸런싱 전략은 필수입니다.");
        }
    }

    private void registerClientConfiguration(BeanDefinitionRegistry registry, String name, Class<?>[] configuration) {
        BeanDefinition beanDefinition = createClientSpecificationBeanDefinition(name, configuration);
        String beanName = generateBeanName(name);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private BeanDefinition createClientSpecificationBeanDefinition(String name, Class<?>[] configuration) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(
            LoadBalancerClientSpecification.class);
        builder.addConstructorArgValue(name);
        builder.addConstructorArgValue(configuration);
        return builder.getBeanDefinition();
    }

    private String generateBeanName(String clientName) {
        return clientName + ".LoadBalancerClientSpecification";
    }
}
