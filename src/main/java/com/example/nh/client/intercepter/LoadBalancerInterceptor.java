package com.example.nh.client.intercepter;

import com.example.nh.client.factory.LoadBalancerClientFactory;
import com.example.nh.lb.core.LoadBalancer;
import com.example.nh.lb.core.ServiceInstance;
import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.util.UriComponentsBuilder;

//TODO: 인터셉터 테스트 추가
@Slf4j
@RequiredArgsConstructor
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {
    private final LoadBalancerClientFactory loadBalancerFactory;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        URI originalUri = request.getURI();
        String serviceId = originalUri.getHost();

        log.info("[LoadBalancerInterceptor] 원본 요청 URI: {}", originalUri);

        try {
            LoadBalancer loadBalancer = loadBalancerFactory.getInstance(serviceId);
            ServiceInstance instance = loadBalancer.choose(serviceId).block();

            if (instance == null) {
                throw new IllegalStateException("[LoadBalancerInterceptor] 사용 가능한 서비스 인스턴스가 없습니다 : " + serviceId);
            }

            URI newUri = UriComponentsBuilder.fromUri(instance.getUri())
                                             .path(originalUri.getRawPath())
                                             .query(originalUri.getRawQuery())
                                             .build(true)
                                             .toUri();

            log.info("[LoadBalancerInterceptor] Transformed URI: {} -> {}", originalUri, newUri);

            HttpRequest newRequest = new HttpRequestWrapper(request) {
                @Override
                public URI getURI() {
                    return newUri;
                }

                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(super.getHeaders());
                    headers.set(HttpHeaders.HOST, instance.getHost() + ":" + instance.getPort());
                    return headers;
                }
            };

            return execution.execute(newRequest, body);

        } catch (Exception e) {
            throw new IOException("[LoadBalancerInterceptor] 로드밸런싱 실패", e);
        }
    }
}
