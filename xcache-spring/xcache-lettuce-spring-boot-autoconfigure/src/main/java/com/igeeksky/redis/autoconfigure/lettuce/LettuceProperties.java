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

    /**
     * lettuce 配置列表
     * <p>
     * 如不使用 Lettuce，可删除此配置
     */
    private List<Lettuce> factories;

    public List<Lettuce> getFactories() {
        return factories;
    }

    public void setFactories(List<Lettuce> factories) {
        this.factories = factories;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"factories:\"");
        if (factories != null) {
            builder.append(factories);
        } else {
            builder.append("[]");
        }
        return builder.append("}").toString();
    }

}
