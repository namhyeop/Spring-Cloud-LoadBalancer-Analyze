package com.example.nh.simulation;

import com.example.nh.client.DefaultServiceInstanceListSupplier;
import com.example.nh.client.annotation.LoadBalancerClient;
import com.example.nh.client.annotation.LoadBalancerClients;
import com.example.nh.client.factory.LoadBalancerClientFactory;
import com.example.nh.lb.config.RandomLoadBalancerConfig;
import com.example.nh.lb.config.RoundRobinLoadBalancerConfig;
import com.example.nh.lb.core.LoadBalancer;
import com.example.nh.lb.core.ServiceInstance;
import com.example.nh.lb.core.ServiceInstanceListSupplier;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/loadbalancer")
@LoadBalancerClients({
    @LoadBalancerClient(name = "nh-service", configuration = RandomLoadBalancerConfig.class),
    @LoadBalancerClient(name = "runit-service", configuration = RoundRobinLoadBalancerConfig.class),
})
@RequiredArgsConstructor
@Slf4j
public class LoadBalancerTestController {

    private final ServiceInstanceListSupplier serviceInstanceListSupplier;
    private final LoadBalancerClientFactory loadBalancerFactory;
    private final RestTemplate restTemplate;
    private final WebClient.Builder webClient;

    @GetMapping("/choose")
    public Mono<ServiceInstance> chooseInstance(@RequestParam String serviceId) {
        log.info("[로드밸런스 테스트] 라운드로빈 인스턴스 serviceId: {}", serviceId);
        return loadBalancerFactory.getInstance(serviceId)
                                  .choose(serviceId)
                                  .doOnSubscribe(subscription -> log.info("[인스턴스 호출] serviceId: {}", serviceId))
                                  .doOnNext(instance -> log.info("조회 인스턴스 HOST: {}, PORT: {}", instance.getHost(),
                                      instance.getPort()))
                                  .switchIfEmpty(Mono.defer(() -> {
                                      log.error("조회된 인스턴스가 존재하지 않습니다: {}", serviceId);
                                      return Mono.empty();
                                  }))
                                  .doOnError(error -> log.error("인스턴스 조회 에러 발생: ", error));
    }

    @GetMapping("/instances")
    public Flux<ServiceInstance> getInstances(@RequestParam String serviceId) {
        log.info("[전체 인스턴스 조회] 조회 인스턴스 serviceId: {}", serviceId);
        return serviceInstanceListSupplier.get()
                                          .doOnNext(instances -> log.info("전체 인스턴스 수: {}", instances.size()))
                                          .flatMap(instances -> {
                                              List<ServiceInstance> filteredInstances = instances.stream()
                                                                                                 .filter(instance -> serviceId.equals(instance.getServiceId()))
                                                                                                 .collect(Collectors.toList());
                                              log.info("조회된 서비스({})의 인스턴스 수: {}", serviceId, filteredInstances.size());
                                              return Flux.fromIterable(filteredInstances);
                                          })
                                          .doOnComplete(() -> log.info("인스턴스 조회가 완료되었습니다."))
                                          .doOnError(error -> log.error("인스턴스 조회 중 에러가 발생했습니다: ", error));
    }

    @PostMapping("/instances")
    public Mono<ResponseEntity<ServiceInstance>> addInstance(@RequestBody ServiceInstance instance) {
        log.info("[인스턴스 등록 요청] serviceId: {}, host: {}, port: {}",
            instance.getServiceId(), instance.getHost(), instance.getPort());

        if (serviceInstanceListSupplier instanceof DefaultServiceInstanceListSupplier supplier) {
            // 인스턴스 추가
            supplier.addInstance(instance);
            log.info("새로운 인스턴스가 등록되었습니다. instanceId: {}", instance.getInstanceId());
            return Mono.just(ResponseEntity.ok(instance));
        }

        log.error("인스턴스 등록에 실패했습니다. 잘못된 ServiceInstanceListSupplier 타입입니다.");
        return Mono.just(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/instances/{instanceId}")
    public Mono<ResponseEntity<Void>> removeInstance(@PathVariable String instanceId) {
        log.info("[인스턴스 제거 요청] instanceId: {}", instanceId);

        if (serviceInstanceListSupplier instanceof DefaultServiceInstanceListSupplier supplier) {
            supplier.removeInstance(instanceId);

            log.info("인스턴스가 성공적으로 제거되었습니다. instanceId: {}", instanceId);

            return Mono.just(ResponseEntity.ok().build());
        }

        log.error("인스턴스 제거에 실패했습니다. 잘못된 ServiceInstanceListSupplier 타입입니다.");
        return Mono.just(ResponseEntity.badRequest().build());
    }

    @GetMapping("/interceptor")
    public String callServiceWithRestTemplate() {
        log.info("[블락킹 Rest API 테스트]");

        return restTemplate.getForObject(
            "http://runit-service/api/endpoint",
            String.class
        );
    }

    @GetMapping("/filter")
    public Mono<String> callServiceWithWebClient() {
        log.info("[논블락킹 Rest API 테스트]");

        return webClient.build()
                        .get()
                        .uri("http://runit-service/api/endpoint")
                        .retrieve()
                        .bodyToMono(String.class);
    }
}
