package com.example.nh.lb.loadbalancer;

import com.example.nh.lb.cache.LoadBalancerCache;
import com.example.nh.lb.core.AbstractLoadBalancer;
import com.example.nh.lb.core.ServiceInstance;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import com.example.nh.lb.health.HealthCheckService;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import reactor.core.publisher.Mono;

public class RandomLoadBalancer extends AbstractLoadBalancer {

    public RandomLoadBalancer(ServiceInstanceListSupplier supplier, LoadBalancerCache cache,
        HealthCheckService healthCheckService) {
        super(supplier, cache, healthCheckService);
    }

    @Override
    protected Mono<ServiceInstance> chooseInstance(List<ServiceInstance> instances,
        Request request) {
        if (instances.isEmpty()) {
            return Mono.empty();
        }
        ServiceInstance instance = instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
        return Mono.just(instance);
    }
}
