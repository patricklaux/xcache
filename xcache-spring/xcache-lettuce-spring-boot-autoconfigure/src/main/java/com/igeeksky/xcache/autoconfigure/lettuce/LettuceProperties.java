package com.igeeksky.xcache.autoconfigure.lettuce;

import com.igeeksky.xcache.redis.lettuce.config.props.Lettuce;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        if (connections != null) {
            builder.append("\"connections:\"").append(connections);
        }
        builder.append("}");
        return builder.toString();
    }
}
