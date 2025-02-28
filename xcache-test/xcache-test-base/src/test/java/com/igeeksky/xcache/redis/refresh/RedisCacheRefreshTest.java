package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.redis.LettuceTestHelper;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xredis.lettuce.LettuceOperator;
import com.igeeksky.xredis.lettuce.LettuceOperatorProxy;
import io.lettuce.core.codec.ByteArrayCodec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisCacheRefreshTest {

    private static RedisCacheRefresh refresh1;
    private static RedisCacheRefresh refresh2;
    private static RedisOperatorProxy operatorProxy;
    private static LettuceOperator<byte[], byte[]> redisOperator;

    @BeforeAll
    static void beforeAll() {
        redisOperator = LettuceTestHelper.createStandaloneFactory().redisOperator(ByteArrayCodec.INSTANCE);
        operatorProxy = new LettuceOperatorProxy(redisOperator);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        RefreshConfig.Builder builder = RefreshConfig.builder();
        builder.name("user")
                .group("shop")
                .charset(StandardCharsets.UTF_8)
                .provider("test")
                .refreshTasksSize(10)
                .refreshAfterWrite(500)
                .refreshThreadPeriod(2000)
                .refreshSequenceSize(32)
                .enableGroupPrefix(true);
        RefreshConfig config1 = builder.sid("test1").build();
        RefreshConfig config2 = builder.sid("test2").refreshThreadPeriod(1999).build();

        refresh1 = new RedisCacheRefresh(config1, scheduler, executor, operatorProxy, 2000);
        refresh2 = new RedisCacheRefresh(config2, scheduler, executor, operatorProxy, 2000);
        refresh1.startRefresh(key -> {
            System.out.println("refresh1:" + key);
            LockSupport.parkNanos(Duration.ofMillis(1000).toNanos());
        }, key -> true);
        refresh2.startRefresh(key -> {
            System.out.println("refresh2:" + key);
            LockSupport.parkNanos(Duration.ofMillis(1000).toNanos());
        }, key -> true);
    }

    @AfterAll
    static void afterAll() {
        refresh1.close();
        refresh2.close();
    }

    @Test
    void test() {
        refresh1.onPut("key1");
        refresh2.onPut("key2");

        LockSupport.parkNanos(Duration.ofMillis(30000).toNanos());

        LockSupport.parkNanos(Duration.ofMillis(5000).toNanos());
    }

}
