package com.igeeksky.xcache.test.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.test.UserLoaderService;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/28
 */
@SpringBootTest
public class CacheLoaderTest {

    @Resource
    private UserLoaderService loaderService;

    @Test
    void test() {
        Key key = new Key(10, "Lucy001");
        loaderService.delete(key);

        CacheValue<User> cacheValue = loaderService.get(key);
        Assertions.assertNull(cacheValue);

        User load = loaderService.getOrLoad(key);
        Assertions.assertEquals("loader:Lucy001", load.getId());
    }

}
