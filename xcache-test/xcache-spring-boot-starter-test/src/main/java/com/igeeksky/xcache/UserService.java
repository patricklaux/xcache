package com.igeeksky.xcache;

import com.igeeksky.xcache.annotation.*;
import com.igeeksky.xcache.core.Cache;
import com.igeeksky.xcache.core.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
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

    private Cache<Key, User> cache;

    @Resource(name = "xcacheManager")
    private CacheManager cacheManager;

    @PostConstruct
    public void init() {
        cache = cacheManager.getOrCreateCache("user", Key.class, User.class);
    }

    public void deleteUserByCache(Key key) {
        cache.evict(key);
    }

    public User getUserByCache(Key key) {
        System.out.println("getUserByCache: " + key);
        CacheValue<User> cacheValue = cache.get(key);
        return cacheValue != null ? cacheValue.getValue() : null;
    }

    public void saveUsersToCache(Map<Key, User> keyValues) {
        System.out.println("saveUserToCache");
        cache.putAll(keyValues);
    }

    @CacheEvict
    public void deleteByKey(Key key) {
        System.out.println("deleteByKey:" + key);
    }

    @Cacheable(unless = "#result.id == '0'")
    public User getUserByKeyUnless(Key key, int times) {
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
    public void saveUser(Key key, User user) {
        System.out.println("saveUser:" + key + ":" + user);
    }

    @CachePutAll
    public void saveUsers(Map<Key, User> users) {
        users.forEach((key, name) -> System.out.println("saveUser:" + key + ":" + name));
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
