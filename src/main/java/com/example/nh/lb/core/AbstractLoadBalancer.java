package com.example.nh.lb.core;

import com.example.nh.client.DefaultServiceInstanceListSupplier;
import com.example.nh.lb.cache.LoadBalancerCache;
import com.example.nh.lb.health.HealthCheckService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractLoadBalancer implements LoadBalancer {

    protected final ServiceInstanceListSupplier serviceInstanceListSupplier;
    protected final LoadBalancerCache cache;
    protected final HealthCheckService healthCheckService;

    @Override
    public Mono<ServiceInstance> choose() {
        return choose(serviceInstanceListSupplier.getServiceId());
    }

    @Override
    public Mono<ServiceInstance> choose(String serviceId) {
        return choose(new Request(serviceId, null));
    }

    @Override
    public Mono<ServiceInstance> choose(Request request) {
        log.info("[동작중인 로드밸런서] 서비스: {}, 로드밸런서 타입: {}",
            request.getServiceId(), this.getClass().getSimpleName());
        if (serviceInstanceListSupplier instanceof DefaultServiceInstanceListSupplier) {
            ((DefaultServiceInstanceListSupplier) serviceInstanceListSupplier)
                .setServiceId(request.getServiceId());
        }

        return Mono.defer(() -> {
            // 캐시에서 서비스 인스턴스 확인
            List<ServiceInstance> cachedInstances = cache.getInstances(request.getServiceId());
            if (cachedInstances != null && !cachedInstances.isEmpty()) {
                log.debug("[로드밸런스 조회]캐시 HIT. 서비스: {}, 인스턴스 수: {}", request.getServiceId(), cachedInstances.size());
                return chooseInstance(cachedInstances, request);
            }

            // 캐시에 없으면 supplier에서 가져오기
            return serviceInstanceListSupplier.get()
                                              .map(instances -> instances.stream()
                                                                         .filter(instance -> request.getServiceId().equals(instance.getServiceId()))
                                                                         .collect(Collectors.toList()))
                                              .flatMap(instances -> {
                                                  if (instances.isEmpty()) {
                                                      log.error("서비스에 대한 인스턴스가 없음: {}", request.getServiceId());
                                                      return Mono.empty();
                                                  }
                                                  // 헬스체크 수행
                                                  return healthCheckService.filterHealthyInstances(instances)
                                                                           .flatMap(healthyInstances -> {
                                                                               if (healthyInstances.isEmpty()) {
                                                                                   log.error("정상 상태의 인스턴스가 없음: {}", request.getServiceId());
                                                                                   return Mono.empty();
                                                                               }
                                                                               // 캐시 업데이트
                                                                               cache.putInstances(request.getServiceId(), healthyInstances);
                                                                               return chooseInstance(healthyInstances, request);
                                                                           });
                                              })
                                              .next();
        });
    }

    protected abstract Mono<ServiceInstance> chooseInstance(List<ServiceInstance> instances, Request request);
}
