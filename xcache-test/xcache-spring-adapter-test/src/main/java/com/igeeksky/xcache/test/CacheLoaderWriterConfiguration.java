package com.igeeksky.xcache.test;

import com.igeeksky.xcache.autoconfigure.CacheAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.holder.CacheLoaderHolder;
import com.igeeksky.xcache.autoconfigure.holder.CacheWriterHolder;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.common.CacheWriter;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import com.igeeksky.xtool.core.collection.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Set;

/**
 * 缓存加载器写入器配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/27
 */
@Configuration
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class CacheLoaderWriterConfiguration {

    /**
     * 模拟数据库
     * <p>
     * 用以测试 CacheWriter 是否写入数据；
     * 用以测试 CacheLoader 是否读取数据。
     */
    private final Map<Key, User> database = Maps.newConcurrentHashMap();

    private static final Logger log = LoggerFactory.getLogger(CacheLoaderWriterConfiguration.class);

    @Bean
    Map<Key, User> database() {
        return database;
    }

    @Bean
    CacheLoaderHolder cacheLoader() {
        CacheLoaderHolder holder = new CacheLoaderHolder();
        CacheLoader<Key, User> cacheLoader = key -> {
            log.info("CacheLoader:load key: {}", key);
            return database.get(key);
        };
        holder.put("user", cacheLoader);
        return holder;
    }

    @Bean
    CacheWriterHolder cacheWriter() {

        CacheWriterHolder holder = new CacheWriterHolder();

        CacheWriter<Key, User> cacheWriter = new CacheWriter<>() {

            @Override
            public void delete(Key key) {
                database.remove(key);
                log.info("CacheWriter:delete key: {}", key);
            }

            @Override
            public void deleteAll(Set<? extends Key> keys) {
                keys.forEach(database::remove);
                log.info("CacheWriter:deleteAll keys: {}", keys);
            }

            @Override
            public void write(Key key, User value) {
                database.put(key, value);
                log.info("CacheWriter:write key: {}, value: {}", key, value);
            }

            @Override
            public void writeAll(Map<? extends Key, ? extends User> keyValues) {
                database.putAll(keyValues);
                log.info("CacheWriter:writeAll keyValues: {}", keyValues);
            }

        };

        holder.put("user", cacheWriter);
        return holder;
    }

}