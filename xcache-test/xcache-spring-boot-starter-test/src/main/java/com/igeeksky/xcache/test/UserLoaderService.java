package com.igeeksky.xcache.test;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * CacheLoader 和 CacheWriter 测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/28
 */
@Service
public class UserLoaderService {

    private final Cache<Key, User> cache;

    public UserLoaderService(CacheManager cacheManager) {
        cache = cacheManager.getOrCreateCache("user", Key.class, User.class);
    }

    public CacheValue<User> get(Key key) {
        return cache.getCacheValue(key);
    }

    public User getOrLoad(Key key) {
        return cache.getOrLoad(key);
    }

    public void put(Key key, User value) {
        cache.put(key, value);
    }

    public void putAll(Map<Key, User> keyValues) {
        cache.putAll(keyValues);
    }

    public Map<Key, CacheValue<User>> getAll(Set<Key> keys) {
        return cache.getAllCacheValues(keys);
    }

    public Map<Key, User> getOrLoadAll(Set<Key> keys) {
        return cache.getAllOrLoad(keys);
    }

    public void delete(Key key) {
        cache.remove(key);
    }

    public void deleteAll(Set<Key> keys) {
        cache.removeAll(keys);
    }

}