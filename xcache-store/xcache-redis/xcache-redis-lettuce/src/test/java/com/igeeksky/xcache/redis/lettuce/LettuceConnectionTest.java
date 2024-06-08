package com.igeeksky.xcache.redis.lettuce;

import com.igeeksky.xcache.redis.RedisConnectionTest;
import org.junit.jupiter.api.*;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/5/8
 */
class LettuceConnectionTest {

    private static LettuceConnectionFactory factory;

    private static RedisConnectionTest redisConnectionTest;

    @BeforeAll
    public static void beforeAll() {
        factory = LettuceTestHelper.createStandaloneConnectionFactory();
        redisConnectionTest = new RedisConnectionTest(factory.getConnection());
    }

    @Test
    void isCluster() {
        Assertions.assertFalse(redisConnectionTest.isCluster());
    }

    // String Command --start--
    @Test
    void get() {
        redisConnectionTest.get();
    }

    @Test
    void mget() {
        redisConnectionTest.mget();
    }

    @Test
    public void set() {
        redisConnectionTest.set();
    }

    /**
     * 性能测试
     * <p>
     * 10000000数据，本地redis，单线程性能测试时长约 785098 ms
     */
    @Test
    @Disabled
    void psetex() {
        redisConnectionTest.psetex();
    }

    /**
     * 性能测试
     * <p>
     * 10000000数据，本地redis，10线程性能测试，单个线程耗时约 197383 ms，总耗时约为 197383 * 10
     * <p>
     * 10000000数据，本地redis，单线程性能测试时长约 785098 ms
     * <p>
     * 测试结果表明，即使只有一个 Lettuce 连接，依然可以提供并行处理能力，从而降低时长。
     * 因此，Lettuce 在不使用事务(MULTI)及阻塞命令 (BLPOP……)时，无需开启连接池。
     *
     * @throws InterruptedException 中断异常
     */
    @Test
    @Disabled
    void psetex2() throws InterruptedException {
        redisConnectionTest.psetex2();
    }

    @Test
    void mset() {
        redisConnectionTest.mset();
    }

    @Test
    void mpsetex() throws InterruptedException {
        redisConnectionTest.mpsetex();
    }

    /**
     * 性能测试
     * <p>
     * 10000000数据，本地redis，单线程批处理，性能测试时长约 28 秒
     *
     * @throws InterruptedException 中断异常
     */
    @Test
    @Disabled
    void mpsetex1() throws InterruptedException {
        redisConnectionTest.mpsetex1();
    }

    /**
     * 性能测试
     * <p>
     * 10000000数据，本地redis，4线程批处理，性能测试时长约 35 * 4 秒
     * <p>
     * 10000000数据，本地redis，单线程批处理，性能测试时长约 28 秒
     * <p>
     * 测试结果表明：
     * 批处理能够有效提高性能，此时的性能瓶颈在于跨进程跨网络数据交换；
     * 多线程调用批处理命令反而会更慢，估计与 Lettuce 内部的 queue 实现有关；
     * Lettuce 在不使用事务(MULTI)及阻塞命令 (,BLPOP……)时，无需开启连接池
     *
     * @throws InterruptedException 中断异常
     */
    @Test
    @Disabled
    void mpsetex2() throws InterruptedException {
        redisConnectionTest.mpsetex2();
    }

    @Test
    void del() {
        redisConnectionTest.del();
    }
    // String Command --end--

    // Hash Command --start--
    @Test
    void hget() {
        redisConnectionTest.hget();
    }

    @Test
    void hset() {
        redisConnectionTest.hset();
    }

    @Test
    void hmget() {
        redisConnectionTest.hmget();
    }

    @Test
    void hmset() {
        redisConnectionTest.hmset();
    }

    @Test
    void hdel() {
        redisConnectionTest.hdel();
    }
    // Hash Command --end--

    @Test
    void keys() {
        redisConnectionTest.keys();
    }

    @Test
    void clear() {
        redisConnectionTest.clear();
    }

    @AfterAll
    public static void release() {
        factory.close();
    }

}