package com.igeeksky.redis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-06
 */
class RedisNodeTest {

    @Test
    void testErrorHostAndPort() {
        Exception err = null;
        try {
            new RedisNode("2:0:1");
        } catch (Exception e) {
            err = e;
        }
        Assertions.assertInstanceOf(IllegalArgumentException.class, err);
        Assertions.assertEquals(err.getMessage(), "node:[2:0:1] is not valid.");
    }

    @Test
    void getHostAndPort() {
        String hp = "127.0.0.1:6379";
        RedisNode node = new RedisNode(hp);
        String host = node.getHost();
        int port = node.getPort();
        Assertions.assertEquals("127.0.0.1", host);
        Assertions.assertEquals(6379, port);
        Assertions.assertNull(node.getSocket());
    }

    @Test
    void getSocket() {
        RedisNode node = new RedisNode("socket:/tmp/redis.sock");
        Assertions.assertNull(node.getHost());
        Assertions.assertEquals(-1, node.getPort());
        Assertions.assertEquals("/tmp/redis.sock", node.getSocket());
    }

}