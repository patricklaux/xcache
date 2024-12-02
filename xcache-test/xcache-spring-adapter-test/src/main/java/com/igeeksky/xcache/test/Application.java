package com.igeeksky.xcache.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;


/**
 * 适配 Spring Cache 测试
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-27
 */
@EnableCaching
@SpringBootApplication(scanBasePackages = "com.igeeksky.xcache.test",
        exclude = {RedisRepositoriesAutoConfiguration.class, RedisAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

}
