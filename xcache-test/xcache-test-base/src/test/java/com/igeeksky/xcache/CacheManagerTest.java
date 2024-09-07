package com.igeeksky.xcache;


import com.igeeksky.xcache.caffeine.CaffeineStoreProvider;
import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.core.CacheManagerConfig;
import com.igeeksky.xcache.core.CacheManagerImpl;
import com.igeeksky.xcache.domain.User;
import com.igeeksky.xcache.extension.codec.JdkCodecProvider;
import com.igeeksky.xcache.extension.compress.DeflaterCompressorProvider;
import com.igeeksky.xcache.extension.compress.GzipCompressorProvider;
import com.igeeksky.xcache.extension.contains.EmbedContainsPredicateProvider;
import com.igeeksky.xcache.extension.jackson.JacksonCodecProvider;
import com.igeeksky.xcache.extension.lock.EmbedCacheLockProvider;
import com.igeeksky.xcache.props.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-24
 */
class CacheManagerTest {

    private Cache<String, User> cache;

    @BeforeEach
    void setUp() {
        String name = "user";
        String app = "shop";

        Template t0 = PropsUtil.defaultTemplate(CacheConstants.DEFAULT_TEMPLATE_ID);

        t0.getCacheSync().setProvider(CacheConstants.NONE);

        CompressProps compress = new CompressProps();
        compress.setProvider(CacheConstants.DEFLATER_COMPRESSOR);
        compress.setLevel(9);
        compress.setNowrap(true);

        StoreProps first = t0.getFirst();
        first.setExpireAfterWrite(10000L);
        first.setExpireAfterAccess(2000L);
        first.setValueCodec(CacheConstants.JACKSON_CODEC);
        first.setValueCompressor(compress);

        t0.getSecond().setProvider(CacheConstants.NONE);
        t0.getThird().setProvider(CacheConstants.NONE);

        CacheManagerConfig managerConfig = CacheManagerConfig.builder()
                .app(app)
                .statPeriod(4000L)
                .scheduler(Executors.newSingleThreadScheduledExecutor())
                .template(t0)
                .build();

        CacheManager cacheManager = new CacheManagerImpl(managerConfig);
        cacheManager.addProvider(CacheConstants.DEFAULT_PREDICATE_PROVIDER, EmbedContainsPredicateProvider.getInstance());
        cacheManager.addProvider(CacheConstants.DEFAULT_LOCK_PROVIDER, EmbedCacheLockProvider.getInstance());
        cacheManager.addProvider(CacheConstants.DEFLATER_COMPRESSOR, DeflaterCompressorProvider.getInstance());
        cacheManager.addProvider(CacheConstants.GZIP_COMPRESSOR, GzipCompressorProvider.getInstance());
        cacheManager.addProvider(CacheConstants.JDK_CODEC, JdkCodecProvider.getInstance());
        cacheManager.addProvider(CacheConstants.JACKSON_CODEC, JacksonCodecProvider.getInstance());
        cacheManager.addProvider(CacheConstants.CAFFEINE_STORE, new CaffeineStoreProvider(null, null));

        cache = cacheManager.getOrCreateCache(name, String.class, User.class);
    }

    @Test
    void testGet() throws InterruptedException {
        User jack = new User("001", "Jack", 16);
        cache.put("001", jack);
        CacheValue<User> cacheValue = cache.get("001");
        System.out.println(1 + ":" + cacheValue.getValue());
        Assertions.assertEquals(cacheValue.getValue(), jack);

        Thread.sleep(5000L);
        cacheValue = cache.get("001");
        System.out.println(2 + ":" + cacheValue.getValue());
        Assertions.assertEquals(cacheValue.getValue(), jack);

        Thread.sleep(5000L);
        cacheValue = cache.get("001");
        System.out.println(3 + ":" + cacheValue);
        Assertions.assertNull(cacheValue);
    }

    @Test
    void testPut() {
        cache.put("001", null);
        CacheValue<User> cacheValue = cache.get("001");
        Assertions.assertNotNull(cacheValue);
        Assertions.assertNull(cacheValue.getValue());
    }

    @Test
    void getOrCreateCache() {
    }

    @Test
    void getAll() {
    }

    @Test
    void getAllCacheNames() {
    }
}