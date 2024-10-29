package com.example.nh.lb.core;

import java.util.List;
import reactor.core.publisher.Flux;

public interface ServiceInstanceListSupplier {
    String getServiceId();
    Flux<List<ServiceInstance>> get();
}

