package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xcache.common.ShutdownBehavior;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.redis.LettuceTestHelper;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xredis.lettuce.LettuceOperator;
import com.igeeksky.xredis.lettuce.LettuceOperatorProxy;
import io.lettuce.core.codec.ByteArrayCodec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisCacheRefreshTest {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheRefreshTest.class);

    private static RedisCacheRefresh refresh1;
    private static RedisCacheRefresh refresh2;
    private static LettuceOperator<byte[], byte[]> redisOperator;

    @BeforeAll
    static void beforeAll() {
        redisOperator = LettuceTestHelper.createStandaloneFactory().redisOperator(ByteArrayCodec.INSTANCE);
        RedisOperatorProxy operatorProxy = new LettuceOperatorProxy(redisOperator);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        RefreshConfig.Builder builder = RefreshConfig.builder();
        builder.name("user")
                .group("shop")
                .charset(StandardCharsets.UTF_8)
                .provider("test")
                .refreshAfterWrite(50)
                .refreshThreadPeriod(1000)
                .refreshSlotSize(16)
                .refreshTaskSize(16)
                .shutdownTimeout(1)
                .shutdownQuietPeriod(0)
                .shutdownBehavior(ShutdownBehavior.AWAIT)
                .enableGroupPrefix(true);
        RefreshConfig config1 = builder.sid("test1").build();
        RefreshConfig config2 = builder.sid("test2").refreshThreadPeriod(1000).build();

        refresh1 = new RedisCacheRefresh(config1, scheduler, executor, operatorProxy);
        refresh2 = new RedisCacheRefresh(config2, scheduler, executor, operatorProxy);
        refresh1.startRefresh(key -> {
            log.info("refresh1:{}", key);
            // LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        }, key -> true);
        refresh2.startRefresh(key -> {
            log.info("refresh2:{}", key);
            // LockSupport.parkNanos(Duration.ofMillis(100).toNanos());
        }, key -> true);
    }

    @AfterAll
    static void afterAll() {
        refresh1.shutdown();
        refresh2.shutdown();
        redisOperator.closeAsync();
    }

    @Test
    @Disabled
    void test() {
        for (int i = 0; i < 10000; i++) {
            refresh1.onPut("key1:" + i);
            refresh2.onPut("key2:" + i);
        }

        LockSupport.parkNanos(Duration.ofMillis(2000).toNanos());
    }

    @Test
    @Disabled
    void testPutAll() {
        Set<String> keys1 = HashSet.newHashSet(10000);
        Set<String> keys2 = HashSet.newHashSet(10000);
        for (int i = 0; i < 10000; i++) {
            keys1.add("key1:" + i);
            keys2.add("key2:" + i);
        }

        refresh1.onPutAll(keys1);
        refresh2.onPutAll(keys2);

        LockSupport.parkNanos(Duration.ofMillis(2000).toNanos());

        refresh2.onRemoveAll(keys1);
        refresh2.onRemoveAll(keys2);
    }

}
