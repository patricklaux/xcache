package com.igeeksky.xcache.common;

/**
 * 具有过期信息的缓存值包装类
 *
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-16
 */
public class ExpiryCacheValue<V> extends CacheValue<V> {

    private volatile long expiry;

    public ExpiryCacheValue(V value) {
        super(value);
    }

    public ExpiryCacheValue(V value, long expiry) {
        super(value);
        this.expiry = expiry;
    }

    public long getExpiry() {
        return expiry;
    }

    public void setExpiry(long expiry) {
        this.expiry = expiry;
    }

}