package com.igeeksky.xcache.core;


import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

/**
 * 缓存键前缀生成类
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-08-20
 */
public class CacheKeyPrefix {

    private final String prefix;

    private final byte[] prefixBytes;

    private final int prefixLength;

    private final StringCodec stringCodec;

    public CacheKeyPrefix(String group, String name, boolean enableGroupPrefix, StringCodec stringCodec) {
        if (enableGroupPrefix) {
            this.prefix = group + ":" + name + ":";
        } else {
            this.prefix = name + ":";
        }
        this.stringCodec = stringCodec;
        this.prefixBytes = stringCodec.encode(prefix);
        this.prefixLength = this.prefixBytes.length;
    }

    public byte[] createHashKey(int index) {
        return stringCodec.encode(prefix + index);
    }

    public String concatPrefix(String key) {
        return prefix + key;
    }

    public byte[] concatPrefixBytes(String key) {
        return ArrayUtils.concat(prefixBytes, stringCodec.encode(key));
    }

    public String removePrefix(String keyWithPrefix) {
        return removePrefix(stringCodec.encode(keyWithPrefix));
    }

    public String removePrefix(byte[] keyWithPrefix) {
        return stringCodec.decode(keyWithPrefix, prefixLength, keyWithPrefix.length - prefixLength);
    }

}
