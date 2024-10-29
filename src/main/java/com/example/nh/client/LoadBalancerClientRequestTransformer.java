package com.example.nh.client;

import com.example.nh.lb.core.ServiceInstance;
import java.net.URI;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;

public class LoadBalancerClientRequestTransformer {
    public static HttpRequest transformRequest(HttpRequest request, ServiceInstance instance) {
        return new HttpRequestWrapper(request) {
            @Override
            public URI getURI() {
                return instance.getUri().resolve(request.getURI());
            }
        };
    }
}
