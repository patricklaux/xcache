package com.igeeksky.xcache.test;

import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Spring cache 注解测试，用户缓存服务
 *
 * @author patrick
 * @since 0.0.4 2024/3/15
 */
@Service
@CacheConfig(cacheNames = "user")
public class SpringUserService {

    private final Cache<Object, Object> cache;

    /**
     * 构造函数
     *
     * @param cacheManager 缓存管理器
     */
    public SpringUserService(CacheManager cacheManager) {
        this.cache = cacheManager.getOrCreateCache("user", Object.class, Object.class);
    }

    /**
     * 根据 key 获取用户数据
     *
     * @param key 缓存key
     * @return 缓存数据
     */
    public User getUserByCache(Key key) {

        System.out.println("getUserByCache: " + key);
        CacheValue<Object> cacheValue = cache.getCacheValue(key);
        if (cacheValue != null) {
            return (User) cacheValue.getValue();
        }
        return null;
    }

    /**
     * 保存用户数据
     *
     * @param key  缓存key
     * @param user 用户数据
     */
    public void saveUserByCache(Key key, User user) {
        System.out.println("saveUserByCache");
        cache.put(key, user);
    }

    /**
     * 批量保存用户数据
     *
     * @param keyValues 缓存key-value
     */
    public void saveUsersByCache(Map<Key, User> keyValues) {
        System.out.println("saveUsersByCache");
        cache.putAll(keyValues);
    }

    /**
     * 删除用户数据
     *
     * @param key 缓存key
     */
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