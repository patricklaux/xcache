package com.igeeksky.xcache.test;

import com.igeeksky.xcache.core.CacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CacheManager 自动配置测试
 *
 * @author patrick
 * @since 0.0.4 2024/6/2
 */
@SpringBootTest
class CacheAutoConfigurationTest {

    private final CacheManager cacheManager;

    @Autowired
    public CacheAutoConfigurationTest(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Test
    void getCacheManager() {
        assertNotNull(cacheManager);
    }

}