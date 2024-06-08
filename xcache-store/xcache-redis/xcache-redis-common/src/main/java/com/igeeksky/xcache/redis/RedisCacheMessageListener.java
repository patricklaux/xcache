package com.igeeksky.xcache.redis;

import com.igeeksky.xcache.extension.CacheMessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class RedisCacheMessageListener implements RedisPubSubListener {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheMessageListener.class);

    private final ConcurrentMap<String, CacheMessageConsumer> consumerMap = new ConcurrentHashMap<>();

    public void register(String channel, CacheMessageConsumer consumer) {
        this.consumerMap.put(channel, consumer);
    }

    @Override
    public void message(String channel, byte[] message) {
        CacheMessageConsumer consumer = consumerMap.get(channel);
        if (consumer != null) {
            consumer.onMessage(message);
            return;
        }
        log.warn("No consumer to process this message. chan:[{}]", channel);
    }

    @Override
    public void message(String pattern, String channel, byte[] message) {
        CacheMessageConsumer consumer = consumerMap.get(channel);
        if (consumer != null) {
            consumer.onMessage(message);
            return;
        }
        log.warn("No consumer to process this message. pattern:[{}],  chan:[{}]", pattern, channel);
    }

    @Override
    public void subscribed(String channel, long count) {
        if (log.isDebugEnabled()) {
            log.debug("subscribed-channel:[{}], count:[{}]", channel, count);
        }
    }

    @Override
    public void psubscribed(String pattern, long count) {
        if (log.isDebugEnabled()) {
            log.debug("psubscribed-pattern:[{}], count:[{}]", pattern, count);
        }
    }

    @Override
    public void unsubscribed(String channel, long count) {
        if (log.isDebugEnabled()) {
            log.debug("unsubscribed-channel:[{}], count:[{}]", channel, count);
        }
    }

    @Override
    public void punsubscribed(String pattern, long count) {
        if (log.isDebugEnabled()) {
            log.debug("punsubscribed-pattern:[{}], count:[{}]", pattern, count);
        }
    }
}