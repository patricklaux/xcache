package com.igeeksky.xcache.core;

import com.igeeksky.xcache.extension.serializer.StringSerializer;
import com.igeeksky.xtool.core.lang.ArrayUtils;

import java.util.Arrays;

/**
 * <p>缓存前缀</p>
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-20
 */
public class CacheKeyPrefix {

    private final String keyPrefix;

    private final byte[] keyPrefixBytes;

    private final StringSerializer serializer;

    public CacheKeyPrefix(String name, StringSerializer serializer) {
        this.keyPrefix = name + ":";
        this.serializer = serializer;
        this.keyPrefixBytes = serializer.serialize(keyPrefix);
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public byte[] getKeyPrefixBytes() {
        return keyPrefixBytes;
    }

    public byte[] createHashKey(int index) {
        return serializer.serialize(keyPrefix + index);
    }

    public String concatPrefix(String key) {
        return keyPrefix + key;
    }

    public byte[] concatPrefixBytes(String key) {
        return ArrayUtils.concat(keyPrefixBytes, serializer.serialize(key));
    }

    public String removePrefix(String keyWithPrefix) {
        return removePrefix(serializer.serialize(keyWithPrefix));
    }

    public String removePrefix(byte[] keyWithPrefixBytes) {
        byte[] keyBytes = Arrays.copyOfRange(keyWithPrefixBytes, keyPrefixBytes.length, keyWithPrefixBytes.length);
        return serializer.deserialize(keyBytes);
    }

}
