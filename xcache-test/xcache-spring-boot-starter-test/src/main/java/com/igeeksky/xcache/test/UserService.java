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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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
        cache.remove(key);
    }

    public CacheValue<User> getUserByCache(Key key) {
        System.out.println("getUserByCache: " + key);
        return cache.getCacheValue(key);
    }

    public void saveUsersToCache(Map<Key, User> keyValues) {
        System.out.println("saveUserToCache");
        cache.putAll(keyValues);
    }

    /**
     * "#result" 获取的是方法返回值，而不是参数 result
     */
    @CachePut(value = "#result")
    public User save(Key key, User result) {
        return new User("1", "MethodResult", 18);
    }

    @CacheRemove
    public void deleteByKey(Key key) {
        System.out.println("deleteByKey:" + key);
    }

    @Cacheable
    public User getUser(Key key, int times) {
        System.out.println("getUser:" + times);
        return new User(Integer.toString(times), key.getName(), key.getAge());
    }

    @Cacheable
    public Optional<User> getOptionalUser(Key key, int times) {
        System.out.println("getOptionalUser:" + times);
        return Optional.of(new User(Integer.toString(times), key.getName(), key.getAge()));
    }

    @Cacheable
    public CompletableFuture<User> getFutureUser(Key key, int times) {
        System.out.println("getFutureUser:" + times);
        return CompletableFuture.completedFuture(new User(Integer.toString(times), key.getName(), key.getAge()));
    }

    /**
     * 方法返回值 CompletableFuture 不能为 null，否则缓存实现将与方法返回值不同
     * 当值不存在时，缓存的实现是 CompletableFuture.completedFuture(null)
     *
     * @param key 键
     * @return CompletableFuture 不能为 null
     */
    @Cacheable
    public CompletableFuture<User> getNullFutureUser(Key key) {
        return CompletableFuture.completedFuture(null);
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

    @CacheRemoveAll
    public void deleteUsers(Set<Key> ids) {
        System.out.println("deleteUsers:" + ids);
    }

    @CacheClear
    public void deleteAllUsers() {
        System.out.println("deleteAllUsers");
    }

}
