package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.core.config.CacheConfig;
import com.igeeksky.xcache.core.config.RemoteConfig;
import com.igeeksky.xcache.redis.RedisStoreProvider;
import com.igeeksky.xcache.redis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xcache.redis.lettuce.config.props.Lettuce;
import com.igeeksky.xcache.redis.lettuce.config.props.LettuceCluster;
import com.igeeksky.xcache.redis.lettuce.config.props.LettuceSentinel;
import com.igeeksky.xcache.redis.lettuce.config.props.LettuceStandalone;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.resource.ClientResources;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author patrick
 * @since 0.0.4 2024/5/30
 */
public class LettuceTestHelper {

    public static RedisStoreProvider createStandaloneRedisStoreProvider() {
        LettuceConnectionFactory factory = createStandaloneConnectionFactory();
        return new RedisStoreProvider(factory);
    }

    public static LettuceConnectionFactory createStandaloneConnectionFactory() {
        LettuceStandalone standalone = new LettuceStandalone();
        standalone.setMaster("127.0.0.1:6379");
        standalone.setReplicas(List.of("127.0.0.1:6380"));
        standalone.setReadFrom("upstreamPreferred");

        Lettuce lettuce = new Lettuce();
        lettuce.setId("test");
        lettuce.setStandalone(standalone);
        lettuce.setCharset(StandardCharsets.UTF_8.name());

        LettuceStandaloneConfig standaloneConfig = lettuce.createStandaloneConfig();
        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceConnectionFactory(standaloneConfig, options, resources);
    }

    public static LettuceSentinelConnectionFactory createSentinelConnectionFactory() {
        LettuceSentinel sentinel = new LettuceSentinel();

        sentinel.setNodes(List.of("127.0.0.1:26379", "127.0.0.1:26380", "127.0.0.1:26381"));
        sentinel.setReadFrom("upstreamPreferred");
        sentinel.setMasterId("mymaster");

        Lettuce lettuce = new Lettuce();
        lettuce.setId("lettuceSentinelConnectionFactory");
        lettuce.setSentinel(sentinel);
        lettuce.setCharset(StandardCharsets.UTF_8.name());

        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceSentinelConnectionFactory(lettuce.createSentinelConfig(), options, resources);
    }

    public static LettuceClusterConnectionFactory createClusterConnectionFactory() {
        LettuceCluster cluster = new LettuceCluster();
        cluster.setNodes(
                List.of("127.0.0.1:7001", "127.0.0.1:7002", "127.0.0.1:7003",
                        "127.0.0.1:7004", "127.0.0.1:7005", "127.0.0.1:7006")
        );
        cluster.setReadFrom("upstreamPreferred");

        Lettuce lettuce = new Lettuce();
        lettuce.setId("lettuceClusterConnectionFactory");
        lettuce.setCluster(cluster);
        lettuce.setCharset(StandardCharsets.UTF_8.name());

        ClusterClientOptions options = ClusterClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceClusterConnectionFactory(lettuce.createClusterConfig(), options, resources);
    }

    public static CacheConfig<String, byte[]> createCacheConfig(String storeName) {
        RemoteConfig<byte[]> remoteConfig = new RemoteConfig<>();
        remoteConfig.setStoreName(storeName);
        remoteConfig.setEnableKeyPrefix(true);
        remoteConfig.setEnableRandomTtl(true);
        remoteConfig.setExpireAfterWrite(3600000);

        CacheConfig<String, byte[]> cacheConfig = new CacheConfig<>();
        cacheConfig.setName(storeName);
        cacheConfig.setCharset(StandardCharsets.UTF_8);
        cacheConfig.setRemoteConfig(remoteConfig);
        return cacheConfig;
    }

}
