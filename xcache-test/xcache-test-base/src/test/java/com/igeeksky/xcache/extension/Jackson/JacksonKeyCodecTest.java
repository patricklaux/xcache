package com.igeeksky.xcache.extension.Jackson;

import com.igeeksky.xcache.domain.User;
import com.igeeksky.xcache.extension.jackson.JacksonKeyCodec;
import com.igeeksky.xtool.core.lang.codec.KeyCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * JacksonKeyCodec ≤‚ ‘
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-15
 */
class JacksonKeyCodecTest {

    @Test
    void encode() {
        User user = new User("John");
        KeyCodec<User> keyCodec = new JacksonKeyCodec<>(User.class);
        String key = keyCodec.encode(user);
        Assertions.assertEquals("{\"name\":\"John\",\"age\":0}", key);
    }

    @Test
    void decode() {
        String key = "{\"name\":\"John\",\"age\":0}";
        KeyCodec<User> keyCodec = new JacksonKeyCodec<>(User.class);
        User user = keyCodec.decode(key);
        Assertions.assertNull(user.getId());
        Assertions.assertEquals(0, user.getAge());
        Assertions.assertEquals("John", user.getName());
    }

    @Test
    void testDecode() {
        String key = "{\"name\":\"John\"}";
        KeyCodec<User> keyCodec = new JacksonKeyCodec<>(User.class);
        User user = keyCodec.decode(key);
        Assertions.assertNull(user.getId());
        Assertions.assertEquals(0, user.getAge());
        Assertions.assertEquals("John", user.getName());
    }

}