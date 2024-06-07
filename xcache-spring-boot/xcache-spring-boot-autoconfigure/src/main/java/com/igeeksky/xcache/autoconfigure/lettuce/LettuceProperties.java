package com.igeeksky.xcache.autoconfigure.lettuce;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
@Configuration
@ConfigurationProperties(prefix = "xcache.redis.lettuce")
public class LettuceProperties {

    private List<Lettuce> connections;

    public List<Lettuce> getConnections() {
        return connections;
    }

    public void setConnections(List<Lettuce> connections) {
        this.connections = connections;
    }

}
