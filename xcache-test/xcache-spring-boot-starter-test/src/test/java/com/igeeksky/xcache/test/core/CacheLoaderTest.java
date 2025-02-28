package com.igeeksky.xcache.test.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import com.igeeksky.xcache.test.UserLoaderService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * CacheLoader 测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/28
 */
@SpringBootTest
public class CacheLoaderTest {

    @Resource
    private Map<Key, User> database;

    @Resource
    private UserLoaderService userLoaderService;

    @Test
    void getOrLoad() {
        Key key = new Key(10, "Lucy001");
        User user = new User("Lucy001", "Lucy001", 10);

        // 1. 测试：数据源有数据，缓存非空值
        // 1.0 删除缓存数据及数据源数据
        database.remove(key);
        userLoaderService.delete(key);

        // 1.1 保存到数据源
        database.put(key, user);

        // 1.2 数据源有数据
        Assertions.assertEquals(user, database.get(key));

        // 1.3 读取缓存，缓存无数据，不执行 CacheLoader，返回空
        CacheValue<User> cacheValue = userLoaderService.get(key);
        Assertions.assertNull(cacheValue);

        // 1.4 读取缓存，缓存无数据，执行 CacheLoader 读取数据源
        User load = userLoaderService.getOrLoad(key);
        Assertions.assertEquals(user, load);

        // 1.5 读取缓存，缓存有数据，直接返回缓存数据
        cacheValue = userLoaderService.get(key);
        Assertions.assertEquals(user, cacheValue.getValue());


        // 2. 测试：数据源无数据，缓存空值
        // 2.0 删除缓存数据及数据源数据
        database.remove(key);
        userLoaderService.delete(key);

        // 2.1 数据源无数据
        Assertions.assertNull(database.get(key));

        // 2.2 读取缓存，缓存无数据，不执行 CacheLoader，返回空
        cacheValue = userLoaderService.get(key);
        Assertions.assertNull(cacheValue);

        // 2.3 读取缓存，缓存无数据，执行 CacheLoader 读取数据源，数据源无数据，缓存存入空值，最后返回空
        load = userLoaderService.getOrLoad(key);
        Assertions.assertNull(load);

        // 2.4 读取缓存，缓存有空值，不执行 CacheLoader，直接返回空值
        cacheValue = userLoaderService.get(key);
        Assertions.assertNotNull(cacheValue);
        Assertions.assertNull(cacheValue.getValue());


        // 3. 测试：缓存有数据，数据源无数据
        // 3.0 保存到 缓存
        userLoaderService.put(key, user);

        // 3.1 数据源无数据
        Assertions.assertNull(database.get(key));

        // 3.2 读取缓存，缓存有数据，直接返回缓存数据
        cacheValue = userLoaderService.get(key);
        Assertions.assertEquals(user, cacheValue.getValue());

        // 3.3 读取缓存，缓存有数据，不执行 CacheLoader，直接返回缓存数据
        load = userLoaderService.getOrLoad(key);
        Assertions.assertEquals(user, load);

        // 3.4 删除缓存数据，数据源无数据，缓存无数据
        userLoaderService.delete(key);

        // 3.5 读取缓存，缓存无数据，执行 CacheLoader 读取数据源，数据源无数据，缓存存入空值，最后返回空
        load = userLoaderService.getOrLoad(key);
        Assertions.assertNull(load);
    }

    @Test
    void getOrLoadAll() {
        Key key1 = new Key(10, "Lucy001");
        User user1 = new User("Lucy001", "Lucy001", 10);
        Key key2 = new Key(20, "Lucy002");
        User user2 = new User("Lucy002", "Lucy002", 20);
        Key key3 = new Key(30, "Lucy003");
        User user3 = new User("Lucy003", "Lucy003", 30);

        Map<Key, User> keyValues = Map.of(key1, user1, key2, user2, key3, user3);
        Set<Key> keys = new HashSet<>(keyValues.keySet());

        // 1. 测试：数据源有数据，缓存非空值
        // 1.0 删除缓存数据和数据源数据
        database.clear();
        userLoaderService.deleteAll(keys);

        // 1.1 保存到数据源
        database.putAll(keyValues);

        // 1.2 数据源不为空
        keyValues.forEach((key, user) -> Assertions.assertEquals(user, database.get(key)));

        // 1.3 仅读取缓存，缓存无数据，不执行 CacheLoader，返回空集
        Map<Key, CacheValue<User>> all = userLoaderService.getAll(keys);
        Assertions.assertTrue(all.isEmpty());

        // 1.4 读取缓存，缓存无数据，执行 CacheLoader 读取数据源
        Map<Key, User> loadAll = userLoaderService.getOrLoadAll(keys);
        Assertions.assertFalse(loadAll.isEmpty());
        loadAll.forEach((key, user) -> Assertions.assertEquals(keyValues.get(key), user));

        // 1.5 读取缓存，缓存有数据，直接返回缓存数据
        all = userLoaderService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertEquals(keyValues.get(key), cacheValue.getValue()));


        // 2. 测试：数据源无数据，缓存空值
        // 2.0 缓存 和 数据源 均删除
        database.clear();
        userLoaderService.deleteAll(keys);

        // 2.1 数据源无数据
        keyValues.forEach((key, user) -> Assertions.assertNull(database.get(key)));

        // 2.2 缓存无数据，不执行 CacheLoader，返回空
        all = userLoaderService.getAll(keys);
        Assertions.assertTrue(all.isEmpty());

        // 2.3 缓存无数据，执行 CacheLoader 读取数据源，数据源无数据，缓存存入空值，最后返回空
        loadAll = userLoaderService.getOrLoadAll(keys);
        Assertions.assertTrue(loadAll.isEmpty());

        // 2.4 缓存有空值，不执行 CacheLoader，直接返回空值
        all = userLoaderService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertNull(cacheValue.getValue()));


        // 3. 测试：缓存有数据，数据源无数据
        // 3. 保存到缓存
        userLoaderService.putAll(keyValues);

        // 3.1 数据源无数据
        keyValues.forEach((key, user) -> Assertions.assertNull(database.get(key)));

        // 3.2 读取缓存，缓存有数据，直接返回缓存数据
        all = userLoaderService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertEquals(keyValues.get(key), cacheValue.getValue()));

        // 3.3 读取缓存，缓存有数据，不执行 CacheLoader，直接返回缓存数据
        loadAll = userLoaderService.getOrLoadAll(keys);
        Assertions.assertFalse(loadAll.isEmpty());
        loadAll.forEach((key, user) -> Assertions.assertEquals(keyValues.get(key), user));

        // 3.4 删除缓存数据，数据源无数据，缓存无数据
        userLoaderService.deleteAll(keys);

        // 3.5 读取缓存，缓存无数据，执行 CacheLoader 读取数据源，数据源无数据，缓存存入空值，最后返回空
        loadAll = userLoaderService.getOrLoadAll(keys);
        Assertions.assertTrue(loadAll.isEmpty());

        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100000));
    }

}
