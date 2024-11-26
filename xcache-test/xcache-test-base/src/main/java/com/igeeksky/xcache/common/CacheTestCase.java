package com.igeeksky.xcache.common;

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

    void getKeyParams() {
        Assertions.assertEquals(0, cache.getKeyParams().length);
    }

    void getValueType() {
        Assertions.assertEquals(User.class, cache.getValueType());
    }

    void getValueParams() {
        Assertions.assertEquals(0, cache.getValueParams().length);
    }

    void get() {

    }

    void getWithCacheLoader() {
        String name = "patrick";

        Key key = new Key(name);
        cache.remove(key);

        Assertions.assertNotNull(cache.getOrLoad(key, k -> null));

        User user = new User(name);
        cache.put(key, user);

        Assertions.assertEquals(user, cache.getOrLoad(key, k -> null));

        cache.remove(key);

        Assertions.assertEquals(user, cache.getOrLoad(key, k -> new User(name)));
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
