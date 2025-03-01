package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.xcache.autoconfigure.CacheAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.SchedulerAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.register.*;
import com.igeeksky.xcache.common.CacheConfigException;
import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xcache.core.store.StoreProvider;
import com.igeeksky.xcache.extension.codec.CodecConfig;
import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.lock.CacheLockProvider;
import com.igeeksky.xcache.extension.refresh.CacheRefreshProvider;
import com.igeeksky.xcache.extension.stat.CacheStatProvider;
import com.igeeksky.xcache.extension.stat.CacheStatistics;
import com.igeeksky.xcache.extension.sync.CacheSyncProvider;
import com.igeeksky.xcache.props.CacheConstants;
import com.igeeksky.xcache.redis.lock.RedisLockProvider;
import com.igeeksky.xcache.redis.refresh.RedisCacheRefreshProvider;
import com.igeeksky.xcache.redis.stat.RedisCacheStatMessageCodec;
import com.igeeksky.xcache.redis.stat.RedisCacheStatProvider;
import com.igeeksky.xcache.redis.stat.RedisStatConfig;
import com.igeeksky.xcache.redis.store.RedisStoreProvider;
import com.igeeksky.xcache.redis.sync.RedisCacheSyncProvider;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.lettuce.LettuceClusterFactory;
import com.igeeksky.xredis.lettuce.LettuceOperatorProxy;
import com.igeeksky.xredis.lettuce.LettuceSentinelFactory;
import com.igeeksky.xredis.lettuce.LettuceStandaloneFactory;
import com.igeeksky.xredis.lettuce.autoconfigure.ClientOptionsHelper;
import com.igeeksky.xredis.lettuce.autoconfigure.ClientResourcesHolder;
import com.igeeksky.xredis.lettuce.config.ClientOptionsBuilderCustomizer;
import com.igeeksky.xredis.lettuce.config.LettuceClusterConfig;
import com.igeeksky.xredis.lettuce.config.LettuceSentinelConfig;
import com.igeeksky.xredis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xredis.lettuce.props.LettuceCluster;
import com.igeeksky.xredis.lettuce.props.LettuceConfigHelper;
import com.igeeksky.xredis.lettuce.props.LettuceSentinel;
import com.igeeksky.xredis.lettuce.props.LettuceStandalone;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.StringUtils;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/**
 * Lettuce 自动配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore({CacheAutoConfiguration.class})
@AutoConfigureAfter({SchedulerAutoConfiguration.class})
@EnableConfigurationProperties({LettuceCacheProperties.class})
public class LettuceCacheAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LettuceCacheAutoConfiguration.class);

    @Value("${xcache.group}")
    private String group;

    private final LettuceCacheProperties lettuceCacheProperties;

    LettuceCacheAutoConfiguration(LettuceCacheProperties lettuceCacheProperties) {
        this.lettuceCacheProperties = lettuceCacheProperties;
        if (log.isDebugEnabled()) {
            log.debug("xcache.redis.lettuce:{}", lettuceCacheProperties.getLettuce());
        }
        System.out.println(lettuceCacheProperties);
    }

    @Bean
    LettuceRegister lettuceRegister(ClientResourcesHolder clientResources, ScheduledExecutorService scheduler,
                                    ObjectProvider<ClientOptionsBuilderCustomizer> customizers) {

        LettuceRegister register = new LettuceRegister();

        Collection<LettuceConfig> lettuces = lettuceCacheProperties.getLettuce();
        if (CollectionUtils.isEmpty(lettuces)) {
            return register;
        }

        for (LettuceConfig lettuce : lettuces) {
            String id = StringUtils.trimToNull(lettuce.getId());
            if (id == null) {
                throw new CacheConfigException("xcache.redis.lettuce:[" + lettuce + "], id must not be null or empty");
            }
            LettuceSentinel sentinel = lettuce.getSentinel();
            if (sentinel != null) {
                LettuceSentinelConfig config = LettuceConfigHelper.createConfig(id, sentinel);
                ClientOptions options = ClientOptionsHelper.clientOptions(config.getId(),
                        sentinel.getClientOptions(), customizers);
                LettuceSentinelFactory factory = new LettuceSentinelFactory(config, options, clientResources.get());
                register.put(config.getId(), new LettuceHolder(lettuce, config.getTimeout(), factory, scheduler));
                continue;
            }
            LettuceCluster cluster = lettuce.getCluster();
            if (cluster != null) {
                LettuceClusterConfig config = LettuceConfigHelper.createConfig(id, cluster);
                ClusterClientOptions options = ClientOptionsHelper.clusterClientOptions(config.getId(),
                        cluster.getClientOptions(), customizers);
                LettuceClusterFactory factory = new LettuceClusterFactory(config, options, clientResources.get());
                register.put(config.getId(), new LettuceHolder(lettuce, config.getTimeout(), factory, scheduler));
                continue;
            }
            LettuceStandalone standalone = lettuce.getStandalone();
            if (standalone != null) {
                LettuceStandaloneConfig config = LettuceConfigHelper.createConfig(id, standalone);
                ClientOptions options = ClientOptionsHelper.clientOptions(config.getId(),
                        standalone.getClientOptions(), customizers);
                LettuceStandaloneFactory factory = new LettuceStandaloneFactory(config, options, clientResources.get());
                register.put(config.getId(), new LettuceHolder(lettuce, config.getTimeout(), factory, scheduler));
                continue;
            }
            throw new CacheConfigException("xcache.redis.lettuce:[" + id + "] init error." + lettuce);
        }
        return register;
    }

    @Bean
    StoreProviderRegister lettuceStoreProviderRegister(LettuceRegister lettuceRegister) {
        StoreProviderRegister register = new StoreProviderRegister();
        lettuceRegister.getAll().forEach((id, holder) -> register.put(id, createStoreProvider(holder)));
        return register;
    }

    @Bean
    CacheSyncProviderRegister lettuceSyncProviderRegister(LettuceRegister lettuceRegister,
                                                          ObjectProvider<CodecProviderRegister> providers) {
        CacheSyncProviderRegister register = new CacheSyncProviderRegister();
        lettuceRegister.getAll().forEach((id, holder) -> register.put(id, createSyncProvider(holder, providers)));
        return register;
    }

    @Bean
    CacheLockProviderRegister lettuceLockProviderRegister(LettuceRegister lettuceRegister,
                                                          ScheduledExecutorService scheduler) {
        CacheLockProviderRegister register = new CacheLockProviderRegister();
        lettuceRegister.getAll().forEach((id, holder) -> register.put(id, createLockProvider(holder, scheduler)));
        return register;
    }

    @Bean
    CacheRefreshProviderRegister lettuceRefreshProviderRegister(LettuceRegister lettuceRegister,
                                                                ScheduledExecutorService scheduler) {
        CacheRefreshProviderRegister register = new CacheRefreshProviderRegister();
        lettuceRegister.getAll().forEach((id, holder) -> register.put(id, createRefreshProvider(holder, scheduler)));
        return register;
    }

    /**
     * @return {@link CacheStatProviderRegister} – 缓存指标信息统计发布
     */
    @Bean
    CacheStatProviderRegister lettuceStatProviderRegister(LettuceRegister lettuceRegister,
                                                          ScheduledExecutorService scheduler,
                                                          ObjectProvider<CodecProviderRegister> providers) {
        CacheStatProviderRegister register = new CacheStatProviderRegister();
        lettuceRegister.getAll()
                .forEach((id, holder) -> register.put(id, createStatProvider(holder, scheduler, group, providers)));
        return register;
    }

    private static Supplier<StoreProvider> createStoreProvider(LettuceHolder holder) {
        SingletonSupplier<LettuceOperatorProxy> proxySupplier = holder.getRedisOperatorProxySupplier();
        return SingletonSupplier.of(() -> new RedisStoreProvider(proxySupplier.get()));
    }

    private static Supplier<CacheSyncProvider> createSyncProvider(LettuceHolder holder,
                                                                  ObjectProvider<CodecProviderRegister> providers) {
        String codec = holder.getSyncOptions().getCodec();
        SingletonSupplier<StreamOperator<byte[], byte[]>> operatorSupplier = holder.getStreamOperatorSupplier();
        SingletonSupplier<StreamContainer<byte[], byte[]>> containerSupplier = holder.getStreamContainerSupplier();
        return SingletonSupplier.of(() -> {
            CodecProvider codecProvider = getCodecProvider(codec, providers);
            StreamOperator<byte[], byte[]> streamOperator = operatorSupplier.get();
            StreamContainer<byte[], byte[]> streamContainer = containerSupplier.get();
            return new RedisCacheSyncProvider(streamOperator, streamContainer, codecProvider);
        });
    }

    private static Supplier<CacheLockProvider> createLockProvider(LettuceHolder holder, ScheduledExecutorService scheduler) {
        SingletonSupplier<LettuceOperatorProxy> proxySupplier = holder.getRedisOperatorProxySupplier();
        return SingletonSupplier.of(() -> new RedisLockProvider(proxySupplier.get(), scheduler));
    }

    private static Supplier<CacheRefreshProvider> createRefreshProvider(LettuceHolder holder,
                                                                        ScheduledExecutorService scheduler) {
        SingletonSupplier<LettuceOperatorProxy> proxySupplier = holder.getRedisOperatorProxySupplier();
        return SingletonSupplier.of(() -> new RedisCacheRefreshProvider(proxySupplier.get(), scheduler));
    }

    private static Supplier<CacheStatProvider> createStatProvider(LettuceHolder holder, ScheduledExecutorService scheduler,
                                                                  String group, ObjectProvider<CodecProviderRegister> providers) {
        RedisStatOptions options = holder.getStatOptions();
        RedisCacheStatMessageCodec statMessageCodec = createStatMessageCodec(options, providers);
        SingletonSupplier<StreamOperator<byte[], byte[]>> streamOperatorSupplier = holder.getStreamOperatorSupplier();
        return SingletonSupplier.of(() -> {
            RedisStatConfig config = RedisStatConfig.builder()
                    .maxLen(options.getMaxLen())
                    .period(options.getPeriod())
                    .group(group)
                    .enableGroupPrefix(options.getEnableGroupPrefix())
                    .operator(streamOperatorSupplier.get())
                    .codec(statMessageCodec)
                    .scheduler(scheduler)
                    .build();
            return new RedisCacheStatProvider(config);
        });
    }

    /**
     * 获取缓存统计消息编码器<p>
     * 该编码器固定采用 Jackson 作为编解码器
     *
     * @return {@link RedisCacheStatMessageCodec} – 用于对缓存统计消息进行编码和解码
     */
    private static RedisCacheStatMessageCodec createStatMessageCodec(RedisStatOptions options,
                                                                     ObjectProvider<CodecProviderRegister> providers) {
        String charsetName = StringUtils.toUpperCase(options.getCharset());
        Charset charset = (charsetName != null) ? Charset.forName(charsetName) : StandardCharsets.UTF_8;

        CodecConfig<CacheStatistics> config = CodecConfig.builder(CacheStatistics.class).charset(charset).build();
        CodecProvider codecProvider = getCodecProvider(options.getCodec(), providers);
        if (codecProvider != null) {
            Codec<CacheStatistics> statCodec = codecProvider.getCodec(config);
            return new RedisCacheStatMessageCodec(statCodec, StringCodec.getInstance(charset));
        }
        return null;
    }

    private static CodecProvider getCodecProvider(String codecName, ObjectProvider<CodecProviderRegister> providers) {
        codecName = StringUtils.trimToNull(codecName);
        String codec = (codecName != null) ? codecName : CacheConstants.JACKSON_CODEC;
        return providers.orderedStream()
                .map(register -> register.get(codec))
                .filter(Objects::nonNull)
                .map(Supplier::get)
                .findFirst()
                .orElse(null);
    }

}