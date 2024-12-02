package com.igeeksky.redis.jedis;

import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

/**
 * Jedis 基础测试
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-27
 */
public class TestJedis {

    @Test
    void test() {
        try (Jedis jedis = new Jedis("localhost", 6379)) {
            String jack = jedis.get("Jack");
            System.out.println(jack);
        }
    }

}
