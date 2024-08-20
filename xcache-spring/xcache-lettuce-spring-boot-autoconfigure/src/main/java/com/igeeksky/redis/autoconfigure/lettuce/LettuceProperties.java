package com.igeeksky.redis.autoconfigure.lettuce;

import com.igeeksky.redis.lettuce.config.props.Lettuce;
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

    private List<Lettuce> factory;

    public List<Lettuce> getFactory() {
        return factory;
    }

    public void setFactory(List<Lettuce> factory) {
        this.factory = factory;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        if (factory != null) {
            builder.append("\"factory:\"").append(factory);
        }
        return builder.append("}").toString();
    }
}
