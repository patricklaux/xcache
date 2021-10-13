package com.igeeksky.xcache.extension.lock;

import com.igeeksky.xcache.common.Singleton;
import com.igeeksky.xcache.config.PropertiesKey;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-06-10
 */
@Singleton
public class LocalCacheLockProvider implements CacheLockProvider {

    private static final int DEFAULT_LOCK_SIZE = 512;
    private final int presetLockSize;

    public LocalCacheLockProvider(Integer lockSize) {
        this.presetLockSize = null != lockSize ? lockSize : DEFAULT_LOCK_SIZE;
    }

    @Override
    public <K> CacheLock<K> get(String name, Class<K> keyClazz, Map<String, Object> metadata) {
        int lockSize = PropertiesKey.getInteger(metadata, PropertiesKey.METADATA_LOCK_SIZE, presetLockSize);
        return new LocalCacheLock<>(lockSize);
    }

}