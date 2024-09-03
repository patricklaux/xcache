package com.igeeksky.xcache.common;

/**
 * 具有过期信息的缓存值包装类
 *
 * @param <V> 缓存值类型
 * @author Patrick.Lau
 * @since 0.0.3 2021-06-16
 */
public class ExpiryCacheValue<V> extends CacheValue<V> {

    private volatile long expiryTime;

    public ExpiryCacheValue(V value) {
        super(value);
    }

    public ExpiryCacheValue(V value, long expiryTime) {
        super(value);
        this.expiryTime = expiryTime;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

}