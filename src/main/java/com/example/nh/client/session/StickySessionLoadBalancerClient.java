package com.example.nh.client.session;

import com.example.nh.lb.core.LoadBalancer;
import com.example.nh.lb.core.ServiceInstance;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class StickySessionLoadBalancerClient {
    private final LoadBalancer loadBalancer;
    private final StickySessionProperties properties;
    private final Map<String, String> sessionToInstanceMap = new ConcurrentHashMap<>();

    public Mono<ServiceInstance> choose(String serviceId, ServerWebExchange exchange) {
        if (!properties.isEnabled()) {
            return loadBalancer.choose(serviceId);
        }

        HttpCookie sessionCookie = exchange.getRequest()
                                           .getCookies()
                                           .getFirst(properties.getCookieName());

        if (sessionCookie != null) {
            String instanceId = sessionToInstanceMap.get(sessionCookie.getValue());
            if (instanceId != null) {
                return loadBalancer.choose(
                    new LoadBalancer.Request(serviceId, instanceId)
                );
            }
        }

        return loadBalancer.choose(serviceId)
                           .doOnNext(instance -> {
                               String sessionId = generateSessionId();
                               sessionToInstanceMap.put(sessionId, instance.getInstanceId());
                               exchange.getResponse().addCookie(
                                   createSessionCookie(sessionId)
                               );
                           });
    }

    private ResponseCookie createSessionCookie(String sessionId) {
        return ResponseCookie.from(properties.getCookieName(), sessionId)
                             .maxAge(properties.getExpiry())
                             .path("/")
                             .build();
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
