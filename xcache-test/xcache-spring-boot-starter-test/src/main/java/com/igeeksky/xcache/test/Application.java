package com.igeeksky.xcache.test;

import com.igeeksky.xcache.aop.EnableCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;


/**
 * Xcache 注解测试启动程序
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-27
 */
@EnableCache(basePackages = {"com.igeeksky.xcache.test"})
@SpringBootApplication(scanBasePackages = "com.igeeksky.xcache.test",
        exclude = {RedisRepositoriesAutoConfiguration.class, RedisAutoConfiguration.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

}
