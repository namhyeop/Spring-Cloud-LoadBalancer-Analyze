package com.example.nh.lb.core;

import reactor.core.publisher.Mono;

public interface LoadBalancer {
    Mono<ServiceInstance> choose();
    Mono<ServiceInstance> choose(String serviceId);
    Mono<ServiceInstance> choose(Request request);

    class Request {
        private final String serviceId;
        private final Object hint;

        public Request(String serviceId, Object hint) {
            this.serviceId = serviceId;
            this.hint = hint;
        }

        public String getServiceId() {
            return serviceId;
        }

        public Object getHint() {
            return hint;
        }
    }
}
