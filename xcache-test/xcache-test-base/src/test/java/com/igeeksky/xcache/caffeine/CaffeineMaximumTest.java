package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试 Caffeine 缓存最大容量
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-14
 */
class CaffeineMaximumTest {

    Cache<Integer, Integer> cache;

    @BeforeEach
    void setUp() {
        cache = Caffeine.newBuilder().maximumSize(16).build();
    }

    @Test
    void get() {
        for (int i = 0; i < 200; i++) {
            cache.put(i, i);
            if (i > 100) {
                System.out.println(cache.getIfPresent(i - 50));
            }
        }
    }

}