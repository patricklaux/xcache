package com.igeeksky.xcache.core;

import com.igeeksky.xtool.core.lang.Assert;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * 单例工厂抽象类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class SingletonSupplier<T> implements Supplier<T> {

    private volatile T instance;
    private volatile boolean initialized;

    private final Supplier<T> supplier;
    private final Lock lock = new ReentrantLock();

    private SingletonSupplier(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> SingletonSupplier<T> of(Supplier<T> supplier) {
        Assert.notNull(supplier, "Supplier must not be null");
        return new SingletonSupplier<>(supplier);
    }

    /**
     * 获取实例，如果实例不存在，则创建实例
     *
     * @return 实例
     */
    @Override
    public final T get() {
        if (instance == null && !initialized) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = supplier.get();
                }
                initialized = true;
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    /**
     * 获取实例，如果实例不存在，则返回 null
     *
     * @return 实例
     */
    public final T getIfPresent() {
        return instance;
    }

}
