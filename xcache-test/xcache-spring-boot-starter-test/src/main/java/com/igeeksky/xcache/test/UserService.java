package com.igeeksky.xcache.test;

import com.igeeksky.xcache.annotation.*;
import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author patrick
 * @since 0.0.4 2024/3/15
 */
@Service
@CacheConfig(name = "user", keyType = Key.class, valueType = User.class)
public class UserService {

    private final Cache<Key, User> cache;

    public UserService(CacheManager cacheManager) {
        this.cache = cacheManager.getOrCreateCache("user", Key.class, User.class);
    }

    public void deleteUserByCache(Key key) {
        cache.evict(key);
    }

    public User getUserByCache(Key key) {
        System.out.println("getUserByCache: " + key);
        CacheValue<User> cacheValue = cache.get(key);
        return (cacheValue != null) ? cacheValue.getValue() : null;
    }

    public void saveUsersToCache(Map<Key, User> keyValues) {
        System.out.println("saveUserToCache");
        cache.putAll(keyValues);
    }

    @CacheEvict
    public void deleteByKey(Key key) {
        System.out.println("deleteByKey:" + key);
    }

    @Cacheable
    public User getUser(Key key, int times) {
        System.out.println("getUserByIdUnless:" + times);
        return new User(Integer.toString(times), key.getName(), key.getAge());
    }

    @Cacheable(condition = "#times < 2")
    public User getUserByKeyCondition(Key key, int times) {
        System.out.println("getUserByIdCondition:" + times);
        return new User(Integer.toString(times), key.getName(), key.getAge());
    }

    @CacheableAll
    public Map<Key, User> getUserList(Set<Key> keys, int times) {
        System.out.println("getUserList:" + keys);
        Map<Key, User> users = new HashMap<>(keys.size() * 2);
        keys.forEach(key -> users.put(key, new User(Integer.toString(times), key.getName(), key.getAge())));
        return users;
    }

    @CachePut
    public User saveUser(Key key, User user) {
        System.out.println("saveUser:" + key + ":" + user);
        return user;
    }

    @CachePutAll
    public Map<Key, User> saveUsers(Map<Key, User> users) {
        users.forEach((key, name) -> System.out.println("saveUser:" + key + ":" + name));
        return users;
    }

    @CacheEvictAll
    public void deleteUsers(Set<Key> ids) {
        System.out.println("deleteUsers:" + ids);
    }

    @CacheClear
    public void deleteAllUsers() {
        System.out.println("deleteAllUsers");
    }

}
