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

import java.util.*;
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
        // log.info("getUserByCache: key: {}", key);
        return cache.getCacheValue(key);
    }

    public void saveUsersToCache(Map<Key, User> keyValues) {
        // log.info("saveUsersToCache: keyValues: {}", keyValues);
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

    @CachePut
    public CompletableFuture<User> saveUserFuture(Key key, User user) {
        log.info("saveUserFuture: key:{}, user:{}", key, user);
        return CompletableFuture.completedFuture(user);
    }

    /**
     * ⭐⭐⭐ 这是一个错误示例！⭐⭐⭐
     * <p>
     * 方法返回值的 {@link CompletableFuture} 不建议为 null！！
     * <p>
     * 当缓存未命中时，方法会返回 {@code null}；缓存命中时，缓存框架会返回 {@code CompletableFuture#completedFuture(null)}。
     */
    @CachePut
    public CompletableFuture<User> saveUserNullFuture(Key key, User user) {
        log.info("saveUserNullFuture: key:{}, user:{}", key, user);
        return null;
    }

    @CachePut
    public Optional<User> saveUserOptional(Key key, User user) {
        log.info("saveUserOptional: key:{}, user:{}", key, user);
        return Optional.ofNullable(user);
    }

    /**
     * ⭐⭐⭐ 这是一个错误示例！⭐⭐⭐
     * <p>
     * 方法返回值的 {@link Optional} 不建议为 null！！
     * <p>
     * 当缓存未命中时，方法会返回 {@code null}；缓存命中时，缓存框架会返回 {@link Optional#ofNullable(Object)}。
     */
    @CachePut
    public Optional<User> saveUserNullOptional(Key key, User user) {
        log.info("saveUserNullOptional: key:{}, user:{}", key, user);
        return null;
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
    public Optional<User> getUserOptional(Key key) {
        log.info("getUserOptional: key:{}", key);
        return Optional.of(new User("0", key.getName(), key.getAge()));
    }

    @Cacheable
    public Optional<User> getUserOptionalNull(Key key) {
        log.info("getUserOptionalNull: key:{}", key);
        return Optional.empty();
    }

    /**
     * ⭐⭐⭐ 这是一个错误示例！⭐⭐⭐
     * <p>
     * 方法返回值的 {@link Optional} 不建议为 null！！
     * <p>
     * 当缓存未命中时，方法会返回 {@code null}；缓存命中时，缓存框架会返回 {@code Optional#ofNullable(null)}。
     */
    @Cacheable
    public Optional<User> getUserNullOptional(Key key) {
        log.info("getUserNullOptional: key:{}", key);
        return null;
    }

    @Cacheable
    public CompletableFuture<User> getUserFuture(Key key, int times) {
        log.info("getUserFuture: key:{}, times:{}", key, times);
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
    public CompletableFuture<User> getUserFutureNull(Key key) {
        log.info("getUserFutureNull:{}", key);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * ⭐⭐⭐ 这是一个错误示例！⭐⭐⭐
     * <p>
     * 方法返回值的 {@link CompletableFuture} 不建议为 null！！
     * <p>
     * 当缓存未命中时，方法会返回 {@code null}；缓存命中时，缓存框架会返回 {@code CompletableFuture#completedFuture(null)}。
     */
    @Cacheable
    public CompletableFuture<User> getUserNullFuture(Key key) {
        log.info("getUserNullFuture:{}", key);
        return null;
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

    @CacheableAll
    public CompletableFuture<Map<Key, User>> getUserListFuture(Set<Key> keys) {
        log.info("getUserListFuture: keys:{}", keys);
        Map<Key, User> users = HashMap.newHashMap(keys.size());
        keys.forEach(key -> users.put(key, new User("0", key.getName(), key.getAge())));
        return CompletableFuture.completedFuture(users);
    }

    @CacheableAll
    public CompletableFuture<Map<Key, User>> getUserListFutureNull(Set<Key> keys) {
        log.info("getUserListFutureNull: keys:{}", keys);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * ⭐⭐⭐ 这是一个错误示例！⭐⭐⭐
     * <p>
     * 方法返回值的 {@link CompletableFuture} 不建议为 null！！
     * <p>
     * 当缓存未命中时，方法会返回 {@code null}；缓存命中时，缓存框架会返回 {@code CompletableFuture#completedFuture(Map)}。
     */
    @CacheableAll
    public CompletableFuture<Map<Key, User>> getUserListNullFuture(Set<Key> keys) {
        log.info("getUserListNullFuture: keys:{}", keys);
        return null;
    }

    @CacheableAll
    public Optional<Map<Key, User>> getUserListOptional(Set<Key> keys) {
        log.info("getUserListOptional: keys:{}", keys);
        Map<Key, User> users = HashMap.newHashMap(keys.size());
        keys.forEach(key -> users.put(key, new User("0", key.getName(), key.getAge())));
        return Optional.of(users);
    }

    @CacheableAll
    public Optional<Map<Key, User>> getUserListOptionalNull(Set<Key> keys) {
        log.info("getUserListOptionalNull: keys:{}", keys);
        return Optional.empty();
    }

    /**
     * ⭐⭐⭐ 这是一个错误示例！⭐⭐⭐
     * <p>
     * 方法返回值的 {@link Optional} 不建议为 null！！
     * <p>
     * 当缓存未命中时，方法会返回 {@code null}；缓存命中时，缓存框架会返回 {@code Optional#ofNullable(Map)}。
     */
    @CacheableAll
    public Optional<Map<Key, User>> getUserListNullOptional(Set<Key> keys) {
        log.info("getUserListNullOptional: keys:{}", keys);
        return null;
    }

    /**
     * ⭐⭐⭐ 这是一个错误示例！⭐⭐⭐
     * <p>
     * 实际传入缓存方法的键集是 age 大于 18 集合，是 keys 的子集，
     * 但方法创建的 User 是 keys 的全集。
     * <p>
     * 当缓存未命中时，方法会返回 keys 的全集对应的 User 集合。 <br>
     * 当缓存部分命中时，方法会返回 keys 的全集对应的 User 集合。<br>
     * 当缓存全部命中时，方法只会返回 age 大于 18 的 User 集合。
     * <p>
     * 很重要的原则：
     * 1.传入缓存的键集必须与传入被注解方法的键集完全一致。
     * 2.方法返回的值集必须与传入缓存的键集必须是一一对应。方法返回值中，缓存键对应的值可以为空，但不能创建多于传入缓存的键集的对象。
     * <p>
     * 总之，{@code keys} 表达式仅仅用来获取键集，千万不要加条件过滤！另，方法也不要凭空多创建对象。
     * 否则，方法的返回结果集将是不确定的！！！
     *
     * @param keys 键集
     * @return 键集对应的 User 集合
     */
    @CacheableAll(keys = "newHashSet(#keys.?[getAge > 18])")
    public Map<Key, User> getUserListByEvalKeys(Set<Key> keys) {
        log.info("getUserListByEvalKeys: keys:{}", keys);
        Map<Key, User> users = HashMap.newHashMap(keys.size());
        keys.forEach(key -> users.put(key, new User("0", key.getName(), key.getAge())));
        return users;
    }

    @CacheableAll(condition = "#keys.?[getAge > 18].size > 0")
    public Map<Key, User> getUserListByCondition(Set<Key> keys) {
        log.info("getUserListByCondition: keys:{}", keys);
        Map<Key, User> users = HashMap.newHashMap(keys.size());
        keys.forEach(key -> users.put(key, new User("0", key.getName(), key.getAge())));
        return users;
    }

    @CacheRemove
    public void deleteByKey(Key key) {
        log.info("deleteByKey: key:{}", key);
    }

    @CacheRemove(key = "#key1")
    public void deleteByEvalKey(int ignore, Key key1) {
        log.info("deleteByEvalKey: key1:{}", key1);
    }

    @CacheRemove(condition = "#key.getAge > 18")
    public void deleteByEvalCondition(Key key) {
        log.info("deleteByEvalCondition: key:{}", key);
    }

    @CacheRemove(beforeInvocation = true, unless = "alwaysFalse")
    public void deleteBeforeInvocation(Key key) {
        log.info("deleteBeforeInvocation: key:{}", key);
    }

    @CacheRemove(unless = "alwaysFalse")
    public void deleteAfterInvocation(Key key) {
        log.info("deleteAfterInvocation: key:{}", key);
    }

    @CacheRemove(unless = "#key.getAge > 18")
    public void deleteEvalUnless(Key key) {
        log.info("deleteEvalUnless: key:{}", key);
    }

    @CacheRemove(condition = "alwaysTrue", unless = "#key.getAge > 18")
    public void deleteEvalConditionUnless(Key key) {
        log.info("deleteEvalConditionUnless: key:{}", key);
    }

    @CacheRemove(condition = "alwaysFalse", unless = "#key.getAge > 18")
    public void deleteEvalConditionFalseUnless(Key key) {
        log.info("deleteEvalFalseConditionUnless: key:{}", key);
    }

    @CacheRemoveAll
    public void deleteUsers(Set<Key> ids) {
        log.info("deleteUsers:{}", ids);
    }

    @CacheRemoveAll(keys = "#ids")
    public void deleteUsersEvalKeys(int ignore, Set<Key> ids) {
        log.info("deleteUsersEvalKeys:{}", ids);
    }

    @CacheRemoveAll(beforeInvocation = true, unless = "alwaysFalse")
    public void deleteUsersBeforeInvocation(Set<Key> ids) {
        log.info("deleteUsersBeforeInvocation:{}", ids);
    }

    @CacheRemoveAll(unless = "alwaysFalse")
    public void deleteUsersAfterInvocation(Set<Key> ids) {
        log.info("deleteUsersAfterInvocation:{}", ids);
    }

    @CacheRemoveAll(condition = "alwaysTrue", unless = "alwaysFalse")
    public void deleteUsersConditionTrueUnlessFalse(Set<Key> ids) {
        log.info("deleteUsersConditionTrueUnlessFalse:{}", ids);
    }

    @CacheRemoveAll(condition = "alwaysTrue", unless = "alwaysTrue")
    public void deleteUsersConditionTrueUnlessTrue(Set<Key> ids) {
        log.info("deleteUsersConditionTrueUnlessTrue:{}", ids);
    }

    @CacheRemoveAll(condition = "alwaysFalse", unless = "alwaysFalse")
    public void deleteUsersConditionFalseUnlessFalse(Set<Key> ids) {
        log.info("deleteUsersConditionFalseUnlessFalse:{}", ids);
    }

    @CacheRemoveAll(condition = "alwaysFalse", unless = "alwaysTrue")
    public void deleteUsersConditionFalseUnlessTrue(Set<Key> ids) {
        log.info("deleteUsersConditionFalseUnlessTrue:{}", ids);
    }

    @CacheClear
    public void deleteAllUsers() {
        log.info("deleteAllUsers");
    }

    @CacheClear(beforeInvocation = true, unless = "alwaysFalse")
    public void deleteAllUsersBeforeInvocation() {
        log.info("deleteAllUsersBeforeInvocation");
    }

    @CacheClear(unless = "alwaysFalse")
    public void deleteAllUsersAfterInvocation() {
        log.info("deleteAllUsersAfterInvocation");
    }

    @CacheClear(condition = "alwaysTrue")
    public void deleteAllUsersConditionTrue() {
        log.info("deleteAllUsersConditionTrue");
    }

    @CacheClear(condition = "alwaysFalse")
    public void deleteAllUsersConditionFalse() {
        log.info("deleteAllUsersConditionFalse");
    }

    @CacheClear(unless = "alwaysTrue")
    public void deleteAllUsersUnlessTrue() {
        log.info("deleteAllUsersUnlessTrue");
    }

    @CacheClear(unless = "alwaysTrue")
    public void deleteAllUsersUnlessFalse() {
        log.info("deleteAllUsersUnlessFalse");
    }

    @CacheClear(condition = "alwaysTrue", unless = "alwaysFalse")
    public void deleteAllUsersConditionTrueUnlessFalse() {
        log.info("deleteAllUsersConditionTrueUnlessFalse");
    }

    @CacheClear(condition = "alwaysTrue", unless = "alwaysTrue")
    public void deleteAllUsersConditionTrueUnlessTrue() {
        log.info("deleteAllUsersConditionTrueUnlessTrue");
    }

    @CacheClear(condition = "alwaysFalse", unless = "alwaysFalse")
    public void deleteAllUsersConditionFalseUnlessFalse() {
        log.info("deleteAllUsersConditionFalseUnlessFalse");
    }

    @CacheClear(condition = "alwaysFalse", unless = "alwaysTrue")
    public void deleteAllUsersConditionFalseUnlessTrue() {
        log.info("deleteAllUsersConditionFalseUnlessTrue");
    }

    /**
     * 永远返回 true
     * <p>
     * 可通过日志判断 beforeInvocation 是否生效等情况
     *
     * @return true
     */
    public static boolean alwaysTrue() {
        log.info("alwaysTrue");
        return true;
    }

    /**
     * 永远返回 false
     * <p>
     * 可通过日志判断 beforeInvocation 是否生效等情况
     *
     * @return true
     */
    public static boolean alwaysFalse() {
        log.info("alwaysFalse");
        return false;
    }

    public static Set<Key> newHashSet(Collection<Key> keys) {
        return new HashSet<>(keys);
    }

}
