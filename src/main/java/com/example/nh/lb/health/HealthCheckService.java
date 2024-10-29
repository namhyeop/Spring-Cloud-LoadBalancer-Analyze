package com.example.nh.lb.health;

import com.example.nh.lb.core.ServiceInstance;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class HealthCheckService {
    private final WebClient webClient;
    private final ServiceInstanceListSupplier serviceInstanceListSupplier;
    private static final String HEALTH_CHECK_PATH = "/actuator/health";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    public HealthCheckService(WebClient.Builder webClientBuilder, ServiceInstanceListSupplier serviceInstanceListSupplier) {
        this.webClient = webClientBuilder.build();
        this.serviceInstanceListSupplier = serviceInstanceListSupplier;
    }

    public Mono<Boolean> checkServiceHealth(String serviceId) {
        return serviceInstanceListSupplier.get()
                                          .next()
                                          .flatMap(instances -> Flux.fromIterable(instances)
                                                                    .filter(instance -> instance.getServiceId().equals(serviceId))
                                                                    .flatMap(this::checkInstanceHealth)
                                                                    .all(healthy -> healthy));
    }

    public Mono<List<ServiceInstance>> filterHealthyInstances(List<ServiceInstance> instances) {
        return Mono.just(instances);
          // 헬스 체크할 외부 Service Discovery 서비스가 있는 경우 활성화
//        return Flux.fromIterable(instances)
//                   .filterWhen(this::checkInstanceHealth)
//                   .collectList();
    }

    private Mono<Boolean> checkInstanceHealth(ServiceInstance instance) {
        return webClient.get()
                        .uri(instance.getUri().resolve(HEALTH_CHECK_PATH))
                        .retrieve()
                        .toBodilessEntity()
                        .map(response -> response.getStatusCode() == HttpStatus.OK)
                        .timeout(TIMEOUT)
                        .doOnError(e -> log.warn("Health check failed for instance {}: {}",
                            instance.getInstanceId(), e.getMessage()))
                        .onErrorReturn(false);
    }
}
