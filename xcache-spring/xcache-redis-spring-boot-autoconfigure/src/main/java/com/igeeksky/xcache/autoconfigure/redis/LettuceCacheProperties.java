package com.igeeksky.xcache.autoconfigure.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Lettuce 自动配置属性
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
@Configuration
@ConfigurationProperties(prefix = "xcache.redis")
public class LettuceCacheProperties {

    /**
     * lettuce 配置列表
     * <p>
     * 如不使用 Lettuce，可删除此配置
     */
    private List<LettuceConfig> lettuce;

    public List<LettuceConfig> getLettuce() {
        return lettuce;
    }

    public void setLettuce(List<LettuceConfig> lettuce) {
        this.lettuce = lettuce;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"lettuce\":");
        if (lettuce != null) {
            builder.append(lettuce);
        } else {
            builder.append("[]");
        }
        return builder.append("}").toString();
    }

}
