package com.igeeksky.xcache.test;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 注解测试
 * <p>
 * 测试之前需先启动 redis
 *
 * @author patrick
 * @since 0.0.4 2024/5/5
 */
@SpringBootTest
public class CacheAnnotationTest {

    private static final Logger log = LoggerFactory.getLogger(CacheAnnotationTest.class);

    private final UserService userService;

    @Autowired
    public CacheAnnotationTest(UserService userService) {
        this.userService = userService;
    }

    @AfterAll
    public static void afterAll() throws InterruptedException {
        Thread.sleep(1);
    }

    @Test
    public void cache_put_future_result() {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        userService.saveUserFuture(jack03, userJack03);

        // 读取缓存，判断是否保存成功
        CacheValue<User> cacheValue = userService.getUserByCache(jack03);
        Assertions.assertEquals(userJack03, cacheValue.getValue());
    }

    @Test
    public void cache_put_future_null_result() {
        Key jack03 = new Key("jack03");

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        CompletableFuture<User> future = userService.saveUserFuture(jack03, null);
        Assertions.assertNotNull(future);

        // 读取缓存，判断是否保存成功
        CacheValue<User> cacheValue = userService.getUserByCache(jack03);
        Assertions.assertNotNull(cacheValue);
        Assertions.assertNull(cacheValue.getValue());
    }

    @Test
    public void cache_put_null_future() {
        Key jack03 = new Key("jack03");

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        CompletableFuture<User> future = userService.saveUserNullFuture(jack03, null);
        Assertions.assertNull(future);

        // 读取缓存，判断是否保存成功
        CacheValue<User> cacheValue = userService.getUserByCache(jack03);
        Assertions.assertNull(cacheValue.getValue());
    }

    @Test
    public void cache_put_optional_result() {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        Optional<User> optional = userService.saveUserOptional(jack03, userJack03);
        Assertions.assertNotNull(optional);

        // 读取缓存，判断是否保存成功
        CacheValue<User> cacheValue = userService.getUserByCache(jack03);
        Assertions.assertEquals(userJack03, cacheValue.getValue());
    }

    @Test
    public void cache_put_optional_null_result() {
        Key jack03 = new Key("jack03");

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        Optional<User> optional = userService.saveUserOptional(jack03, null);
        Assertions.assertNotNull(optional);

        // 读取缓存，判断是否保存成功
        CacheValue<User> cacheValue = userService.getUserByCache(jack03);
        Assertions.assertNotNull(cacheValue);
        Assertions.assertNull(cacheValue.getValue());
    }

    @Test
    public void cache_put_null_optional() {
        Key jack03 = new Key("jack03");

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        Optional<User> future = userService.saveUserNullOptional(jack03, null);
        Assertions.assertNull(future);

        // 读取缓存，判断是否保存成功
        CacheValue<User> cacheValue = userService.getUserByCache(jack03);
        Assertions.assertNull(cacheValue.getValue());
    }

    @Test
    public void cache_put_method_result() {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        userService.saveUser(jack03, userJack03);

        // 读取缓存，判断是否保存成功
        CacheValue<User> cacheValue = userService.getUserByCache(jack03);
        Assertions.assertEquals(userJack03, cacheValue.getValue());
    }

    @Test
    public void cache_put_method_result_0() {
        Key key = new Key("test-result");
        User methodResult = new User("1", "MethodResult", 18);

        // 删除缓存元素
        userService.deleteUserByCache(key);

        // 调用方法，缓存元素
        User result1 = userService.saveCacheMethodResult(key);
        Assertions.assertEquals(methodResult, result1);

        // 读取缓存，缓存中保存的是方法返回值，即 methodResult
        CacheValue<User> cacheValue = userService.getUserByCache(key);
        log.info("cache_put_method_result_0: {}", cacheValue.getValue());
        Assertions.assertEquals(methodResult, cacheValue.getValue());
    }

    @Test
    public void cache_put_method_result_1() {
        Key key = new Key("test-result");
        User paramsResult = new User("0", "ParamsResult", key.getAge());
        User methodResult = new User("1", "MethodResult", 18);

        // 删除缓存元素
        userService.deleteUserByCache(key);

        // 调用方法，缓存元素
        User result1 = userService.saveCacheMethodResult(key, paramsResult);
        Assertions.assertEquals(methodResult, result1);

        // 读取缓存，缓存中保存的是通过 "#result" 获取的方法返回值，即 methodResult
        CacheValue<User> cacheValue = userService.getUserByCache(key);
        log.info("cache_put_method_result_1: {}", cacheValue.getValue());
        Assertions.assertEquals(methodResult, cacheValue.getValue());
    }

    @Test
    public void cache_put_params_result() {
        Key key = new Key("test-result");
        User paramsResult = new User("0", "ParamsResult", key.getAge());
        User methodResult = new User("1", "MethodResult", 18);

        // 删除缓存元素
        userService.deleteUserByCache(key);

        // 调用方法，缓存元素（方法返回值为 methodResult）
        User user = userService.saveCacheParamsResult(key, paramsResult);
        Assertions.assertEquals(methodResult, user);

        // 读取缓存，缓存中保存的是通过 "#p1" 获取的方法参数值，即 paramsResult
        CacheValue<User> cacheValue = userService.getUserByCache(key);
        log.info("cache_put_params_result: {}", cacheValue.getValue());
        Assertions.assertEquals(paramsResult, cacheValue.getValue());
    }

    @Test
    public void cache_put_eval_key_value_0() {
        Key key = new Key("cache_put_eval_key_value_0");
        User paramsResult = new User("0", "ParamsResult", key.getAge());
        User methodResult = new User("1", "MethodResult", 18);

        // 删除缓存元素
        userService.deleteUserByCache(key);

        // 调用方法，缓存元素
        User result1 = userService.saveByEvalKeyValue(paramsResult, key);
        Assertions.assertEquals(methodResult, result1);

        // 读取缓存，缓存中保存的是通过 "#value" 获取的方法参数值，即 methodResult
        CacheValue<User> cacheValue = userService.getUserByCache(key);
        log.info("cache_put_eval_key_value_0: {}", cacheValue.getValue());
        Assertions.assertEquals(paramsResult, cacheValue.getValue());
    }

    @Test
    public void cache_put_eval_condition() {
        Key key1 = new Key("cache_put_eval_condition_k1");
        Key key2 = new Key("cache_put_eval_condition_k2");
        User user1 = new User("0", "ParamsResult", 18);
        User user2 = new User("0", "ParamsResult", 19);

        // 删除缓存元素
        userService.deleteUserByCache(key1);
        userService.deleteUserByCache(key2);

        // 调用方法，年龄大于 18 的条件判断为 false，不缓存
        User result1 = userService.saveByEvalCondition(key1, user1);
        Assertions.assertEquals(user1, result1);

        // 调用方法，年龄大于 18 的条件判断为 true，缓存
        User result2 = userService.saveByEvalCondition(key2, user2);
        Assertions.assertEquals(user2, result2);

        // 读取缓存，值不存在
        CacheValue<User> cacheValue1 = userService.getUserByCache(key1);
        log.info("cache_put_eval_condition:cacheValue1: {}", cacheValue1);
        Assertions.assertNull(cacheValue1);

        // 读取缓存，值已存在
        CacheValue<User> cacheValue2 = userService.getUserByCache(key2);
        log.info("cache_put_eval_condition:cacheValue2: {}", cacheValue2);
        Assertions.assertEquals(user2, cacheValue2.getValue());
    }

    @Test
    public void cache_put_eval_unless() {
        Key key1 = new Key("cache_put_eval_unless_k1");
        Key key2 = new Key("cache_put_eval_unless_k2");
        User user1 = new User("0", "ParamsResult", 18);
        User user2 = new User("0", "ParamsResult", 19);

        // 删除缓存元素
        userService.deleteUserByCache(key1);
        userService.deleteUserByCache(key2);

        // 调用方法，年龄大于 18 的条件判断为 false，缓存
        User result1 = userService.saveByUnless(key1, user1);
        Assertions.assertEquals(user1, result1);

        // 调用方法，年龄大于 18 的条件判断为 true，不缓存
        User result2 = userService.saveByUnless(key2, user2);
        Assertions.assertEquals(user2, result2);

        // 读取缓存，值存在
        CacheValue<User> cacheValue1 = userService.getUserByCache(key1);
        log.info("cache_put_eval_unless:cacheValue1: {}", cacheValue1);
        Assertions.assertEquals(user1, cacheValue1.getValue());

        // 读取缓存，值不存在
        CacheValue<User> cacheValue2 = userService.getUserByCache(key2);
        log.info("cache_put_eval_unless:cacheValue2: {}", cacheValue2);
        Assertions.assertNull(cacheValue2);
    }

    @Test
    public void cache_put_all_method_result() {
        Key jack08 = new Key("jack08");
        Key jack09 = new Key("jack09");

        User userJack08 = new User("0", jack08.getName(), jack08.getAge());
        User userJack09 = new User("0", jack09.getName(), jack09.getAge());

        Map<Key, User> users = new HashMap<>();
        users.put(jack08, userJack08);
        users.put(jack09, userJack09);

        // 驱逐元素
        userService.deleteUserByCache(jack08);
        userService.deleteUserByCache(jack09);

        // 缓存方法返回值
        Map<Key, User> methodResult = userService.saveUsers(users);

        // 读取缓存，并对比元素
        Assertions.assertNotEquals(userJack08, userService.getUserByCache(jack08).getValue());
        Assertions.assertEquals(methodResult.get(jack08), userService.getUserByCache(jack08).getValue());

        Assertions.assertNotEquals(userJack09, userService.getUserByCache(jack09).getValue());
        Assertions.assertEquals(methodResult.get(jack09), userService.getUserByCache(jack09).getValue());
    }

    @Test
    public void cache_put_all_eval_keyValues() {
        Key jack08 = new Key("jack08");
        Key jack09 = new Key("jack09");

        User userJack08 = new User("0", jack08.getName(), 18);
        User userJack09 = new User("0", jack09.getName(), 18);

        Map<Key, User> users = new HashMap<>();
        users.put(jack08, userJack08);
        users.put(jack09, userJack09);

        // 驱逐元素
        userService.deleteUserByCache(jack08);
        userService.deleteUserByCache(jack09);

        // 缓存方法返回值
        Map<Key, User> methodResult = userService.saveUsersByEvalKeyValues(users);

        // 读取缓存，并对比元素
        Assertions.assertEquals(userJack08, userService.getUserByCache(jack08).getValue());
        Assertions.assertNotEquals(methodResult.get(jack08), userService.getUserByCache(jack08).getValue());

        Assertions.assertEquals(userJack09, userService.getUserByCache(jack09).getValue());
        Assertions.assertNotEquals(methodResult.get(jack09), userService.getUserByCache(jack09).getValue());
    }

    @Test
    public void cache_put_all_by_condition() {
        Key key1 = new Key("cache_put_all_by_condition1");
        Key key2 = new Key("cache_put_all_by_condition2");

        User user1 = new User("0", key1.getName(), 18);
        User user2 = new User("0", key2.getName(), 18);

        Map<Key, User> users = new HashMap<>();
        users.put(key1, user1);
        users.put(key2, user2);

        // 驱逐元素
        userService.deleteUserByCache(key1);
        userService.deleteUserByCache(key2);

        // condition 为 false，不缓存数据
        userService.saveUsersByCondition(users);

        // 读取缓存，值不存在
        CacheValue<User> cacheValue1 = userService.getUserByCache(key1);
        Assertions.assertNull(cacheValue1);
        CacheValue<User> cacheValue2 = userService.getUserByCache(key2);
        Assertions.assertNull(cacheValue2);

        // 修改年龄，满足 condition，缓存数据
        user1.setAge(19);
        user2.setAge(19);

        // condition 为 true，缓存数据
        Map<Key, User> methodResult = userService.saveUsersByCondition(users);

        // 读取缓存，值存在
        cacheValue1 = userService.getUserByCache(key1);
        cacheValue2 = userService.getUserByCache(key2);
        Assertions.assertNotNull(cacheValue1);
        Assertions.assertNotNull(cacheValue2);

        Assertions.assertEquals(methodResult.get(key1), cacheValue1.getValue());
        Assertions.assertEquals(methodResult.get(key2), cacheValue2.getValue());
    }

    @Test
    public void cache_put_all_by_unless() {
        Key key1 = new Key("cache_put_all_by_unless1");
        Key key2 = new Key("cache_put_all_by_unless2");

        User user1 = new User("0", key1.getName(), 18);
        User user2 = new User("0", key2.getName(), 18);

        Map<Key, User> users = new HashMap<>();
        users.put(key1, user1);
        users.put(key2, user2);

        // 驱逐元素
        userService.deleteUserByCache(key1);
        userService.deleteUserByCache(key2);

        // unless 为 false，缓存数据
        Map<Key, User> methodResult = userService.saveUsersByUnless(users);

        // 读取缓存，值存在
        CacheValue<User> cacheValue1 = userService.getUserByCache(key1);
        CacheValue<User> cacheValue2 = userService.getUserByCache(key2);

        Assertions.assertNotNull(cacheValue1);
        Assertions.assertNotNull(cacheValue2);

        Assertions.assertEquals(methodResult.get(key1), cacheValue1.getValue());
        Assertions.assertEquals(methodResult.get(key2), cacheValue2.getValue());

        // 删除已缓存数据
        userService.deleteUserByCache(key1);
        userService.deleteUserByCache(key2);

        // 修改年龄
        user1.setAge(19);
        user2.setAge(19);

        // unless 为 true，不缓存数据
        userService.saveUsersByUnless(users);

        // 读取缓存，值不存在
        cacheValue1 = userService.getUserByCache(key1);
        cacheValue2 = userService.getUserByCache(key2);

        Assertions.assertNull(cacheValue1);
        Assertions.assertNull(cacheValue2);
    }

    @Test
    public void cacheable() {
        Key jack01 = new Key("jack01");
        User userJack01 = new User("2", jack01.getName(), jack01.getAge());

        // 1. 删除缓存元素
        userService.deleteUserByCache(jack01);

        // 2. 第一次：调用方法，并缓存元素
        User result1 = userService.getUser(jack01, 2);
        Assertions.assertEquals(userJack01, result1);

        // 3. 第二次：不调用方法，读取缓存
        User result2 = userService.getUser(jack01, 3);
        Assertions.assertEquals(userJack01, result2);
    }

    @Test
    public void cacheable_eval_key() {
        Key key1 = new Key("key1");
        Key key2 = new Key("key2");
        User user = new User("2", key2.getName(), key2.getAge());

        // 1. 删除缓存元素
        userService.deleteUserByCache(key1);
        userService.deleteUserByCache(key2);

        // 2. 调用方法，缓存元素
        User result = userService.getUserByEvalKey(key1, key2);
        log.info("cacheable_eval_key:result: {}", result);
        Assertions.assertEquals(user, result);

        // 3. key1 获取的数据为空，key2 获取的数据不为空，说明 表达式 #key2 生效
        CacheValue<User> cacheValue1 = userService.getUserByCache(key1);
        Assertions.assertNull(cacheValue1);

        CacheValue<User> cacheValue2 = userService.getUserByCache(key2);
        Assertions.assertNotNull(cacheValue2);
        Assertions.assertEquals(user, cacheValue2.getValue());
    }

    @Test
    public void cacheable_by_condition() {
        Key jack02 = new Key("jack02");
        User userJack02 = new User("0", jack02.getName(), jack02.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack02);

        // 第一次调用时, condition 为 true，调用方法，并缓存数据
        User result0 = userService.getUserByCondition(jack02, 0);
        log.info("result0: {}", result0);
        Assertions.assertEquals(userJack02, result0);

        // 第二次调用时, condition 为 true，不调用方法，读取缓存
        User result1 = userService.getUserByCondition(jack02, 1);
        log.info("result1: {}", result1);
        Assertions.assertEquals(userJack02, result1);

        // 第三次调用时, condition 为 false，调用方法，不读取缓存
        User result2 = userService.getUserByCondition(jack02, 2);
        log.info("result2: {}", result2);
        Assertions.assertNotEquals(userJack02, result2);
    }

    @Test
    public void cacheable_optional_loop() {
        for (int i = 0; i < 10; i++) {
            cacheable_optional();
        }
    }

    @Test
    public void cacheable_optional() {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 第一次调用，调用方法，并缓存数据
        Optional<User> result0 = userService.getUserOptional(jack03);
        log.info("cacheable_optional:result0: {}", result0);
        Assertions.assertEquals(userJack03, result0.orElse(null));

        Optional<User> result1 = userService.getUserOptional(jack03);
        log.info("cacheable_optional:result1: {}", result1);
        Assertions.assertEquals(userJack03, result1.orElse(null));
    }

    @Test
    public void cacheable_optional_null() {
        Key jack03 = new Key("jack03");

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 第一次调用，调用方法，并缓存数据
        Optional<User> result0 = userService.getUserOptionalNull(jack03);
        log.info("cacheable_optional_null:result0: {}", result0);
        Assertions.assertNotNull(result0);
        Assertions.assertNull(result0.orElse(null));

        CacheValue<User> result1 = userService.getUserByCache(jack03);
        log.info("cacheable_optional_null:result1: {}", result1);
        Assertions.assertNotNull(result1);
        Assertions.assertNull(result1.getValue());
    }

    @Test
    public void cacheable_null_optional() {
        Key key = new Key("cacheable_null_optional");

        // 删除缓存元素
        userService.deleteUserByCache(key);

        // 第一次调用，调用方法，并缓存数据
        Optional<User> result0 = userService.getUserNullOptional(key);
        log.info("cacheable_null_optional:result0: {}", result0);
        Assertions.assertNull(result0);

        CacheValue<User> result1 = userService.getUserByCache(key);
        log.info("cacheable_null_optional:result1: {}", result1);
        Assertions.assertNotNull(result1);
        Assertions.assertNull(result1.getValue());
    }

    @Test
    public void cacheable_future() throws ExecutionException, InterruptedException {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 第一次调用，调用方法，并缓存数据
        CompletableFuture<User> result0 = userService.getUserFuture(jack03, 0);
        System.out.println("0: " + result0);
        Assertions.assertEquals(userJack03, result0.get());

        CompletableFuture<User> result1 = userService.getUserFuture(jack03, 0);
        System.out.println("0: " + result1);
        Assertions.assertEquals(userJack03, result1.get());
    }

    @Test
    public void cacheable_future_null() throws ExecutionException, InterruptedException {
        Key key = new Key("cacheable_future_null");

        // 删除缓存元素
        userService.deleteUserByCache(key);

        // 第一次调用，调用方法，并缓存数据
        CompletableFuture<User> result0 = userService.getUserFutureNull(key);
        Assertions.assertNull(result0.get());

        CompletableFuture<User> result1 = userService.getUserFuture(key, 0);
        Assertions.assertNull(result1.get());

        CacheValue<User> cacheValue = userService.getUserByCache(key);
        Assertions.assertFalse(cacheValue.hasValue());
    }

    @Test
    public void cacheable_null_future() {
        Key key = new Key("cacheable_null_future");

        // 删除缓存元素
        userService.deleteUserByCache(key);

        // 第一次调用，调用方法，CompletableFuture 为 null
        CompletableFuture<User> future1 = userService.getUserNullFuture(key);
        Assertions.assertNull(future1);

        CompletableFuture<User> future2 = userService.getUserNullFuture(key);
        Assertions.assertNotNull(future2);

        CacheValue<User> cacheValue = userService.getUserByCache(key);
        Assertions.assertFalse(cacheValue.hasValue());
    }

    @Test
    public void cacheable_all() {
        Key jack04 = new Key("jack04");
        Key jack05 = new Key("jack05");
        Key jack06 = new Key("jack06");
        Key jack07 = new Key("jack07");

        User userJack04 = new User("1", jack04.getName(), jack04.getAge());
        User userJack05 = new User("1", jack05.getName(), jack05.getAge());
        User userJack06 = new User("0", jack06.getName(), jack06.getAge());
        User userJack07 = new User("0", jack07.getName(), jack07.getAge());

        Map<Key, User> keyValues = new HashMap<>();
        keyValues.put(jack04, userJack04);
        keyValues.put(jack05, userJack05);
        keyValues.put(jack06, userJack06);
        keyValues.put(jack07, userJack07);

        // 删除缓存元素
        keyValues.forEach((key, user) -> userService.deleteUserByCache(key));

        log.info("保存 2个元素--------------------------");
        userService.saveUser(jack04, userJack04);
        userService.saveUser(jack05, userJack05);

        log.info("第一次调用：缓存命中 2个元素（需调用方法）--------------------------");

        // 从缓存读取 2个元素，调用方法后创建 2个元素并缓存，最终有 4 个元素缓存
        Map<Key, User> result1 = userService.getUserList(new HashSet<>(keyValues.keySet()), 0);
        keyValues.forEach((key, user) -> Assertions.assertEquals(result1.get(key), user));

        // 验证缓存是否 4 个元素都存在
        keyValues.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));

        log.info("第二次调用：缓存命中全部 4个元素（无需调用方法）--------------------------");

        // times >=1 时，缓存已有全部元素，无需调用方法，调用方法会抛出异常
        Map<Key, User> result2 = userService.getUserList(new HashSet<>(keyValues.keySet()), 1);
        keyValues.forEach((key, user) -> Assertions.assertEquals(result2.get(key), user));
    }

    @Test
    public void cacheable_all_future() throws ExecutionException, InterruptedException {
        Map<Key, User> keysUsers = createKeyUserMap("cacheable_all_eval_keys", 17, 4);

        // 删除缓存元素
        keysUsers.forEach((key, user) -> userService.deleteUserByCache(key));

        // 第一次调用：调用方法，并缓存数据
        CompletableFuture<Map<Key, User>> future1 = userService.getUserListFuture(new HashSet<>(keysUsers.keySet()));
        Assertions.assertNotNull(future1.get());

        // 验证缓存是否 4 个元素都存在
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));

        // 第一次调用：无需调用方法
        CompletableFuture<Map<Key, User>> future2 = userService.getUserListFuture(new HashSet<>(keysUsers.keySet()));
        Assertions.assertNotNull(future2.get());
    }

    @Test
    public void cacheable_all_future_null() throws ExecutionException, InterruptedException {
        Map<Key, User> keysUsers = createKeyUserMap("cacheable_all_future_null", 17, 4);

        // 删除缓存元素
        keysUsers.forEach((key, user) -> userService.deleteUserByCache(key));

        // 第一次调用：调用方法，并缓存空值
        CompletableFuture<Map<Key, User>> future1 = userService.getUserListFutureNull(new HashSet<>(keysUsers.keySet()));
        Assertions.assertNull(future1.get());

        // 验证缓存是否 4 个元素都存在
        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key).getValue()));

        // 第二次调用：无需调用方法，返回缓存的空值
        CompletableFuture<Map<Key, User>> future2 = userService.getUserListFutureNull(new HashSet<>(keysUsers.keySet()));
        Map<Key, User> map = future2.get();
        Assertions.assertNotNull(map);
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void cacheable_all_null_future() throws ExecutionException, InterruptedException {
        Map<Key, User> keysUsers = createKeyUserMap("cacheable_all_null_future", 17, 4);

        // 删除缓存元素
        keysUsers.forEach((key, user) -> userService.deleteUserByCache(key));

        // 第一次调用：调用方法，并缓存空值
        CompletableFuture<Map<Key, User>> future1 = userService.getUserListNullFuture(new HashSet<>(keysUsers.keySet()));
        Assertions.assertNull(future1);

        // 验证缓存是否 4 个元素都不存在
        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key).getValue()));

        // 第二次调用：无需调用方法，返回缓存的空值
        CompletableFuture<Map<Key, User>> future2 = userService.getUserListNullFuture(new HashSet<>(keysUsers.keySet()));
        Map<Key, User> map = future2.get();
        Assertions.assertNotNull(map);
        Assertions.assertTrue(map.isEmpty());
    }


    @Test
    public void cacheable_all_optional() {
        Map<Key, User> keysUsers = createKeyUserMap("cacheable_all_eval_keys", 17, 4);

        // 删除缓存元素
        keysUsers.forEach((key, user) -> userService.deleteUserByCache(key));

        // 第一次调用：调用方法，并缓存数据
        Optional<Map<Key, User>> optional1 = userService.getUserListOptional(new HashSet<>(keysUsers.keySet()));
        Assertions.assertNotNull(optional1.orElse(null));

        // 验证缓存是否 4 个元素都存在
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));

        // 第一次调用：无需调用方法
        Optional<Map<Key, User>> optional2 = userService.getUserListOptional(new HashSet<>(keysUsers.keySet()));
        Assertions.assertNotNull(optional2.orElse(null));
    }

    @Test
    public void cacheable_all_optional_null() {
        Map<Key, User> keysUsers = createKeyUserMap("cacheable_all_future_null", 17, 4);

        // 删除缓存元素
        keysUsers.forEach((key, user) -> userService.deleteUserByCache(key));

        // 第一次调用：调用方法，并缓存空值
        Optional<Map<Key, User>> optional1 = userService.getUserListOptionalNull(new HashSet<>(keysUsers.keySet()));
        Assertions.assertNull(optional1.orElse(null));

        // 验证缓存是否 4 个元素都存在
        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key).getValue()));

        // 第二次调用：无需调用方法，返回缓存的空值
        Optional<Map<Key, User>> optional2 = userService.getUserListOptionalNull(new HashSet<>(keysUsers.keySet()));
        Map<Key, User> map = optional2.orElse(null);
        Assertions.assertNotNull(map);
        Assertions.assertTrue(map.isEmpty());
    }

    @Test
    public void cacheable_all_null_optional() {
        Map<Key, User> keysUsers = createKeyUserMap("cacheable_all_null_future", 17, 4);

        // 删除缓存元素
        keysUsers.forEach((key, user) -> userService.deleteUserByCache(key));

        // 第一次调用：调用方法，并缓存空值，Optional 为空
        Optional<Map<Key, User>> optional1 = userService.getUserListNullOptional(new HashSet<>(keysUsers.keySet()));
        Assertions.assertNull(optional1);

        // 验证缓存是否 4 个元素都不存在
        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key).getValue()));

        // 第二次调用：无需调用方法，Optional 不为空
        Optional<Map<Key, User>> optional2 = userService.getUserListNullOptional(new HashSet<>(keysUsers.keySet()));
        Map<Key, User> map = optional2.orElse(null);
        Assertions.assertNotNull(map);
        Assertions.assertTrue(map.isEmpty());
    }

    /**
     * 注意：⭐⭐⭐当方法创建的值集与传入缓存的键集不匹配时，方法返回值是不确定的。
     */
    @Test
    public void cacheable_all_eval_keys() {
        Map<Key, User> keysUsers = createKeyUserMap("cacheable_all_eval_keys", 17, 4);

        // 删除缓存元素
        keysUsers.forEach((key, user) -> userService.deleteUserByCache(key));

        // ⭐⭐⭐ 第一次调用，缓存未命中，会执行方法，返回 4 个元素
        // 因为传入缓存的键集是 2 个，所以只会缓存 2 个元素，后续查询缓存时也只会找到 2 个元素
        Map<Key, User> methodResult1 = userService.getUserListByEvalKeys(new HashSet<>(keysUsers.keySet()));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(methodResult1.get(key), user));

        // ⭐⭐⭐ 第二次调用，缓存全命中，不执行方法，只会返回缓存中的 2 个元素
        Map<Key, User> methodResult2 = userService.getUserListByEvalKeys(new HashSet<>(keysUsers.keySet()));
        keysUsers.forEach((key, user) -> {
            if (user.getAge() > 18) {
                Assertions.assertEquals(user, methodResult2.get(key));
            } else {
                Assertions.assertNull(methodResult2.get(key));
            }
        });
    }

    @Test
    public void cacheable_all_condition() {
        log.info("数据集一：缓存条件为 true：condition = \"#keys.?[getAge > 18].size > 0\"");
        Map<Key, User> keyValues = createKeyUserMap("cacheable_all_1", 18, 5);
        // 删除缓存元素
        keyValues.forEach((key, user) -> userService.deleteUserByCache(key));
        // 第一次调用：包含 age > 18 的元素，condition 为 true，缓存数据
        Map<Key, User> result1 = userService.getUserListByCondition(new HashSet<>(keyValues.keySet()));
        keyValues.forEach((key, user) -> Assertions.assertEquals(user, result1.get(key)));
        // 缓存验证：是否 5 个元素都已缓存
        keyValues.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        // 第二次调用：判断有缓存和无缓存是否结果一致
        Map<Key, User> result2 = userService.getUserListByCondition(new HashSet<>(keyValues.keySet()));
        keyValues.forEach((key, user) -> Assertions.assertEquals(user, result2.get(key)));

        log.info("数据集二：缓存条件为 false：condition = \"#keys.?[getAge > 18].size > 0\"");
        Map<Key, User> keyValues2 = createKeyUserMap("cacheable_all_2", 16, 3);
        // 删除缓存元素
        keyValues2.forEach((key, user) -> userService.deleteUserByCache(key));
        // 第一次调用：不包含 age > 18 的元素，condition 为 false，不缓存数据
        Map<Key, User> result3 = userService.getUserListByCondition(keyValues2.keySet());
        keyValues2.forEach((key, user) -> Assertions.assertEquals(user, result3.get(key)));
        // 缓存验证：是否 3 个元素都未缓存
        keyValues2.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
        // 第二次调用：判断有缓存和无缓存是否结果一致
        Map<Key, User> result4 = userService.getUserListByCondition(keyValues2.keySet());
        keyValues2.forEach((key, user) -> Assertions.assertEquals(user, result4.get(key)));
    }

    @Test
    public void cache_remove() {
        Key key = new Key("cache_remove1");
        User user = new User("0", key.getName(), key.getAge());
        userService.saveUser(key, user);

        Assertions.assertEquals(user, userService.getUserByCache(key).getValue());

        userService.deleteByKey(key);

        Assertions.assertNull(userService.getUserByCache(key));
    }

    @Test
    public void cache_remove1() {
        Key key = new Key("cache_remove1");
        User user = new User("0", key.getName(), key.getAge());
        userService.saveUser(key, user);

        Assertions.assertEquals(user, userService.getUserByCache(key).getValue());

        userService.deleteByEvalKey(1, key);

        Assertions.assertNull(userService.getUserByCache(key));
    }

    @Test
    public void cache_remove2() {
        Key key1 = new Key(18, "cache_remove2");
        User user = new User("0", key1.getName(), key1.getAge());
        userService.saveUser(key1, user);
        Assertions.assertEquals(user, userService.getUserByCache(key1).getValue());

        // 未删除，condition age > 18 为 false，不删除
        userService.deleteByEvalCondition(key1);
        Assertions.assertEquals(user, userService.getUserByCache(key1).getValue());

        Key key2 = new Key(19, "cache_remove2");
        userService.saveUser(key2, user);
        Assertions.assertEquals(user, userService.getUserByCache(key2).getValue());

        // 已删除，condition age > 18 为 true，删除
        userService.deleteByEvalCondition(key2);
        Assertions.assertNull(userService.getUserByCache(key2));
    }

    @Test
    public void cache_remove_before_invocation() {
        Key key1 = new Key(18, "delete_before_invocation");
        User user = new User("0", key1.getName(), key1.getAge());
        userService.saveUser(key1, user);
        Assertions.assertEquals(user, userService.getUserByCache(key1).getValue());

        // ⭐⭐⭐ 方法调用前删除缓存，unless 表达式无效
        userService.deleteBeforeInvocation(key1);
        Assertions.assertNull(userService.getUserByCache(key1));
    }

    @Test
    public void cache_remove_after_invocation() {
        Key key1 = new Key(18, "cache_remove_after_invocation");
        User user = new User("0", key1.getName(), key1.getAge());
        userService.saveUser(key1, user);
        Assertions.assertEquals(user, userService.getUserByCache(key1).getValue());

        // 方法调用后删除缓存，unless 表达式有效
        userService.deleteAfterInvocation(key1);
        Assertions.assertNull(userService.getUserByCache(key1));
    }

    @Test
    public void cache_remove_eval_unless() {
        Key key1 = new Key(18, "cache_remove_after_invocation");
        User user = new User("0", key1.getName(), key1.getAge());
        userService.saveUser(key1, user);
        Assertions.assertEquals(user, userService.getUserByCache(key1).getValue());

        // unless 为 false，删除缓存元素
        userService.deleteEvalUnless(key1);
        Assertions.assertNull(userService.getUserByCache(key1));

        Key key2 = new Key(19, "cache_remove_after_invocation");
        User user2 = new User("0", key1.getName(), key1.getAge());
        userService.saveUser(key2, user2);
        Assertions.assertEquals(user2, userService.getUserByCache(key2).getValue());

        // unless 为 true，未删除缓存元素
        userService.deleteEvalUnless(key2);
        Assertions.assertNotNull(userService.getUserByCache(key2));
    }

    @Test
    public void cache_remove_condition_unless() {
        Key key1 = new Key(18, "cache_remove_after_invocation");
        User user = new User("0", key1.getName(), key1.getAge());
        User saved = userService.saveUser(key1, user);
        Assertions.assertEquals(saved, userService.getUserByCache(key1).getValue());
        Assertions.assertEquals(user, userService.getUserByCache(key1).getValue());

        // unless 为 false，删除缓存元素
        userService.deleteEvalConditionUnless(key1);
        Assertions.assertNull(userService.getUserByCache(key1));

        Key key2 = new Key(19, "cache_remove_after_invocation");
        User user2 = new User("0", key1.getName(), key1.getAge());
        userService.saveUser(key2, user2);
        Assertions.assertEquals(user2, userService.getUserByCache(key2).getValue());

        // unless 为 true，未删除缓存元素
        userService.deleteEvalConditionUnless(key2);
        Assertions.assertNotNull(userService.getUserByCache(key2));
    }


    @Test
    public void cache_remove_condition_unless2() {
        Key key1 = new Key(18, "cache_remove_condition_unless2");
        User user = new User("0", key1.getName(), key1.getAge());
        userService.saveUser(key1, user);
        Assertions.assertEquals(user, userService.getUserByCache(key1).getValue());

        // condition 为 false，unless 表达式无意义，未删除缓存元素
        userService.deleteEvalConditionFalseUnless(key1);
        Assertions.assertEquals(user, userService.getUserByCache(key1).getValue());

        Key key2 = new Key(19, "cache_remove_after_invocation");
        User user2 = new User("0", key1.getName(), key1.getAge());
        userService.saveUser(key2, user2);
        Assertions.assertEquals(user2, userService.getUserByCache(key2).getValue());

        // condition 为 false，unless 表达式无意义，未删除缓存元素
        userService.deleteEvalConditionFalseUnless(key2);
        Assertions.assertEquals(user2, userService.getUserByCache(key2).getValue());
    }


    @Test
    public void cache_remove_all() {
        Map<Key, User> keysUsers = createKeyUserMap("cache_remove_all", 10, 2);

        userService.saveUsersToCache(new HashMap<>(keysUsers));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        userService.deleteUsers(new HashSet<>(keysUsers.keySet()));

        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_remove_all_eval_keys() {
        Map<Key, User> keysUsers = createKeyUserMap("cache_remove_all_eval_keys", 10, 2);

        userService.saveUsersToCache(new HashMap<>(keysUsers));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        userService.deleteUsersEvalKeys(1, new HashSet<>(keysUsers.keySet()));

        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_remove_all_before_invocation() {
        Map<Key, User> keysUsers = createKeyUserMap("cache_remove_all_before_invocation", 10, 2);

        userService.saveUsersToCache(new HashMap<>(keysUsers));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        userService.deleteUsersBeforeInvocation(new HashSet<>(keysUsers.keySet()));

        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_remove_all_after_invocation() {
        Map<Key, User> keysUsers = createKeyUserMap("cache_remove_all_after_invocation", 10, 2);

        userService.saveUsersToCache(new HashMap<>(keysUsers));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        userService.deleteUsersAfterInvocation(new HashSet<>(keysUsers.keySet()));

        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_remove_all_condition_unless_1() {
        Map<Key, User> keysUsers = createKeyUserMap("cache_remove_all_condition_unless_1", 10, 2);

        userService.saveUsersToCache(new HashMap<>(keysUsers));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        userService.deleteUsersConditionTrueUnlessFalse(new HashSet<>(keysUsers.keySet()));

        keysUsers.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_remove_all_condition_unless_2() {
        Map<Key, User> keysUsers = createKeyUserMap("cache_remove_all_condition_unless_2", 10, 2);

        userService.saveUsersToCache(new HashMap<>(keysUsers));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        userService.deleteUsersConditionTrueUnlessTrue(new HashSet<>(keysUsers.keySet()));

        keysUsers.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_remove_all_condition_unless_3() {
        Map<Key, User> keysUsers = createKeyUserMap("cache_remove_all_condition_unless_3", 10, 2);

        userService.saveUsersToCache(new HashMap<>(keysUsers));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        userService.deleteUsersConditionFalseUnlessFalse(new HashSet<>(keysUsers.keySet()));

        keysUsers.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_remove_all_condition_unless_4() {
        Map<Key, User> keysUsers = createKeyUserMap("cache_remove_all_condition_unless_3", 10, 2);

        userService.saveUsersToCache(new HashMap<>(keysUsers));
        keysUsers.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));
        userService.deleteUsersConditionFalseUnlessTrue(new HashSet<>(keysUsers.keySet()));

        keysUsers.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);

        users.forEach((key, user) -> Assertions.assertEquals(user, userService.getUserByCache(key).getValue()));

        userService.deleteAllUsers();

        users.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear_BeforeInvocation() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersBeforeInvocation();

        users.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear_AfterInvocation() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersAfterInvocation();

        users.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear_ConditionTrue() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersConditionTrue();

        users.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear_ConditionFalse() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersConditionFalse();

        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear_UnlessTrue() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersUnlessTrue();

        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }


    @Test
    public void cache_clear_UnlessFalse() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersUnlessFalse();

        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }


    @Test
    public void cache_clear_ConditionTrueUnlessFalse() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersConditionTrueUnlessFalse();

        users.forEach((key, user) -> Assertions.assertNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear_ConditionTrueUnlessTrue() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersConditionTrueUnlessTrue();

        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear_ConditionFalseUnlessFalse() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersConditionFalseUnlessFalse();

        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }

    @Test
    public void cache_clear_ConditionFalseUnlessTrue() {
        Map<Key, User> users = createKeyUserMap("cache_clear", 10, 5);
        users.forEach(userService::saveUser);
        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));

        userService.deleteAllUsersConditionFalseUnlessTrue();

        users.forEach((key, user) -> Assertions.assertNotNull(userService.getUserByCache(key)));
    }

    @Test
    public void otherTest() {
        Assertions.assertTrue(UserService.alwaysTrue());
        Assertions.assertFalse(UserService.alwaysFalse());
        Assertions.assertNotNull(UserService.newHashSet(List.of(new Key("s"))));
    }

    private static Map<Key, User> createKeyUserMap(String name, int age, int size) {
        Map<Key, User> map = HashMap.newHashMap(size);
        for (int i = 0; i < size; i++, age++) {
            Key key = new Key(age, name + age);
            User user = new User("0", key.getName(), key.getAge());
            map.put(key, user);
        }
        return map;
    }

}