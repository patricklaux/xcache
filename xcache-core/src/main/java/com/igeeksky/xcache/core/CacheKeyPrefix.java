package com.igeeksky.xcache.core;


import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

/**
 * <p>缓存前缀</p>
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-20
 */
public class CacheKeyPrefix {

    private final String keyPrefix;

    private final byte[] keyPrefixBytes;

    private final int keyPrefixLength;

    private final StringCodec stringCodec;

    public CacheKeyPrefix(String name, StringCodec stringCodec) {
        this.keyPrefix = name + ":";
        this.stringCodec = stringCodec;
        this.keyPrefixBytes = stringCodec.encode(keyPrefix);
        this.keyPrefixLength = keyPrefixBytes.length;
    }

    public byte[] createHashKey(int index) {
        return stringCodec.encode(keyPrefix + index);
    }

    public String concatPrefix(String key) {
        return keyPrefix + key;
    }

    public byte[] concatPrefixBytes(String key) {
        return ArrayUtils.concat(keyPrefixBytes, stringCodec.encode(key));
    }

    public String removePrefix(String keyWithPrefix) {
        return removePrefix(stringCodec.encode(keyWithPrefix));
    }

    public String removePrefix(byte[] keyWithPrefixBytes) {
        return stringCodec.decode(keyWithPrefixBytes, keyPrefixLength, keyWithPrefixBytes.length);
    }

}
