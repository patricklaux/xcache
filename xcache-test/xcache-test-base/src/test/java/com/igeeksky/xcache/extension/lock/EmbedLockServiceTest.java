package com.igeeksky.xcache.extension.lock;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 内嵌锁测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/23
 */
class EmbedLockServiceTest {

    private static final String key = "test-lock";

    @Test
    void lock() throws InterruptedException {
        EmbedLockService lockService = new EmbedLockService(256);
        LockTestTask task = new LockTestTask(lockService, key);

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(task);
            thread.start();
        }

        Thread.sleep(2000);

        Assertions.assertEquals(0, lockService.size());
    }

}