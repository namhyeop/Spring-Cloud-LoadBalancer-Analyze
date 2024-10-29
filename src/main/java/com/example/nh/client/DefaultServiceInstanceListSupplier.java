package com.example.nh.client;


import com.example.nh.lb.cache.LoadBalancerCache;
import com.example.nh.lb.core.ServiceInstance;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class DefaultServiceInstanceListSupplier implements ServiceInstanceListSupplier {

    private final ConcurrentMap<String, List<ServiceInstance>> serviceInstances = new ConcurrentHashMap<>();
    private String serviceId;
    private final LoadBalancerCache loadBalancerCache;

    public DefaultServiceInstanceListSupplier(LoadBalancerCache loadBalancerCache) {
        this.loadBalancerCache = loadBalancerCache;
        this.serviceId = "runit-service";
        initializeAndCacheInstances();
    }

    private void initializeAndCacheInstances() {
        // 기본 더미 서버 설정
        List<ServiceInstance> runitInstance = new ArrayList<>();
        runitInstance.add(ServiceInstance.builder()
                                         .serviceId("runit-service")
                                         .host("runit-service")
                                         .instanceId("1")
                                         .port(8081)
                                         .build());
        runitInstance.add(ServiceInstance.builder()
                                         .serviceId("runit-service")
                                         .host("runit-service")
                                         .instanceId("2")
                                         .port(8082)
                                         .build());
        runitInstance.add(ServiceInstance.builder()
                                         .serviceId("runit-service")
                                         .host("runit-service")
                                         .instanceId("3")
                                         .port(8083)
                                         .build());

        List<ServiceInstance> nhInstance = new ArrayList<>();
        nhInstance.add(ServiceInstance.builder()
                                      .serviceId("nh-service")
                                      .host("nh-service")
                                      .instanceId("4")
                                      .port(9081)
                                      .build());
        nhInstance.add(ServiceInstance.builder()
                                      .serviceId("nh-service")
                                      .host("nh-service")
                                      .instanceId("5")
                                      .port(9082)
                                      .build());
        nhInstance.add(ServiceInstance.builder()
                                      .serviceId("nh-service")
                                      .host("nh-service")
                                      .instanceId("6")
                                      .port(9083)
                                      .build());

        serviceInstances.put(serviceId, runitInstance);
        serviceInstances.put("nh-service", nhInstance);

        loadBalancerCache.putInstances(serviceId, runitInstance);
        loadBalancerCache.putInstances("nh-service", nhInstance);
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        List<ServiceInstance> allInstances = new ArrayList<>();

        serviceInstances.keySet().forEach(key -> {
            List<ServiceInstance> instances = loadBalancerCache.getInstances(key);
            if (instances != null) {
                allInstances.addAll(instances);
            }
        });

        return Flux.just(allInstances);
    }

    public void addInstance(ServiceInstance instance) {
        serviceInstances.compute(instance.getServiceId(), (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(instance);

            loadBalancerCache.putInstances(key, list);
            return list;
        });
    }

    public void removeInstance(String instanceId) {
        serviceInstances.forEach((serviceId, instances) -> {
            boolean removed = instances.removeIf(instance -> instance.getInstanceId().equals(instanceId));
            if (removed) {
                loadBalancerCache.putInstances(serviceId, instances);
            }
        });
    }
}
