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

        // 0. ����ɾ�����漰����Դ���ݣ�����Ӱ�����
        loaderWriterService.delete(key);

        // 1. �����浽����Դ
        database.put(key, user);

        // 1.1 ����Դ��Ϊ��
        Assertions.assertEquals(user, database.get(key));

        // 1.2 ����ȡ���棬���������ݣ���ִ�� CacheLoader�����ؿ�
        CacheValue<User> cacheValue = loaderWriterService.get(key);
        Assertions.assertNull(cacheValue);

        // 1.3 ��ȡ���棬���������ݣ�ִ�� CacheLoader ��ȡ����Դ
        User load = loaderWriterService.getOrLoad(key);
        Assertions.assertEquals(user, load);

        // 1.4 ��ȡ���棬���������ݣ�ֱ�ӷ��ػ�������
        cacheValue = loaderWriterService.get(key);
        Assertions.assertEquals(user, cacheValue.getValue());


        // 2. ���� �� ����Դ ��ɾ��
        loaderWriterService.delete(key);

        // 2.1 ����Դ������
        Assertions.assertNull(database.get(key));

        // 2.2 ���������ݣ���ִ�� CacheLoader�����ؿ�
        cacheValue = loaderWriterService.get(key);
        Assertions.assertNull(cacheValue);

        // 2.3 ���������ݣ�ִ�� CacheLoader ��ȡ����Դ������Դ�����ݣ���������ֵ����󷵻ؿ�
        load = loaderWriterService.getOrLoad(key);
        Assertions.assertNull(load);

        // 2.4 �����п�ֵ����ִ�� CacheLoader��ֱ�ӷ��ؿ�ֵ
        cacheValue = loaderWriterService.get(key);
        Assertions.assertNotNull(cacheValue);
        Assertions.assertNull(cacheValue.getValue());


        // 3. ���浽 ���棬���� CacheWriter ͬ��д������Դ
        loaderWriterService.put(key, user);

        // 3.1 ����Դ������
        Assertions.assertNotNull(database.get(key));

        // 3.2 ��ȡ���棬���������ݣ�ֱ�ӷ��ػ�������
        cacheValue = loaderWriterService.get(key);
        Assertions.assertEquals(user, cacheValue.getValue());

        // 3.3 ��ȡ���棬���������ݣ�ֱ�ӷ��ػ�������
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

        // 0. ����ɾ�����漰����Դ���ݣ�����Ӱ�����
        loaderWriterService.deleteAll(keys);

        // 1. �����浽����Դ
        database.putAll(keyValues);

        // 1.1 ����Դ��Ϊ��
        keyValues.forEach((key, user) -> Assertions.assertEquals(user, database.get(key)));

        // 1.2 ����ȡ���棬���������ݣ���ִ�� CacheLoader�����ؿռ�
        Map<Key, CacheValue<User>> all = loaderWriterService.getAll(keys);
        Assertions.assertTrue(all.isEmpty());

        // 1.3 ��ȡ���棬���������ݣ�ִ�� CacheLoader ��ȡ����Դ
        Map<Key, User> loadAll = loaderWriterService.getOrLoadAll(keys);
        Assertions.assertFalse(loadAll.isEmpty());
        loadAll.forEach((key, user) -> Assertions.assertEquals(keyValues.get(key), user));

        // 1.4 ��ȡ���棬���������ݣ�ֱ�ӷ��ػ�������
        all = loaderWriterService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertEquals(keyValues.get(key), cacheValue.getValue()));


        // 2. ���� �� ����Դ ��ɾ��
        loaderWriterService.deleteAll(keys);

        // 2.1 ����Դ������
        keyValues.forEach((key, user) -> Assertions.assertNull(database.get(key)));

        // 2.2 ���������ݣ���ִ�� CacheLoader�����ؿ�
        all = loaderWriterService.getAll(keys);
        Assertions.assertTrue(all.isEmpty());

        // 2.3 ���������ݣ�ִ�� CacheLoader ��ȡ����Դ������Դ�����ݣ���������ֵ����󷵻ؿ�
        loadAll = loaderWriterService.getOrLoadAll(keys);
        Assertions.assertTrue(loadAll.isEmpty());

        // 2.4 �����п�ֵ����ִ�� CacheLoader��ֱ�ӷ��ؿ�ֵ
        all = loaderWriterService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertNull(cacheValue.getValue()));


        // 3. ���浽 ���棬���� CacheWriter ͬ��д������Դ
        loaderWriterService.putAll(keyValues);

        // 3.1 ����Դ������
        keyValues.forEach((key, user) -> Assertions.assertNotNull(database.get(key)));

        // 3.2 ��ȡ���棬���������ݣ�ֱ�ӷ��ػ�������
        all = loaderWriterService.getAll(keys);
        Assertions.assertFalse(all.isEmpty());
        all.forEach((key, cacheValue) -> Assertions.assertEquals(keyValues.get(key), cacheValue.getValue()));

        // 3.3 ��ȡ���棬���������ݣ�ֱ�ӷ��ػ�������
        loadAll = loaderWriterService.getOrLoadAll(keys);
        Assertions.assertFalse(loadAll.isEmpty());
        loadAll.forEach((key, user) -> Assertions.assertEquals(keyValues.get(key), user));
    }

}
