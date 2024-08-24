package com.igeeksky.xcache.test;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/28
 */
@Service
public class UserLoaderService {

    private Cache<Key, User> cache;

    @Resource
    private CacheManager cacheManager;

    @PostConstruct
    void postConstruct() {
        cache = cacheManager.getOrCreateCache("user", Key.class, User.class);
    }

    public void delete(Key key) {
        cache.evict(key);
    }

    public CacheValue<User> get(Key key) {
        return cache.get(key);
    }

    public User getOrLoad(Key key) {
        return cache.getOrLoad(key);
    }

}