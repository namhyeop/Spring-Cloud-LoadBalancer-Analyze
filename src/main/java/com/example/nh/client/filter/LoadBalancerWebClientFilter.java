package com.example.nh.client.filter;

import com.example.nh.client.factory.LoadBalancerClientFactory;
import com.example.nh.lb.core.LoadBalancer;
import com.example.nh.lb.core.ServiceInstance;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

//TODO: 클라이언트 필터 테스트 추가
@Slf4j
@RequiredArgsConstructor
public class LoadBalancerWebClientFilter implements ExchangeFilterFunction {
    private final LoadBalancerClientFactory loadBalancerFactory;

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        URI originalUrl = request.url();
        String serviceId = originalUrl.getHost();

        LoadBalancer loadBalancer = loadBalancerFactory.getInstance(serviceId);

        return loadBalancer.choose(serviceId)
                           .map(server -> buildRequest(request, server))
                           .flatMap(next::exchange)
                           .switchIfEmpty(Mono.error(
                               new IllegalStateException("사용 가능한 인스턴스 서비스가 없습니다 : " + serviceId)
                           ));
    }

    private ClientRequest buildRequest(ClientRequest request, ServiceInstance server) {
        URI originalUrl = request.url();

        URI newUri = UriComponentsBuilder.fromUri(server.getUri())
                                         .path(originalUrl.getRawPath())
                                         .query(originalUrl.getRawQuery())
                                         .build(true)
                                         .toUri();

        return ClientRequest.from(request)
                            .url(newUri)
                            .build();
    }
}
