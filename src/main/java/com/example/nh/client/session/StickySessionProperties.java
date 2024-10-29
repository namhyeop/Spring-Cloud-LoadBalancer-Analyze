package com.example.nh.client.session;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "loadbalancer.sticky-session")
public class StickySessionProperties {
    private boolean enabled = false;
    private String cookieName = "SERVERID";
    private int expiry = 3600; // seconds
}
