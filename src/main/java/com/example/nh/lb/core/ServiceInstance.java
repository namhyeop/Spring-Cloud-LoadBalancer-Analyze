package com.example.nh.lb.core;

import java.net.URI;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

@Getter
@Builder
@With
public class ServiceInstance {
    private final String serviceId;
    private final String host;
    private final int port;
    private final URI uri;
    private final String instanceId;
    private final Map<String, String> metadata;
    private final boolean secure;
    private final String scheme;

    public String getScheme() {
        return secure ? "https" : "http";
    }

    public URI getUri() {
        if (uri != null) {
            return uri;
        }
        return URI.create(String.format("%s://%s:%d", getScheme(), host, port));
    }
}
