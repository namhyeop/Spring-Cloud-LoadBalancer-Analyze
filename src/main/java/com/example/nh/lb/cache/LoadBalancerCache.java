package com.example.nh.lb.cache;

import com.example.nh.lb.core.ServiceInstance;
import java.util.List;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

public class LoadBalancerCache {
    private final Cache cache;
    private static final String CACHE_NAME = "loadbalancer";

    public LoadBalancerCache(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(CACHE_NAME);
    }

    public List<ServiceInstance> getInstances(String serviceId) {
        Cache.ValueWrapper wrapper = cache.get(serviceId);
        return wrapper != null ? (List<ServiceInstance>) wrapper.get() : null;
    }

    public void putInstances(String serviceId, List<ServiceInstance> instances) {
        cache.put(serviceId, instances);
    }

    public void evict(String serviceId) {
        cache.evict(serviceId);
    }

    public void clear() {
        cache.clear();
    }
}

