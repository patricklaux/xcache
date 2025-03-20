package com.igeeksky.xcache.redis.sync;

import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xcache.extension.sync.*;
import com.igeeksky.xredis.common.ByteArrayTimeConvertor;
import com.igeeksky.xredis.common.flow.RetrySubscription;
import com.igeeksky.xredis.common.flow.Subscriber;
import com.igeeksky.xredis.common.flow.Subscription;
import com.igeeksky.xredis.common.stream.*;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.codec.Codec;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis 缓存数据同步工厂类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-13
 */
public class RedisCacheSyncProvider implements CacheSyncProvider {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheSyncProvider.class);

    private final Map<Charset, RedisCacheSyncMessageCodec> messageCodecs = new ConcurrentHashMap<>();

    private final CodecProvider codecProvider;
    private final StreamOperator<byte[], byte[]> operator;
    private final StreamContainer<byte[], byte[]> container;

    /**
     * 创建 Redis 缓存数据同步工厂类
     *
     * @param operator      Redis 流信息操作
     * @param container     Redis 流信息接收
     * @param codecProvider 编解码器工厂
     */
    public RedisCacheSyncProvider(StreamOperator<byte[], byte[]> operator,
                                  StreamContainer<byte[], byte[]> container,
                                  CodecProvider codecProvider) {
        Assert.notNull(operator, "RedisOperator must not be null");
        Assert.notNull(container, "StreamContainer must not be null");
        Assert.notNull(codecProvider, "CodecProvider must not be null");
        this.operator = operator;
        this.container = container;
        this.codecProvider = codecProvider;
    }

    /**
     * 注册消息监听器
     *
     * @param channel  消息的通道标识，用于指定订阅的特定通道。
     * @param listener 消息监听器，用于消费缓存同步消息（删除特定键集 或 清空缓存）
     * @param <V>      缓存的值泛型类型
     */
    @Override
    public <V> void register(String channel, Charset charset, SyncMessageListener<V> listener) {
        // 获取 Redis 主机时间作为起始 ID（缓存被使用是在此方法完成之后，因此不会遗漏消息）
        String startId = this.operator.timeMillis(ByteArrayTimeConvertor.getInstance()) + "-0";

        // 使用获取的起始ID和监听器创建一个消息消费者，并将其注册到监控器中。
        RedisCacheSyncMessageCodec codec = this.getCacheSyncMessageCodec(charset);
        this.container.subscribe(XStreamOffset.from(codec.encodeKey(channel), startId))
                .subscribe(new Subscriber<>() {
                    @Override
                    public void onNext(XStreamMessage<byte[], byte[]> element) {
                        listener.onMessage(codec.decodeMsg(element.body()));
                    }

                    @Override
                    public void onError(Throwable t, Subscription s) {
                        log.error("RedisCacheSyncProvider.subscribe has error:{}", t.getMessage(), t);
                        s.pausePull(Duration.ofMillis(100));
                    }

                    @Override
                    public void onError(Throwable t, XStreamMessage<byte[], byte[]> element,
                                        int attempts, RetrySubscription<XStreamMessage<byte[], byte[]>> s) {
                        log.error("RedisCacheSyncProvider.subscribe has error:{}", t.getMessage(), t);
                        s.retry(element, Duration.ofSeconds(Math.min(attempts, 3)));
                    }
                }, 2);
    }

    @Override
    public <V> CacheSyncMonitor getMonitor(SyncConfig<V> config) {
        long maxLen = config.getMaxLen();
        RedisCacheSyncMessageCodec codec = this.getCacheSyncMessageCodec(config.getCharset());
        XAddOptions options = XAddOptions.builder().maxLen(maxLen).approximateTrimming().build();

        byte[] stream = codec.encodeKey(config.getChannel());
        StreamPublisher<byte[], byte[], CacheSyncMessage> publisher =
                new StreamPublisher<>(stream, options, this.operator, codec);
        return new CacheSyncMonitorImpl(config, publisher::publish);
    }

    /**
     * 获取缓存同步消息编码器
     *
     * @return {@link RedisCacheSyncMessageCodec} – 用于对缓存同步消息进行编码和解码
     */
    private RedisCacheSyncMessageCodec getCacheSyncMessageCodec(Charset charset) {
        return messageCodecs.computeIfAbsent(charset, charset1 -> {
            StringCodec stringCodec = StringCodec.getInstance(charset1);
            Codec<Set<String>> setCodec = codecProvider.getSetCodec(charset1, String.class);
            return new RedisCacheSyncMessageCodec(setCodec, stringCodec);
        });
    }

}