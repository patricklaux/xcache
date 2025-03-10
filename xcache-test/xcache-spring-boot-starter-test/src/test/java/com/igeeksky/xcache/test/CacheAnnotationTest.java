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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
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
        System.out.println(result1);
        Assertions.assertEquals(userJack01, result1);

        // 3. 第二次：不调用方法，读取缓存
        User result2 = userService.getUser(jack01, 3);
        System.out.println(result2);
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
        Optional<User> result0 = userService.getOptionalUser(jack03, 0);
        log.info("cacheable_optional:result0: {}", result0);
        Assertions.assertEquals(userJack03, result0.orElse(null));

        Optional<User> result1 = userService.getOptionalUser(jack03, 0);
        log.info("cacheable_optional:result1: {}", result1);
        Assertions.assertEquals(userJack03, result1.orElse(null));
    }

    @Test
    public void cacheable_future() throws ExecutionException, InterruptedException {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 第一次调用，调用方法，并缓存数据
        CompletableFuture<User> result0 = userService.getFutureUser(jack03, 0);
        System.out.println("0: " + result0);
        Assertions.assertEquals(userJack03, result0.get());

        CompletableFuture<User> result1 = userService.getFutureUser(jack03, 0);
        System.out.println("0: " + result1);
        Assertions.assertEquals(userJack03, result1.get());
    }

    @Test
    public void cacheable_null_future() throws ExecutionException, InterruptedException {
        Key jack03 = new Key("jack03");

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 第一次调用，调用方法，并缓存数据
        CompletableFuture<User> result0 = userService.getNullFutureUser(jack03);
        System.out.println("0: " + result0);
        Assertions.assertNull(result0.get());

        CompletableFuture<User> result1 = userService.getFutureUser(jack03, 0);
        System.out.println("0: " + result1);
        Assertions.assertNull(result1.get());

        CacheValue<User> cacheValue = userService.getUserByCache(jack03);
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

    /**
     * 注意：⭐⭐⭐⭐⭐当方法创建的值集与传入的键集不匹配时，方法返回值是不确定的。
     */
    @Test
    public void cacheable_all_eval_keys() {
        Key jack04 = new Key("jack04");
        Key jack05 = new Key("jack05");
        Key jack06 = new Key("jack06");
        Key jack07 = new Key("jack07");

        User userJack04 = new User("0", jack04.getName(), jack04.getAge());
        User userJack05 = new User("0", jack05.getName(), jack05.getAge());
        User userJack06 = new User("0", jack06.getName(), jack06.getAge());
        User userJack07 = new User("0", jack07.getName(), jack07.getAge());

        Map<Key, User> keyValues1 = new HashMap<>();
        Map<Key, User> keyValues2 = new HashMap<>();
        keyValues1.put(jack04, userJack04);
        keyValues1.put(jack05, userJack05);

        keyValues2.put(jack06, userJack06);
        keyValues2.put(jack07, userJack07);

        // 删除缓存元素
        keyValues1.forEach((key, user) -> userService.deleteUserByCache(key));
        keyValues2.forEach((key, user) -> userService.deleteUserByCache(key));

        // ⭐⭐⭐⭐⭐ 第一次调用，缓存未命中，执行方法，返回 4 个元素
        // 因为传入的缓存键集是 2 个，而方法返回值是 4 个，所以只会缓存 2 个元素，查询缓存时也只会找到 2 个元素
        Map<Key, User> result1 = userService.getUserListByEvalKeys(new HashSet<>(keyValues1.keySet()), keyValues2.keySet());
        keyValues1.forEach((key, user) -> Assertions.assertEquals(result1.get(key), user));
        keyValues2.forEach((key, user) -> Assertions.assertEquals(result1.get(key), user));

        // ⭐⭐⭐⭐⭐ 第二次调用，缓存全命中，不执行方法，返回 2 个元素
        Map<Key, User> result2 = userService.getUserListByEvalKeys(new HashSet<>(keyValues1.keySet()), keyValues2.keySet());
        keyValues1.forEach((key, user) -> Assertions.assertNull(result2.get(key)));
        keyValues2.forEach((key, user) -> Assertions.assertEquals(result2.get(key), user));
    }

    @Test
    public void cacheRemoveAll1() {
        Key jack10 = new Key("jack10");
        Key jack11 = new Key("jack11");

        User userJack10 = new User("0", jack10.getName(), jack10.getAge());
        User userJack11 = new User("0", jack11.getName(), jack11.getAge());

        Map<Key, User> keyValues = new HashMap<>();
        keyValues.put(jack10, userJack10);
        keyValues.put(jack11, userJack11);

        userService.saveUsersToCache(new HashMap<>(keyValues));

        Assertions.assertEquals(userJack10, userService.getUserByCache(jack10).getValue());
        Assertions.assertEquals(userJack11, userService.getUserByCache(jack11).getValue());

        userService.deleteUsers(new HashSet<>(keyValues.keySet()));

        Assertions.assertNull(userService.getUserByCache(jack10));
        Assertions.assertNull(userService.getUserByCache(jack11));
    }

    @Test
    public void cache_remove() {
        Key jack12 = new Key("jack12");
        User userJack12 = new User("0", jack12.getName(), jack12.getAge());
        userService.saveUser(jack12, userJack12);

        Assertions.assertEquals(userJack12, userService.getUserByCache(jack12).getValue());

        userService.deleteByKey(jack12);

        Assertions.assertNull(userService.getUserByCache(jack12));
    }

    @Test
    public void cache_clear() {
        Key jack13 = new Key("jack13");
        User userJack13 = new User("0", jack13.getName(), jack13.getAge());
        User user = userService.saveUser(jack13, userJack13);

        Assertions.assertNotNull(user);
        Assertions.assertEquals(user, userService.getUserByCache(jack13).getValue());

        userService.deleteAllUsers();

        Assertions.assertNull(userService.getUserByCache(jack13));
    }

}