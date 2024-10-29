package com.example.nh.client.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "loadbalancer")
public class LoadBalancerProperties {
    private Map<String, ClientConfiguration> clients = new HashMap<>();


    @Data
    public static class ClientConfiguration {
        private String name;
        private String strategy = "round-robin";  // 기본값
        private boolean enabled = true;
        private Map<String, String> hints = new HashMap<>();
    }
}
