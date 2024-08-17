package com.igeeksky.xcache.core;

import com.igeeksky.xcache.Cache;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import org.junit.jupiter.api.Assertions;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/9
 */
public class CacheTestCase {

    private final Cache<Key, User> cache;

    public CacheTestCase(Cache<Key, User> cache) {
        this.cache = cache;
    }

    String getName() {
        return cache.getName();
    }

    void getKeyType() {
        Assertions.assertEquals(Key.class, cache.getKeyType());
    }

    void getValueType() {
        Assertions.assertEquals(User.class, cache.getValueType());
    }

    void get() {

    }

    void getWithCacheLoader() {
        String name = "patrick";

        Key key = new Key(name);
        cache.evict(key);

        Assertions.assertNotNull(cache.get(key, k -> null));

        User user = new User(name);
        cache.put(key, user);

        Assertions.assertEquals(user, cache.get(key, k -> null));

        cache.evict(key);

        Assertions.assertEquals(user, cache.get(key, k -> new User(name)));
    }

    void getAll() {

    }

    void put() {

    }

    void putAll() {

    }

    void evict() {

    }

    void evictAll() {

    }

    void clear() {

    }
}
