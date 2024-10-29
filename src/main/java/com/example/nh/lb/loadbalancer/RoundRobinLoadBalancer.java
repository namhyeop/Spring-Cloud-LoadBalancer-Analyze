package com.example.nh.lb.loadbalancer;

import com.example.nh.lb.cache.LoadBalancerCache;
import com.example.nh.lb.core.AbstractLoadBalancer;
import com.example.nh.lb.core.ServiceInstance;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import com.example.nh.lb.health.HealthCheckService;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import reactor.core.publisher.Mono;

public class RoundRobinLoadBalancer extends AbstractLoadBalancer {
    private final AtomicInteger position = new AtomicInteger(0);

    public RoundRobinLoadBalancer(
        ServiceInstanceListSupplier supplier,
        LoadBalancerCache cache,
        HealthCheckService healthCheckService
    ) {
        super(supplier, cache, healthCheckService);
    }

    @Override
    protected Mono<ServiceInstance> chooseInstance(List<ServiceInstance> instances, Request request) {
        if (instances.isEmpty()) {
            return Mono.empty();
        }
        int pos = Math.abs(position.incrementAndGet());
        int index = pos % instances.size();
        ServiceInstance instance = instances.get(index);
        return Mono.just(instance);
    }
}
