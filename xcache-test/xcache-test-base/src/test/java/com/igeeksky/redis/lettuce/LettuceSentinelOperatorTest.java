package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.RedisOperatorTestCase;
import org.junit.jupiter.api.*;

/**
 * Lettuce 哨兵操作测试
 *
 * @author patrick
 * @since 0.0.4 2024/5/8
 */
class LettuceSentinelOperatorTest {

    private static RedisOperatorTestCase redisTestCase;
    private static LettuceSentinelFactory sentinelFactory;

    @BeforeAll
    public static void beforeAll() {
        sentinelFactory = LettuceTestHelper.createSentinelFactory();
        redisTestCase = new RedisOperatorTestCase(sentinelFactory.getRedisOperator());
    }

    @AfterAll
    public static void afterAll() {
        sentinelFactory.close();
    }

    @Test
    void isCluster() {
        Assertions.assertFalse(redisTestCase.isCluster());
    }

    // String Command --start--
    @Test
    void get() {
        redisTestCase.get();
    }

    @Test
    void mget() {
        redisTestCase.mget();
    }

    @Test
    public void set() {
        redisTestCase.set();
    }

    /**
     * 性能测试
     * <p>
     * 10000000数据，本地redis，单线程性能测试时长约 785098 ms
     */
    @Test
    @Disabled
    void psetex() {
        redisTestCase.psetex();
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
        redisTestCase.psetex2();
    }

    @Test
    void mset() {
        redisTestCase.mset();
    }

    /**
     * 性能测试
     * <p>
     * 1000 万数据，本地redis，单线程批处理，性能测试时长约 32221 毫秒
     */
    @Test
    @Disabled
    void msetPerformance() throws InterruptedException {
        redisTestCase.msetPerformance();

        Thread.sleep(3000);

        redisTestCase.clear("test-mset:*");
    }

    /**
     * 性能测试
     * <p>
     * 1000 万数据，本地redis，2线程批处理，性能测试时长约 16609 * 2 毫秒
     * <p>
     * 1000 万数据，本地redis，单线程批处理，性能测试时长约 32221 毫秒
     */
    @Test
    @Disabled
    void msetPerformance2() throws InterruptedException {
        redisTestCase.msetPerformance2();

        Thread.sleep(3000);

        redisTestCase.clear("test-mset:*");
    }

    @Test
    void mpsetex() {
        redisTestCase.mpsetex();
    }

    /**
     * 性能测试
     * <p>
     * 10000000数据，本地redis，单线程批处理，性能测试时长约 28 秒
     */
    @Test
    @Disabled
    void mpsetex1() throws InterruptedException {
        redisTestCase.mpsetex1();

        Thread.sleep(3000);

        redisTestCase.clear("test-mpsetex:*");
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
     */
    @Test
    @Disabled
    void mpsetex2() throws InterruptedException {
        redisTestCase.mpsetex2();

        Thread.sleep(3000);

        redisTestCase.clear("test-mpsetex:*");
    }

    @Test
    void del() {
        redisTestCase.del();
    }
    // String Command --end--

    // Hash Command --start--
    @Test
    void hget() {
        redisTestCase.hget();
    }

    @Test
    void hset() {
        redisTestCase.hset();
    }

    @Test
    void hmget() {
        redisTestCase.hmget();
    }

    @Test
    void hmset() {
        redisTestCase.hmset();
    }

    @Test
    void hdel() {
        redisTestCase.hdel();
    }
    // Hash Command --end--

    @Test
    void keys() {
        redisTestCase.keys();
    }

}