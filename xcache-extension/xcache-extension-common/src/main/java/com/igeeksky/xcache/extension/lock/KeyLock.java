package com.igeeksky.xcache.extension.lock;

import java.util.concurrent.locks.Lock;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/12
 */
public abstract class KeyLock implements Lock {

    private final String key;

    public KeyLock(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}