package com.igeeksky.xcache.other;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class GeneralTest {

    @Test
    void testHash() {
        Map<byte[], String> map = new ConcurrentHashMap<>();

        String value = "hash";
        byte[] key0 = value.getBytes(StandardCharsets.UTF_8);
        map.put(key0, value);

        byte[] key1 = value.getBytes(StandardCharsets.UTF_8);

        System.out.println("key0: " + map.get(key0));
        System.out.println("key1: " + map.get(key1));

        Assertions.assertNotNull(map.get(key0));
        Assertions.assertNull(map.get(key1));
    }

    @Test
    void testUuid() {
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid);
        System.out.println(uuid.getMostSignificantBits());
        System.out.println(uuid.getLeastSignificantBits());
    }

}
