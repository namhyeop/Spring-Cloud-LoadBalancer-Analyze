package com.example.nh.lb.config;

import com.example.nh.lb.cache.LoadBalancerCache;
import com.example.nh.lb.core.LoadBalancer;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import com.example.nh.lb.health.HealthCheckService;
import com.example.nh.lb.loadbalancer.RandomLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RandomLoadBalancerConfig {

    @Bean
    public LoadBalancer randomLoadBalancer(ServiceInstanceListSupplier supplier, LoadBalancerCache cache,
        HealthCheckService healthCheckService) {
        return new RandomLoadBalancer(supplier, cache, healthCheckService);
    }
}
