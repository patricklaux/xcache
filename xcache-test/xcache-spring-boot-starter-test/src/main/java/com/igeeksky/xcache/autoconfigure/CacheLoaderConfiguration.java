package com.igeeksky.xcache.autoconfigure;

import com.igeeksky.xcache.core.CacheLoader;
import com.igeeksky.xcache.autoconfigure.holder.CacheLoaderHolder;
import com.igeeksky.xcache.domain.Key;
import com.igeeksky.xcache.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/27
 */
@Configuration
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class CacheLoaderConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CacheLoaderConfiguration.class);

    @Bean
    CacheLoaderHolder cacheLoader() {
        CacheLoaderHolder holder = new CacheLoaderHolder();
        CacheLoader<Key, User> cacheLoader = key -> {
            String name = key.getName();
            log.info("loader: {}", name);
            return new User("loader:" + name, name, key.getAge());
        };
        holder.put("user", cacheLoader);
        return holder;
    }

}