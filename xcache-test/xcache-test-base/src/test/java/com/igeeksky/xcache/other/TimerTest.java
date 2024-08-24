package com.igeeksky.xcache.other;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/25
 */
public class TimerTest {

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @Test
    public void test() throws InterruptedException {
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> System.out.println("timer: " + System.currentTimeMillis()),
                1000, 1000, TimeUnit.MILLISECONDS
        );

        Thread.sleep(5000);

        System.out.println("1: " + future.cancel(false));
        System.out.println("2: " + future.isCancelled());

        Assertions.assertTrue(future.isCancelled());
    }

}