package com.igeeksky.xcache.redis.lettuce;

import io.lettuce.core.internal.Exceptions;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/10
 */
class Futures {

    public static int awaitAll(long timeout, TimeUnit unit, int start, Future<?>[] futures) {
        int i = start, len = futures.length;
        try {
            long nanos = unit.toNanos(timeout);
            long time = System.nanoTime();

            for (; i < len; i++) {
                if (nanos < 0) {
                    return i;
                }

                futures[i].get(nanos, TimeUnit.NANOSECONDS);

                long now = System.nanoTime();
                nanos -= now - time;
                time = now;
            }

            return i;
        } catch (TimeoutException e) {
            return i;
        } catch (Exception e) {
            throw Exceptions.fromSynchronization(e);
        }
    }

}
