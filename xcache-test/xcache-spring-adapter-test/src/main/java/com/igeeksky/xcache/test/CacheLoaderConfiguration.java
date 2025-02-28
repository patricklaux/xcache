package com.igeeksky.xcache.test;

import com.igeeksky.xcache.autoconfigure.CacheAutoConfiguration;
import com.igeeksky.xcache.autoconfigure.register.CacheLoaderRegister;
import com.igeeksky.xcache.common.CacheLoader;
import com.igeeksky.xcache.core.SingletonSupplier;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import com.igeeksky.xtool.core.collection.Maps;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * CacheLoader 自动配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/27
 */
@Configuration
@AutoConfigureBefore(CacheAutoConfiguration.class)
class CacheLoaderConfiguration {

    /**
     * 模拟数据库
     * <p>
     * 用以测试 CacheWriter 是否写入数据；
     * 用以测试 CacheLoader 是否读取数据。
     */
    private final Map<Key, User> database = Maps.newConcurrentHashMap();

    @Bean
    Map<Key, User> database() {
        return database;
    }

    @Bean
    CacheLoaderRegister cacheLoader() {
        CacheLoaderRegister holder = new CacheLoaderRegister();
        holder.put("user", SingletonSupplier.of(() -> (CacheLoader<Key, User>) database::get));
        return holder;
    }

}