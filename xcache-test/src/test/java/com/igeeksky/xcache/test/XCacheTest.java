package com.igeeksky.xcache.test;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.CacheManager;
import com.igeeksky.xcache.common.CacheValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-19
 */
public class XCacheTest {

    private static final String application = "shop";
    private static final String namespace = "default";
    private static final String name = "test";
    private static final String name2 = "user";

    static CacheManager cacheManager;

    @BeforeAll
    static void cacheManager() {
//        ExtensionConfig stringSerializerConfig = new ExtensionConfig();
//        stringSerializerConfig.setClassName("com.igeeksky.xcache.core.extension.serialization.StringSerializer$StringSerializerProvider");
//        stringSerializerConfig.setId("stringSerializerProvider");
//
//        ExtensionConfig jacksonSerializerConfig = new ExtensionConfig();
//        jacksonSerializerConfig.setClassName("com.igeeksky.xcache.serialization.jackson.JacksonSerializerProvider");
//        jacksonSerializerConfig.setId("jacksonSerializerProvider");
//
//        ExtensionConfig compressorConfig = new ExtensionConfig();
//        compressorConfig.setClassName("com.igeeksky.xcache.core.extension.compress.GzipCompressor$GzipCompressorProvider");
//        compressorConfig.setId("gzipCompressorProvider");
//
//        ManagerProperties caffeineCacheConfig = new ManagerProperties();
//        caffeineCacheConfig.setClazz("com.igeeksky.xcache.support.caffeine.CaffeineCacheProvider");
//        caffeineCacheConfig.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_WRITE, "" + 2009900000000L);
//        caffeineCacheConfig.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_ACCESS, "" + 5000000000L);
//
//
//        ManagerProperties redisManagerProperties = new ManagerProperties();
//        redisManagerProperties.setClazz("com.igeeksky.xcache.support.redis.RedisCacheProvider");
//        redisManagerProperties.addMetadata(PropertiesKey.EXTENSION_REDIS_WRITER, "com.igeeksky.xcache.store.redis.lettuce.LettuceRedisWriterProvider");
//        redisManagerProperties.addMetadata(RedisPropertiesKey.HOST, "127.0.0.1");
//        redisManagerProperties.addMetadata(RedisPropertiesKey.PORT, "6379");
//        redisManagerProperties.addMetadata(RedisPropertiesKey.DATABASE, "0");
//        redisManagerProperties.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_WRITE, "" + 2009900000000L * 10);
//
//
//        CacheProperties stringL1CacheProperties = new CacheProperties();
//
//        stringL1CacheProperties.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_WRITE, "" + 2009900000000L);
//        stringL1CacheProperties.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_ACCESS, "" + 5000000000L);
//
//
//        CacheProperties stringL2CacheProperties = new CacheProperties();
//
//        stringL2CacheProperties.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_WRITE, "" + 2009900000000L * 10);
//
//
//        MultiCacheProperties stringMultiCacheProperties = new MultiCacheProperties(name);
//
//        CacheProperties userL1CacheProperties = new CacheProperties();
//
//        userL1CacheProperties.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_WRITE, "" + 2009900000000L);
//        userL1CacheProperties.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_ACCESS, "" + 5000000000L);
//
//
//        CacheProperties userL2CacheProperties = new CacheProperties();
//
//        userL2CacheProperties.addMetadata(PropertiesKey.METADATA_EXPIRE_AFTER_WRITE, "" + 2009900000000L * 10);
//
//        MultiCacheProperties userMultiCacheProperties = new MultiCacheProperties(name2);
//
//        XcacheProperties xcacheProperties = new XcacheProperties();
//        globalCacheConfig.setMultiCacheConfigByKey(name, stringMultiCacheConfig);
//        globalCacheConfig.setMultiCacheConfigByKey(name2, userMultiCacheConfig);
//
//        cacheManager = new GlobalCacheManager(xcacheProperties);
    }

    @Test
    public void testSyncSetGetString() {
        Cache<String, String> cache = cacheManager.getOrCreateCache(name, String.class, String.class, null);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            String kv = "aa" + i;
            cache.put(kv, kv);
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        for (int i = 0; i < 1000000; i++) {
            String kv = "aa" + i;
            CacheValue<String> value = cache.get(kv);
            if ((i & 4095) == 0) {
                System.out.println(value.getValue());
            }
        }
        long end2 = System.currentTimeMillis();
        System.out.println(end2 - end);
    }

    @Test
    public void testSyncSetGetUser() throws InterruptedException {
        Cache<String, User> cache = cacheManager.getOrCreateCache(name2, String.class, User.class, null);
        cache.put("bb", new User());
        CacheValue<User> cacheValue = cache.get("bb");
        System.out.println(cacheValue.getValue());
        Thread.sleep(1000);
    }

    public void otherTest() {

    }

    public static class User {
        private String name = "aa";
        private int age = 20;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        @Override
        public String toString() {
            return "{\"name\":\"" + name + "\",\"age\":" + age + "}";
        }
    }

}
