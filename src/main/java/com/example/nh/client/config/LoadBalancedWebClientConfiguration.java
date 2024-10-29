package com.example.nh.client.config;

import com.example.nh.client.factory.LoadBalancerClientFactory;
import com.example.nh.client.filter.LoadBalancerWebClientFilter;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

@Configuration
public class LoadBalancedWebClientConfiguration {

    @Bean
    public WebClient.Builder loadBalancedWebClientBuilder(LoadBalancerClientFactory loadBalancerFactory) {
        return WebClient.builder()
                        .filter(new LoadBalancerWebClientFilter(loadBalancerFactory));
    }

    @Bean
    public SmartInitializingSingleton loadBalancedWebClientInitializer(
        final LoadBalancerClientFactory loadBalancerFactory,
        final ObjectProvider<List<Builder>> webClientBuilders
    ) {
        return () -> {
            webClientBuilders.ifAvailable(builders -> {
                builders.forEach(builder -> {
                    builder.filter(new LoadBalancerWebClientFilter(loadBalancerFactory));
                });
            });
        };
    }
}
