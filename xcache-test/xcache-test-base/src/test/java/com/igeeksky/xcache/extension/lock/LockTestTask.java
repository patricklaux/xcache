package com.igeeksky.xcache.extension.lock;

import com.igeeksky.xtool.core.lang.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Lock;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/27
 */
public record LockTestTask(LockService lockService, String key) implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(LockTestTask.class);

    @Override
    public void run() {
        String lockKey = (key != null) ? key : RandomUtils.nextString(5);
        for (int i = 0; i < 100; i++) {
            Lock lock = lockService.acquire(lockKey);
            lock.lock();
            try {
                log.info("LockService-size: {}", lockService.size());
                log.info("Lock acquired: {}", Thread.currentThread().getName());
            } finally {
                lock.unlock();
            }
            lockService.release(lockKey);
        }
    }

}