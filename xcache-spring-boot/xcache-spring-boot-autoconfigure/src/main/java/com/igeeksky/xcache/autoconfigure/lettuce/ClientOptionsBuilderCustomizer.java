package com.igeeksky.xcache.autoconfigure.lettuce;


import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;

/**
 * 用户可以重写此接口，将该类作为 bean 注入到 Spring 容器中。
 * <p>
 * 此接口用以实现自定义的客户端选项，因为部分客户端选项无法采用配置的方式来处理。
 * <p>
 * 生成 LettuceConnectionFactory 时会先读取配置，再用此自定义类来配置客户端，如果读取的配置和此自定义类有相同配置项，以此自定义类为准。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-05
 */
public interface ClientOptionsBuilderCustomizer {

    /**
     * @param id      LettuceConnectionFactory 的 beanId
     * @param builder 过期时间配置，可以实现不同命令有不同的超时设置，一般情况下默认即可
     */
    default void customizeTimeout(String id, TimeoutOptions.Builder builder) {
    }

    /**
     * @param id      LettuceConnectionFactory 的 beanId
     * @param builder ssl 配置
     */
    default void customizeSsl(String id, SslOptions.Builder builder) {
    }

    /**
     * @param id      LettuceConnectionFactory 的 beanId
     * @param builder 单机、主从、哨兵客户端配置
     */
    default void customizeClient(String id, ClientOptions.Builder builder) {
    }

    /**
     * @param id      LettuceConnectionFactory 的 beanId
     * @param builder 集群客户端配置
     */
    default void customizeClusterClient(String id, ClusterClientOptions.Builder builder) {
    }

}
