package com.example.nh.client.factory;

import com.example.nh.client.LoadBalancerClientSpecification;
import com.example.nh.client.config.LoadBalancerClientConfiguration;
import com.example.nh.lb.core.LoadBalancer;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.GenericApplicationContext;

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

    public LoadBalancer getInstance(String name) {
        return getInstance(name, LoadBalancer.class);
    }

    @Override
    public void setConfigurations(List<LoadBalancerClientSpecification> configurations) {
        super.setConfigurations(configurations);
    }
}
