package com.example.nh.client.config;

import com.example.nh.client.DefaultServiceInstanceListSupplier;
import com.example.nh.client.LoadBalancerClientSpecification;
import com.example.nh.client.factory.LoadBalancerClientFactory;
import com.example.nh.client.intercepter.LoadBalancerInterceptor;
import com.example.nh.client.session.StickySessionLoadBalancerClient;
import com.example.nh.client.session.StickySessionProperties;
import com.example.nh.lb.cache.LoadBalancerCache;
import com.example.nh.lb.core.LoadBalancer;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({LoadBalancerProperties.class, StickySessionProperties.class})
public class LoadBalancerClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServiceInstanceListSupplier serviceInstanceListSupplier(LoadBalancerCache loadBalancerCache) {
        return new DefaultServiceInstanceListSupplier(loadBalancerCache);
    }

    @Bean
    public LoadBalancerInterceptor loadBalancerInterceptor(LoadBalancerClientFactory loadBalancerFactory) {
        return new LoadBalancerInterceptor(loadBalancerFactory);
    }

    @Bean
    public LoadBalancerClientSpecification loadBalancerClientSpecification() {
        return new LoadBalancerClientSpecification("default", new Class<?>[]{LoadBalancerClientConfiguration.class});
    }

    @Bean
    @ConditionalOnProperty(prefix = "loadbalancer.sticky-session", name = "enabled", havingValue = "true")
    public StickySessionLoadBalancerClient stickySessionLoadBalancerClient(LoadBalancer loadBalancer,
        StickySessionProperties properties) {
        return new StickySessionLoadBalancerClient(loadBalancer, properties);
    }

    @Configuration(proxyBeanMethods = false)
    public static class LoadBalancerClientSpecificationConfiguration {

        @Autowired(required = false)
        private List<LoadBalancerClientSpecification> configurations = new ArrayList<>();

        @Bean
        public LoadBalancerClientFactory loadBalancerClientFactory() {
            LoadBalancerClientFactory factory = new LoadBalancerClientFactory();
            factory.setConfigurations(this.configurations);
            return factory;
        }
    }

    @Configuration
    @ConditionalOnClass(RestTemplate.class)
    @EnableConfigurationProperties(LoadBalancerProperties.class)
    @AutoConfigureBefore(LoadBalancerClientConfiguration.class)
    public static class LoadBalancerAutoConfiguration {

        @Configuration(proxyBeanMethods = false)
        @Conditional(LoadBalancerRestTemplateCondition.class)
        static class LoadBalancerRestTemplateConfiguration {
            @Bean
            LoadBalanceRestTemplateConfiguration loadBalancedRestTemplateConfiguration() {
                return new LoadBalanceRestTemplateConfiguration();
            }
        }

        @Configuration(proxyBeanMethods = false)
        @ConditionalOnClass(WebClient.class)
        static class LoadBalancerWebClientConfiguration {
            @Bean
            LoadBalancedWebClientConfiguration loadBalancedWebClientConfiguration() {
                return new LoadBalancedWebClientConfiguration();
            }
        }
    }

    public static class LoadBalancerRestTemplateCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return ClassUtils.isPresent("org.springframework.web.client.RestTemplate",
                context.getClassLoader());        }
    }
}
