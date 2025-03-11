package com.igeeksky.xcache.test;

import com.igeeksky.xcache.annotation.*;
import com.igeeksky.xcache.common.Cache;
import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.core.CacheManager;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Xcache 注解测试
 *
 * @author patrick
 * @since 0.0.4 2024/3/15
 */
@Service
@CacheConfig(name = "user", keyType = Key.class, valueType = User.class)
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final Cache<Key, User> cache;

    /**
     * 构造方法注入
     *
     * @param cacheManager CacheManager
     */
    public UserService(CacheManager cacheManager) {
        this.cache = cacheManager.getOrCreateCache("user", Key.class, User.class);
    }

    public void deleteUserByCache(Key key) {
        cache.remove(key);
    }

    public CacheValue<User> getUserByCache(Key key) {
        log.info("getUserByCache: key: {}", key);
        return cache.getCacheValue(key);
    }

    public void saveUsersToCache(Map<Key, User> keyValues) {
        log.info("saveUsersToCache: keyValues: {}", keyValues);
        cache.putAll(keyValues);
    }

    /**
     * 默认缓存方法返回值
     */
    @CachePut
    public User saveUser(Key key, User user) {
        log.info("saveUser: key:{}, user:{}", key, user);
        return user;
    }

    /**
     * 默认缓存方法返回值
     */
    @CachePut
    public User saveCacheMethodResult(Key key) {
        log.info("saveCacheMethodResult: key:{}", key);
        return new User("1", "MethodResult", 18);
    }

    /**
     * 缓存方法返回值
     * <p>
     * 方法返回值默认采用 {@code result} 作为键保存。
     * 如方法参数名也为 {@code result}，则 "#result" 获取到的是方法返回值，而不是方法参数值。
     */
    @CachePut(value = "#result")
    public User saveCacheMethodResult(Key key, User result) {
        log.info("saveCacheMethodResult: key:{}, result:{}", key, result);
        return new User("1", "MethodResult", 18);
    }

    /**
     * 缓存方法参数值
     * <p>
     * 方法参数值默认采用 {@code result} 作为键保存。
     * 如方法参数名也为 {@code result}，如想缓存方法参数值，则需通过 {@code "#p" + index} 获取方法参数值。
     */
    @CachePut(value = "#p1")
    public User saveCacheParamsResult(Key key, User result) {
        log.info("saveCacheParamsResult: key:{}, result:{}", key, result);
        return new User("1", "MethodResult", 18);
    }

    /**
     * 通过表达式 {@code #key & #user} 获取 {@code key & value}
     */
    @CachePut(key = "#key", value = "#user")
    public User saveByEvalKeyValue(User user, Key key) {
        log.info("saveByEvalKeyValue: key:{}, user:{}", key, user);
        return new User("1", "MethodResult", 18);
    }

    /**
     * 通过 condition 表达式 判断是否缓存
     * <p>
     * 年龄大于 18岁才缓存，即 condition 表达式计算结果为 true 时才缓存。
     */
    @CachePut(key = "#key", condition = "#user.age > 18")
    public User saveByEvalCondition(Key key, User user) {
        log.info("saveByEvalCondition: key:{}, user:{}", key, user);
        return user;
    }

    /**
     * 通过 unless 表达式 判断是否缓存
     * <p>
     * 年龄大于 18岁不缓存，即 unless 表达式计算结果为 false 时才缓存。
     */
    @CachePut(key = "#key", unless = "#result.age > 18")
    public User saveByUnless(Key key, User user) {
        log.info("saveByUnless: key:{}, user:{}", key, user);
        return user;
    }

    /**
     * 无 keyValues 表达式：默认缓存方法返回值
     */
    @CachePutAll
    public Map<Key, User> saveUsers(Map<Key, User> users) {
        log.info("saveUsers: users:{}", users);
        Map<Key, User> results = HashMap.newHashMap(users.size());
        users.forEach((key, user) -> {
            User clone = user.clone();
            clone.setAge(user.getAge() + 1);
            results.put(key, clone);
        });
        return results;
    }

    /**
     * 有 keyValues 表达式：缓存表达式计算结果，即方法参数值
     */
    @CachePutAll(keyValues = "#users")
    public Map<Key, User> saveUsersByEvalKeyValues(Map<Key, User> users) {
        log.info("saveUsersByEvalKeyValues: users:{}", users);
        Map<Key, User> results = HashMap.newHashMap(users.size());
        users.forEach((key, user) -> {
            User clone = user.clone();
            clone.setAge(user.getAge() + 1);
            results.put(key, clone);
        });
        return results;
    }

    /**
     * condition 表达式计算结果为 true 时才缓存：即年龄大于 18岁才缓存
     */
    @CachePutAll(condition = "#users.values.?[getAge > 18].size > 0")
    public Map<Key, User> saveUsersByCondition(Map<Key, User> users) {
        log.info("saveUsersByCondition: users:{}", users);
        Map<Key, User> results = HashMap.newHashMap(users.size());
        users.forEach((key, user) -> {
            User clone = user.clone();
            clone.setAge(user.getAge() + 1);
            results.put(key, clone);
        });
        return results;
    }

    /**
     * unless 表达式计算结果为 false 时才缓存：即年龄小于等于 18岁才缓存
     */
    @CachePutAll(unless = "#users.values.?[getAge > 18].size > 0")
    public Map<Key, User> saveUsersByUnless(Map<Key, User> users) {
        log.info("saveUsersByUnless: users:{}", users);
        Map<Key, User> results = HashMap.newHashMap(users.size());
        users.forEach((key, user) -> {
            User clone = user.clone();
            clone.setAge(user.getAge() + 1);
            results.put(key, clone);
        });
        return results;
    }

    @Cacheable
    public User getUser(Key key, int times) {
        log.info("getUser: key:{}, times:{}", key, times);
        return new User(Integer.toString(times), key.getName(), key.getAge());
    }

    @Cacheable(key = "#key2")
    public User getUserByEvalKey(Key key1, Key key2) {
        log.info("getUserByEvalKey: key1:{}, key2:{}", key1, key2);
        return new User(Integer.toString(2), key2.getName(), key2.getAge());
    }

    @Cacheable
    public Optional<User> getOptionalUser(Key key, int times) {
        log.info("getOptionalUser: key:{}, times:{}", key, times);
        return Optional.of(new User(Integer.toString(times), key.getName(), key.getAge()));
    }

    @Cacheable
    public CompletableFuture<User> getFutureUser(Key key, int times) {
        log.info("getFutureUser: key:{}, times:{}", key, times);
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
        log.info("getNullFutureUser:{}", key);
        return CompletableFuture.completedFuture(null);
    }

    @Cacheable(condition = "#times < 2")
    public User getUserByCondition(Key key, int times) {
        log.info("getUserByKeyCondition:times {}", times);
        return new User(Integer.toString(times), key.getName(), key.getAge());
    }

    @CacheableAll
    public Map<Key, User> getUserList(Set<Key> keys, int times) {
        if (times >= 1) {
            throw new RuntimeException("times >= 2");
        }
        log.info("getUserList: keys:{} times:{}", keys, times);
        Map<Key, User> users = HashMap.newHashMap(keys.size());
        keys.forEach(key -> users.put(key, new User(Integer.toString(times), key.getName(), key.getAge())));
        return users;
    }

    @CacheableAll(keys = "#keys2")
    public Map<Key, User> getUserListByEvalKeys(Set<Key> keys1, Set<Key> keys2) {
        log.info("getUserListByEvalKeys: keys1:{} keys2:{}", keys1, keys2);
        Map<Key, User> users = HashMap.newHashMap(keys2.size());
        keys1.forEach(key -> users.put(key, new User(Integer.toString(0), key.getName(), key.getAge())));
        keys2.forEach(key -> users.put(key, new User(Integer.toString(0), key.getName(), key.getAge())));
        return users;
    }

    @CacheableAll(condition = "#keys.?[getAge > 18].size > 0")
    public Map<Key, User> getUserListByCondition(Set<Key> keys) {
        log.info("getUserListByEvalKeys: keys:{}", keys);
        Map<Key, User> users = HashMap.newHashMap(keys.size());
        keys.forEach(key -> users.put(key, new User(Integer.toString(0), key.getName(), key.getAge())));
        return users;
    }

    @CacheRemove
    public void deleteByKey(Key key) {
        log.info("deleteByKey: key:{}", key);
    }

    @CacheRemoveAll
    public void deleteUsers(Set<Key> ids) {
        log.info("deleteUsers:{}", ids);
    }

    @CacheClear
    public void deleteAllUsers() {
        log.info("deleteAllUsers");
    }

}
