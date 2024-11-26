package com.igeeksky.xcache.test;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author patrick
 * @since 0.0.4 2024/3/15
 */
@Service
@CacheConfig(cacheNames = "user")
public class SpringUserService {

    private Cache<Object, Object> cache;

    @Resource
    private CacheManager cacheManager;

    @PostConstruct
    public void init() {
        cache = cacheManager.getOrCreateCache("user", Object.class, Object.class);
    }

    public User getUserByCache(Key key) {
        System.out.println("getUserByCache: " + key);
        CacheValue<Object> cacheValue = cache.getCacheValue(key);
        if (cacheValue != null) {
            return (User) cacheValue.getValue();
        }
        return null;
    }

    public void saveUserByCache(Key key, User user) {
        System.out.println("saveUserByCache");
        cache.put(key, user);
    }

    public void saveUsersByCache(Map<Key, User> keyValues) {
        System.out.println("saveUsersByCache");
        cache.putAll(keyValues);
    }

    public void deleteUserByCache(Key key) {
        cache.remove(key);
    }

    @Cacheable(key = "#key", unless = "#result.id == '0'")
    public User getUserByKeyUnless(Key key, int times) {
        System.out.println("getUserByIdUnless:" + times);
        return new User(Integer.toString(times), key.getName(), key.getAge());
    }

    @Cacheable(key = "#key", condition = "#times < 2")
    public User getUserByKeyCondition(Key key, int times) {
        System.out.println("getUserByIdCondition:" + times);
        return new User(Integer.toString(times), key.getName(), key.getAge());
    }

    @CachePut(key = "#key")
    public User saveUser(Key key, User user) {
        System.out.println("saveUser:" + key + ":" + user);
        return user;
    }

    @CacheEvict(key = "#key")
    public void deleteUser(Key key) {
        System.out.println("deleteUser:" + key);
    }

    @CacheEvict(allEntries = true)
    public void deleteAllUsers() {
        System.out.println("deleteAllUsers");
    }

}