package com.example.nh.client.factory;

import com.example.nh.client.LoadBalancerClientSpecification;
import com.example.nh.client.config.LoadBalancerClientConfiguration;
import com.example.nh.lb.core.LoadBalancer;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

@Slf4j
public class LoadBalancerClientFactory extends NamedContextFactory<LoadBalancerClientSpecification> {

    private static final String PROPERTY_NAME = "loadbalancer.client.name";

    public LoadBalancerClientFactory() {
        super(LoadBalancerClientConfiguration.class, "loadbalancer", PROPERTY_NAME);
    }

    @Override
    protected GenericApplicationContext createContext(String name) {
        GenericApplicationContext context = super.createContext(name);
        return context;
    }

    protected void setEnvironmentForContext(AnnotationConfigApplicationContext context, String name) {
        ConfigurableEnvironment env = context.getEnvironment();
        env.getPropertySources().addFirst(
            new MapPropertySource("loadbalancer",
                Collections.singletonMap("loadbalancer.client.name", name))
        );
    }

    public LoadBalancer getInstance(String name) {
        return getInstance(name, LoadBalancer.class);
    }

    public ServiceInstanceListSupplier getInstanceSupplier(String name) {
        return getInstance(name, ServiceInstanceListSupplier.class);
    }

    public <T> Map<String, T> getInstances(String name, Class<T> type) {
        return getContext(name).getBeansOfType(type);
    }

    @Override
    public void setConfigurations(List<LoadBalancerClientSpecification> configurations) {
        super.setConfigurations(configurations);
    }
}
