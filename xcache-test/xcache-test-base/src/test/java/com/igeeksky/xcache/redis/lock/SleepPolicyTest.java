package com.igeeksky.xcache.redis.lock;

import com.igeeksky.xtool.core.lang.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 锁休眠策略测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/27
 */
class SleepPolicyTest {

    @Test
    void getNext() {
        SleepPolicy policy = new SleepPolicy(1000, 10);
        for (int i = 0; i < 100; i++) {
            long next = policy.getNext(RandomUtils.nextLong(10, 100));
            System.out.println(next);
            Assertions.assertTrue(next > 0);
        }
    }

    @Test
    void testGetNext() {
        SleepPolicy policy = new SleepPolicy(10);
        for (int i = 0; i < 100; i++) {
            long next = policy.getNext(RandomUtils.nextLong(10, 1000));
            System.out.println(next);
            Assertions.assertTrue(next >= 5);
            Assertions.assertTrue(next <= 25);
        }
    }

}