package com.example.nh.lb.config;

import com.example.nh.lb.cache.LoadBalancerCache;
import com.example.nh.lb.core.LoadBalancer;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import com.example.nh.lb.health.HealthCheckService;
import com.example.nh.lb.loadbalancer.RoundRobinLoadBalancer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoundRobinLoadBalancerConfig {

    @Bean
    public LoadBalancer RoundRobinloadBalancer(ServiceInstanceListSupplier supplier, LoadBalancerCache cache,
        HealthCheckService healthCheckService) {
        return new RoundRobinLoadBalancer(supplier, cache, healthCheckService);
    }
}
