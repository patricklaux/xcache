package com.igeeksky.xcache.test.annotation;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import com.igeeksky.xcache.test.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 测试之前需先启动 redis
 *
 * @author patrick
 * @since 0.0.4 2024/5/5
 */
@SpringBootTest
public class CacheAnnotationTest {

    @Resource
    private UserService userService;

    @AfterAll
    public static void afterAll() throws InterruptedException {
        Thread.sleep(5000);
    }

    /**
     * unless 为 false，缓存数据
     */
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
    public void cacheableCondition() {
        Key jack02 = new Key("jack02");
        User userJack02 = new User("0", jack02.getName(), jack02.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack02);

        // 第一次调用时, condition 为 true，调用方法，并缓存数据
        User result0 = userService.getUserByKeyCondition(jack02, 0);
        System.out.println("0: " + result0);
        Assertions.assertEquals(userJack02, result0);

        // 第二次调用时, condition 为 true，不调用方法，读取缓存
        User result1 = userService.getUserByKeyCondition(jack02, 1);
        System.out.println("1: " + result1);
        Assertions.assertEquals(userJack02, result1);

        // 第三次调用时, condition 为 false，调用方法，不读取缓存
        User result2 = userService.getUserByKeyCondition(jack02, 2);
        System.out.println("2: " + result2);
        userJack02.setId("2");
        Assertions.assertEquals(userJack02, result2);
    }

    @Test
    public void cacheableOptional() {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 第一次调用，调用方法，并缓存数据
        Optional<User> result0 = userService.getOptionalUser(jack03, 0);
        System.out.println("0: " + result0);
        Assertions.assertEquals(userJack03, result0.orElse(null));

        Optional<User> result1 = userService.getOptionalUser(jack03, 0);
        System.out.println("0: " + result1);
        Assertions.assertEquals(userJack03, result1.orElse(null));
    }

    @Test
    public void cacheableOptionalLoop() {
        for(int i = 0; i < 10; i++){
            cacheableOptional();
        }
    }

    @Test
    public void cacheableFuture() throws ExecutionException, InterruptedException {
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
    public void cacheableNullFuture() throws ExecutionException, InterruptedException {
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
    public void cachePut1() {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        userService.saveUser(jack03, userJack03);

        // 只读取缓存，判断是否保存成功
        CacheValue<User> result2 = userService.getUserByCache(jack03);
        System.out.println(result2);
        Assertions.assertEquals(userJack03, result2.getValue());
    }

    @Test
    public void cacheableAll() {
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

        System.out.println("保存 2个元素--------------------------");
        userService.saveUser(jack04, userJack04);
        userService.saveUser(jack05, userJack05);

        System.out.println("缓存读取 2个元素--------------------------");

        // 缓存读取 2个元素，调用方法获取剩下的 2个元素，并将 2个元素保存到缓存
        Map<Key, User> result1 = userService.getUserList(new HashSet<>(keyValues.keySet()), 0);

        result1.forEach((key, user) -> {
            System.out.println(user);
            Assertions.assertEquals(keyValues.get(key), user);
        });

        System.out.println("缓存读取全部 4个元素--------------------------");

        // 缓存读取全部 4个元素
        Map<Key, User> result2 = userService.getUserList(new HashSet<>(keyValues.keySet()), 2);
        result2.forEach((key, user) -> {
            System.out.println(user);
            Assertions.assertEquals(keyValues.get(key), user);
        });
    }

    @Test
    public void cachePutAll1() {
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

        // 缓存元素
        userService.saveUsers(users);

        // 读取缓存，并对比元素
        Assertions.assertEquals(userJack08, userService.getUserByCache(jack08).getValue());
        Assertions.assertEquals(userJack09, userService.getUserByCache(jack09).getValue());
    }

    @Test
    public void cacheEvictAll1() {
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
    public void cacheEvict1() {
        Key jack12 = new Key("jack12");
        User userJack12 = new User("0", jack12.getName(), jack12.getAge());
        userService.saveUser(jack12, userJack12);

        Assertions.assertEquals(userJack12, userService.getUserByCache(jack12).getValue());

        userService.deleteByKey(jack12);

        Assertions.assertNull(userService.getUserByCache(jack12));
    }

    @Test
    public void cacheClear() {
        Key jack13 = new Key("jack13");
        User userJack13 = new User("0", jack13.getName(), jack13.getAge());
        userService.saveUser(jack13, userJack13);

        Assertions.assertEquals(userJack13, userService.getUserByCache(jack13).getValue());

        userService.deleteAllUsers();

        Assertions.assertNull(userService.getUserByCache(jack13));
    }

}