package com.example.nh.lb.config;

import com.example.nh.lb.cache.LoadBalancerCache;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import com.example.nh.lb.health.HealthCheckService;
import java.util.Collections;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
public class LoadBalancerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HealthCheckService healthCheckService(WebClient.Builder webClientBuilder,
        ServiceInstanceListSupplier serviceInstanceListSupplier) {
        return new HealthCheckService(webClientBuilder, serviceInstanceListSupplier);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancerCache loadBalancerCache(CacheManager cacheManager) {
        return new LoadBalancerCache(cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheManager loadBalancerCacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Collections.singletonList(
            new ConcurrentMapCache("loadbalancer")
        ));
        return cacheManager;
    }
}
