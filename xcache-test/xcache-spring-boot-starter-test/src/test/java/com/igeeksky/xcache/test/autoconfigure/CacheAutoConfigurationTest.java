package com.igeeksky.xcache.test.autoconfigure;

import com.igeeksky.xcache.core.CacheManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * CacheManager ◊‘∂Ø≈‰÷√≤‚ ‘
 *
 * @author patrick
 * @since 0.0.4 2024/6/2
 */
@SpringBootTest
class CacheAutoConfigurationTest {

    private CacheManager cacheManager;

    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Test
    void getCacheManager() {
        assertNotNull(cacheManager);
    }

}