package com.igeeksky.xcache.test;

import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 测试用户缓存服务
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/5
 */
@SpringBootTest
class SpringUserServiceTest {

    @Resource
    private SpringUserService userService;

    @AfterAll
    static void afterAll() throws InterruptedException {
        Thread.sleep(10000);
    }

    @Test
    void getUserByKeyUnless() {
        Key jack00 = new Key("jack00");
        User userJack00 = new User("0", jack00.getName(), jack00.getAge());

        // 1. 删除缓存元素
        userService.deleteUserByCache(jack00);

        // 2. unless 为 true，调用方法，且不缓存元素
        User result0 = userService.getUserByKeyUnless(jack00, 0);
        System.out.println(result0);
        Assertions.assertEquals(userJack00, result0);

        // 3. unless 为 false，调用方法，并缓存元素
        User result1 = userService.getUserByKeyUnless(jack00, 1);
        System.out.println(result1);
        userJack00.setId("1");
        Assertions.assertEquals(userJack00, result1);

        // 3. unless 为 false，不调用方法，读取缓存
        User result2 = userService.getUserByKeyUnless(jack00, 0);
        System.out.println(result2);
        Assertions.assertEquals(userJack00, result2);
    }


    @Test
    void getUserByKeyUnless1() {
        Key jack01 = new Key("jack01");
        User userJack01 = new User("2", jack01.getName(), jack01.getAge());

        // 1. 删除缓存元素
        userService.deleteUserByCache(jack01);

        // 2. 第一次：unless 为 false，调用方法，并缓存元素
        User result1 = userService.getUserByKeyUnless(jack01, 2);
        System.out.println(result1);
        Assertions.assertEquals(userJack01, result1);

        // 3. 第二次：unless 为 false，不调用方法，读取缓存
        User result2 = userService.getUserByKeyUnless(jack01, 3);
        System.out.println(result2);
        Assertions.assertEquals(userJack01, result2);
    }


    @Test
    void getUserByKeyCondition() {
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
    void saveUser() {
        Key jack03 = new Key("jack03");
        User userJack03 = new User("0", jack03.getName(), jack03.getAge());

        // 删除缓存元素
        userService.deleteUserByCache(jack03);

        // 调用方法，缓存元素
        userService.saveUser(jack03, userJack03);

        // 只读取缓存，判断是否保存成功
        User result2 = userService.getUserByCache(jack03);
        System.out.println(result2);
        Assertions.assertEquals(userJack03, result2);
    }

    @Test
    void deleteUser() {
        Key jack12 = new Key("jack12");
        User userJack12 = new User("0", jack12.getName(), jack12.getAge());
        userService.saveUser(jack12, userJack12);

        Assertions.assertEquals(userJack12, userService.getUserByCache(jack12));

        userService.deleteUser(jack12);

        Assertions.assertNull(userService.getUserByCache(jack12));
    }

    @Test
    void deleteAllUsers() {
        Key jack13 = new Key("jack13");
        User userJack13 = new User("0", jack13.getName(), jack13.getAge());

        Key jack14 = new Key("jack14");
        User userJack14 = new User("0", jack14.getName(), jack14.getAge());

        userService.saveUser(jack13, userJack13);
        userService.saveUser(jack14, userJack14);

        Assertions.assertEquals(userJack13, userService.getUserByCache(jack13));
        Assertions.assertEquals(userJack14, userService.getUserByCache(jack14));

        userService.deleteAllUsers();

        Assertions.assertNull(userService.getUserByCache(jack13));
        Assertions.assertNull(userService.getUserByCache(jack14));
    }

}