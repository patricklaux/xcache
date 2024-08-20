package com.igeeksky.xcache.other;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class TestHash {

    public static void main(String[] args) {
        UUID uuid = UUID.randomUUID();
        System.out.println(uuid);
        System.out.println(uuid.getMostSignificantBits());

        String str = "hash";
        byte[] source = str.getBytes(StandardCharsets.UTF_8);
        Map<byte[], byte[]> map = new ConcurrentHashMap<>();
        map.put(source, source);

        System.out.println("str" + str.hashCode());
        System.out.println("source:" + Arrays.hashCode(source));

        // byte[] source2 = str.getBytes(StandardCharsets.UTF_8);

        byte[] bytes = map.get(source);
        if (null != bytes) {
            System.out.println(new String(bytes));
        } else {
            System.out.println("null");
        }
    }

}
