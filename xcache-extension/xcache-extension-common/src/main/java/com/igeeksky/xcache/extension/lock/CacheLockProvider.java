package com.igeeksky.xcache.extension.lock;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.common.SPI;

import java.util.Map;

/**
 * 缓存锁工厂，用于获取 {@link CacheLock}
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
@SPI
public interface CacheLockProvider extends Provider {

    /**
     * 获取缓存锁的Holder
     */
    <K> CacheLock<K> get(String name, Class<K> keyClazz, Map<String, Object> metadata);

}
