package com.example.nh.client.config;

import com.example.nh.client.factory.LoadBalancerClientFactory;
import com.example.nh.client.intercepter.LoadBalancerInterceptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LoadBalanceRestTemplateConfiguration {

    @Bean
    public RestTemplate loadBalancedRestTemplate(LoadBalancerClientFactory loadBalancerFactory) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
            Collections.singletonList(new LoadBalancerInterceptor(loadBalancerFactory))
        );
        return restTemplate;
    }

    @Bean
    public SmartInitializingSingleton loadBalancedRestTemplateInitializer(LoadBalancerClientFactory loadBalancerFactory,
        ObjectProvider<List<RestTemplate>> restTemplates) {
        return () -> {
            restTemplates.ifAvailable(templates -> {
                for (RestTemplate template : templates) {
                    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(
                        template.getInterceptors());
                    interceptors.add(new LoadBalancerInterceptor(loadBalancerFactory));
                    template.setInterceptors(interceptors);
                }
            });
        };
    }
}
