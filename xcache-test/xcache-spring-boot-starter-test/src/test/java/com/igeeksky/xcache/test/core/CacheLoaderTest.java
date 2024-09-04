package com.igeeksky.xcache.test.core;

import com.igeeksky.xcache.common.CacheValue;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import com.igeeksky.xcache.test.UserLoaderWriterService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/28
 */
@SpringBootTest
public class CacheLoaderTest {

    @Resource
    private Map<Key, User> database;

    @Resource
    private UserLoaderWriterService loaderWriterService;

    @Test
    void getOrLoad() {
        Key key = new Key(10, "Lucy001");
        User user = new User("Lucy001", "Lucy001", 10);

        // 0. 首先删除缓存及数据源数据，避免影响测试
        loaderWriterService.delete(key);

        // 1. 仅保存到数据源
        database.put(key, user);

        // 1.1 数据源不为空
        Assertions.assertEquals(user, database.get(key));

        // 1.2 仅读取缓存，缓存无数据，不执行 CacheLoader，返回空
        CacheValue<User> cacheValue = loaderWriterService.get(key);
        Assertions.assertNull(cacheValue);

        // 1.3 读取缓存，缓存无数据，执行 CacheLoader 读取数据源
        User load = loaderWriterService.getOrLoad(key);
        Assertions.assertEquals(user, load);

        // 1.4 读取缓存，缓存有数据，直接返回缓存数据
        cacheValue = loaderWriterService.get(key);
        Assertions.assertEquals(user, cacheValue.getValue());


        // 2. 缓存 和 数据源 均删除
        loaderWriterService.delete(key);

        // 2.1 数据源无数据
        Assertions.assertNull(database.get(key));

        // 2.2 缓存无数据，不执行 CacheLoader，返回空
        cacheValue = loaderWriterService.get(key);
        Assertions.assertNull(cacheValue);

        // 2.3 缓存无数据，执行 CacheLoader 读取数据源，数据源无数据，缓存存入空值，最后返回空
        load = loaderWriterService.getOrLoad(key);
        Assertions.assertNull(load);

        // 2.4 缓存有空值，不执行 CacheLoader，直接返回空值
        cacheValue = loaderWriterService.get(key);
        Assertions.assertNotNull(cacheValue);
        Assertions.assertNull(cacheValue.getValue());


        // 3. 保存到 缓存，并由 CacheWriter 同步写入数据源
        loaderWriterService.put(key, user);

        // 3.1 数据源有数据
        Assertions.assertNotNull(database.get(key));

        // 3.2 读取缓存，缓存有数据，直接返回缓存数据
        cacheValue = loaderWriterService.get(key);
        Assertions.assertEquals(user, cacheValue.getValue());

        // 3.3 读取缓存，缓存有数据，直接返回缓存数据
        load = loaderWriterService.getOrLoad(key);
        Assertions.assertEquals(user, load);
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

        // 0. 首先删除缓存及数据源数据，避免影响测试
        loaderWriterService.deleteAll(keys);

        // 1. 仅保存到数据源
        database.putAll(keyValues);

        // 1.1 数据源不为空
        keyValues.forEach((key, user) -> Assertions.assertEquals(user, database.get(key)));

        // 1.2 仅读取缓存，缓存无数据，不执行 CacheLoader，返回空集
        Map<Key, CacheValue<User>> all = loaderWriterService.getAll(keys);
        Assertions.assertTrue(all.isEmpty());

        // 1.3 读取缓存，缓存无数据，执行 CacheLoader 读取数据源
        Map<Key, User> loadAll = loaderWriterService.getOrLoadAll(keys);
        Assertions.assertFalse(loadAll.isEmpty());
        loadAll.forEach((key, user) -> Assertions.assertEquals(keyValues.get(key), user));

        // 1.4 读取缓存，缓存有数据，直接返回缓存数据
        all = loaderWriterService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertEquals(keyValues.get(key), cacheValue.getValue()));


        // 2. 缓存 和 数据源 均删除
        loaderWriterService.deleteAll(keys);

        // 2.1 数据源无数据
        keyValues.forEach((key, user) -> Assertions.assertNull(database.get(key)));

        // 2.2 缓存无数据，不执行 CacheLoader，返回空
        all = loaderWriterService.getAll(keys);
        Assertions.assertTrue(all.isEmpty());

        // 2.3 缓存无数据，执行 CacheLoader 读取数据源，数据源无数据，缓存存入空值，最后返回空
        loadAll = loaderWriterService.getOrLoadAll(keys);
        Assertions.assertTrue(loadAll.isEmpty());

        // 2.4 缓存有空值，不执行 CacheLoader，直接返回空值
        all = loaderWriterService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertNull(cacheValue.getValue()));


        // 3. 保存到 缓存，并由 CacheWriter 同步写入数据源
        loaderWriterService.putAll(keyValues);

        // 3.1 数据源有数据
        keyValues.forEach((key, user) -> Assertions.assertNotNull(database.get(key)));

        // 3.2 读取缓存，缓存有数据，直接返回缓存数据
        all = loaderWriterService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertEquals(keyValues.get(key), cacheValue.getValue()));

        // 3.3 读取缓存，缓存有数据，直接返回缓存数据
        loadAll = loaderWriterService.getOrLoadAll(keys);
        Assertions.assertFalse(loadAll.isEmpty());
        loadAll.forEach((key, user) -> Assertions.assertEquals(keyValues.get(key), user));
    }

}
