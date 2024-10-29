package com.example.nh.client.annotation;

import com.example.nh.client.config.LoadBalancerClientConfigurationRegistrar;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Configuration(proxyBeanMethods = false)
@Import(LoadBalancerClientConfigurationRegistrar.class)
public @interface LoadBalancerClients {
    LoadBalancerClient[] value();
}
