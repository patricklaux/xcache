package com.igeeksky.xcache.store;

import com.igeeksky.xcache.common.annotation.NotNull;
import com.igeeksky.xcache.extension.serialization.Serializer;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 缓存前缀<br/>
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-20
 */
public class CacheKeyPrefix<K> {

    private final String keyPrefix;
    private final byte[] keyPrefixBytes;
    private final Charset charset;
    private final Serializer<K> serializer;

    public CacheKeyPrefix(Serializer<K> serializer, Charset charset, String namespace, String name) {
        this.serializer = serializer;
        this.charset = charset;
        this.keyPrefix = createKeyPrefix(namespace, name);
        this.keyPrefixBytes = this.keyPrefix.getBytes(charset);
    }

    @NotNull
    private String createKeyPrefix(String namespace, String name) {
        String SEPARATOR = "::";
        namespace = StringUtils.trim(namespace);
        if (null == namespace) {
            return name + SEPARATOR;
        }
        return namespace + SEPARATOR + name + SEPARATOR;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public byte[] createHashKey(int index) {
        return (keyPrefix + index).getBytes(charset);
    }

    public String concatPrefix(K key) {
        return keyPrefix + new String(serializer.serialize(key), charset);
    }

    public byte[] concatPrefixBytes(K key) {
        return ArrayUtils.concat(keyPrefixBytes, serializer.serialize(key));
    }

    public K removePrefix(String keyWithPrefix) {
        byte[] keyWithPrefixBytes = keyWithPrefix.getBytes(charset);
        return removePrefix(keyWithPrefixBytes);
    }

    public K removePrefix(byte[] keyWithPrefixBytes) {
        byte[] keyBytes = Arrays.copyOfRange(keyWithPrefixBytes, keyPrefixBytes.length, keyWithPrefixBytes.length);
        return serializer.deserialize(keyBytes);
    }

}
