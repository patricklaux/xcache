package com.igeeksky.xcache.test;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/28
 */
@Service
public class UserLoaderWriterService {

    private Cache<Key, User> cache;

    @Resource
    private CacheManager cacheManager;

    @PostConstruct
    void postConstruct() {
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