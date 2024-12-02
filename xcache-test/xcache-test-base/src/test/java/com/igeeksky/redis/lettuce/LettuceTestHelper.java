package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.redis.lettuce.props.Lettuce;
import com.igeeksky.redis.lettuce.props.LettuceCluster;
import com.igeeksky.redis.lettuce.props.LettuceSentinel;
import com.igeeksky.redis.lettuce.props.LettuceStandalone;
import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.extension.jackson.JacksonCodec;
import com.igeeksky.xcache.props.RedisType;
import com.igeeksky.xcache.redis.store.RedisStoreProvider;
import com.igeeksky.xtool.core.lang.compress.GzipCompressor;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.resource.ClientResources;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Lettuce ≤‚ ‘∏®÷˙¿‡
 *
 * @author patrick
 * @since 0.0.4 2024/5/30
 */
public class LettuceTestHelper {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static RedisStoreProvider createStandaloneRedisStoreProvider() {
        LettuceStandaloneFactory factory = createStandaloneFactory();
        return new RedisStoreProvider(factory);
    }

    public static LettuceStandaloneFactory createStandaloneFactory() {
        LettuceStandalone standalone = new LettuceStandalone();
        standalone.setNode("127.0.0.1:6379");
        standalone.setNodes(List.of("127.0.0.1:6380"));
        standalone.setReadFrom("upstreamPreferred");

        Lettuce lettuce = new Lettuce();
        lettuce.setId("test");
        lettuce.setStandalone(standalone);

        LettuceStandaloneConfig standaloneConfig = lettuce.createStandaloneConfig();
        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceStandaloneFactory(standaloneConfig, options, resources);
    }

    public static LettuceSentinelFactory createSentinelFactory() {
        LettuceSentinel sentinel = new LettuceSentinel();

        sentinel.setNodes(List.of("127.0.0.1:26379", "127.0.0.1:26380", "127.0.0.1:26381"));
        sentinel.setReadFrom("upstreamPreferred");
        sentinel.setMasterId("mymaster");

        Lettuce lettuce = new Lettuce();
        lettuce.setId("lettuceSentinelConnectionFactory");
        lettuce.setSentinel(sentinel);

        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceSentinelFactory(lettuce.createSentinelConfig(), options, resources);
    }

    public static LettuceClusterFactory createClusterConnectionFactory() {
        LettuceCluster cluster = new LettuceCluster();
        cluster.setNodes(
                List.of("127.0.0.1:7001", "127.0.0.1:7002", "127.0.0.1:7003",
                        "127.0.0.1:7004", "127.0.0.1:7005", "127.0.0.1:7006")
        );
        cluster.setReadFrom("upstreamPreferred");

        Lettuce lettuce = new Lettuce();
        lettuce.setId("lettuceClusterConnectionFactory");
        lettuce.setCluster(cluster);

        ClusterClientOptions options = ClusterClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceClusterFactory(lettuce.createClusterConfig(), options, resources);
    }

    public static StoreConfig<String> createRedisStoreConfig(RedisType redisType) {
        StoreConfig.Builder<String> builder = StoreConfig.builder(String.class);
        return builder.redisType(redisType)
                .enableGroupPrefix(true)
                .enableRandomTtl(true)
                .enableNullValue(true)
                .expireAfterWrite(3600000)
                .charset(UTF_8)
                .valueCompressor(GzipCompressor.getInstance())
                .valueCodec(new JacksonCodec<>(String.class))
                .build();
    }

}
