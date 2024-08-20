package com.igeeksky.xcache;


import com.igeeksky.xcache.caffeine.CaffeineStoreProvider;
import com.igeeksky.xcache.core.Cache;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.CacheManagerImpl;
import com.igeeksky.xcache.core.ComponentRegister;
import com.igeeksky.xcache.domain.User;
import com.igeeksky.xcache.extension.jackson.JacksonCodecProvider;
import com.igeeksky.xcache.props.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-24
 */
class CacheManagerTest {

    private Cache<String, User> cache;
    private CacheManagerImpl cacheManager;

    @BeforeEach
    void setUp() {
        String name = "user";
        String application = "shop";

        String id = CacheConstants.DEFAULT_TEMPLATE_ID;
        Template t0 = PropsUtil.defaultTemplate(id);

        SyncProps cacheSync = t0.getCacheSync();
        cacheSync.setProvider(CacheConstants.NONE);

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

        Map<String, Template> templatesMap = new HashMap<>();
        templatesMap.put(id, t0);

        Map<String, CacheProps> propsMap = new HashMap<>();
        // propsMap.put(name, t0);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        ComponentRegister register = new ComponentRegister(scheduler, 4000L);

        cacheManager = new CacheManagerImpl(application, register, templatesMap, propsMap);

        register.embedContainsPredicate(cacheManager);
        register.embedCacheLock(cacheManager);
        register.deflaterCompressor(cacheManager);
        register.gzipCompressor(cacheManager);
        register.jdkCodec(cacheManager);

        // cacheManager.addProvider("lettuceCacheSyncManager", new LettuceCacheSyncManager());
        // cacheManager.addProvider("lettuceCacheStoreProvider", new LettuceCacheStoreProvider());


        cacheManager.addProvider(CacheConstants.JACKSON_CODEC, JacksonCodecProvider.getInstance());
        cacheManager.addProvider(CacheConstants.CAFFEINE_STORE, new CaffeineStoreProvider(null, null));

        cache = cacheManager.getOrCreateCache(name, String.class, null, User.class, null);
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