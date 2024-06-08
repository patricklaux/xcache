package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.core.config.HostAndPort;
import com.igeeksky.xcache.redis.lettuce.config.LettuceGenericConfig;
import io.lettuce.core.RedisURI;

import java.time.Duration;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-03
 */
public abstract class LettuceHelper {

    private LettuceHelper() {
    }

    public static RedisURI redisURI(LettuceGenericConfig config, HostAndPort node) {
        return redisURIBuilder(config).withHost(node.getHost()).withPort(node.getPort()).build();
    }

    public static RedisURI.Builder redisURIBuilder(LettuceGenericConfig config, String masterId) {
        return redisURIBuilder(config).withSentinelMasterId(masterId);
    }

    public static RedisURI.Builder redisURIBuilder(LettuceGenericConfig config) {
        RedisURI.Builder uriBuilder = RedisURI.builder()
                .withDatabase(config.getDatabase())
                .withSsl(config.isSsl())
                .withStartTls(config.isStartTls())
                .withTimeout(Duration.ofMillis(config.getTimeout()))
                .withVerifyPeer(config.getSslVerifyMode());

        String clientName = config.getClientName();
        if (clientName != null) {
            uriBuilder.withClientName(clientName);
        }

        String username = config.getUsername();
        String password = config.getPassword();
        if (username != null && password != null) {
            uriBuilder.withAuthentication(username, password);
        } else if (username == null && password != null) {
            uriBuilder.withPassword(password.toCharArray());
        }

        return uriBuilder;
    }

}
