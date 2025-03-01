package com.igeeksky.xcache.redis;


import com.igeeksky.xcache.core.store.StoreConfig;
import com.igeeksky.xcache.extension.jackson.JacksonCodec;
import com.igeeksky.xcache.props.RedisType;
import com.igeeksky.xcache.redis.store.RedisStoreProvider;
import com.igeeksky.xredis.lettuce.*;
import com.igeeksky.xredis.lettuce.config.LettuceClusterConfig;
import com.igeeksky.xredis.lettuce.config.LettuceSentinelConfig;
import com.igeeksky.xredis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xredis.lettuce.props.LettuceCluster;
import com.igeeksky.xredis.lettuce.props.LettuceConfigHelper;
import com.igeeksky.xredis.lettuce.props.LettuceSentinel;
import com.igeeksky.xredis.lettuce.props.LettuceStandalone;
import com.igeeksky.xtool.core.lang.compress.GzipCompressor;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.resource.ClientResources;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Lettuce 测试辅助类
 *
 * @author patrick
 * @since 0.0.4 2024/5/30
 */
public class LettuceTestHelper {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static RedisStoreProvider createStandaloneRedisStoreProvider() {
        LettuceStandaloneFactory factory = createStandaloneFactory();
        LettuceOperator<byte[], byte[]> redisOperator = factory.redisOperator(ByteArrayCodec.INSTANCE);
        return new RedisStoreProvider(new LettuceOperatorProxy(10000, 60000, redisOperator));
    }

    public static LettuceStandaloneFactory createStandaloneFactory() {
        LettuceStandalone standalone = new LettuceStandalone();
        standalone.setNode("127.0.0.1:6379");
        standalone.setNodes(List.of("127.0.0.1:6378"));
        standalone.setReadFrom("upstreamPreferred");

        LettuceStandaloneConfig standaloneConfig = LettuceConfigHelper.createConfig("test", standalone);
        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceStandaloneFactory(standaloneConfig, options, resources);
    }

    public static LettuceSentinelFactory createSentinelFactory() {
        LettuceSentinel sentinel = new LettuceSentinel();
        sentinel.setNodes(List.of("127.0.0.1:26379", "127.0.0.1:26380", "127.0.0.1:26381"));
        sentinel.setReadFrom("upstreamPreferred");
        sentinel.setMasterId("mymaster");

        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();
        LettuceSentinelConfig config = LettuceConfigHelper.createConfig("lettuce", sentinel);

        return new LettuceSentinelFactory(config, options, resources);
    }

    public static LettuceClusterFactory createClusterConnectionFactory() {
        LettuceCluster cluster = new LettuceCluster();
        cluster.setNodes(List.of("127.0.0.1:7001", "127.0.0.1:7002", "127.0.0.1:7003"));
        cluster.setReadFrom("upstreamPreferred");

        ClientResources resources = ClientResources.builder().build();
        ClusterClientOptions options = ClusterClientOptions.builder().build();
        LettuceClusterConfig config = LettuceConfigHelper.createConfig("test", cluster);

        return new LettuceClusterFactory(config, options, resources);
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
