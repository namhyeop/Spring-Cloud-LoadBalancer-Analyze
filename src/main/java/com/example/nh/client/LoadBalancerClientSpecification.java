package com.example.nh.client;

import com.example.nh.client.factory.NamedContextFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoadBalancerClientSpecification implements NamedContextFactory.Specification {
    private String name;
    private Class<?>[] configuration;
}
