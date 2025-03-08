package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xcache.common.ShutdownBehavior;
import com.igeeksky.xcache.extension.TasksInfo;
import com.igeeksky.xtool.core.concurrent.Futures;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 刷新工具类
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/12/10
 */
public final class RefreshHelper {

    private static final Logger log = LoggerFactory.getLogger(RefreshHelper.class);

    /**
     * 虚拟线程工厂
     */
    public static final VirtualThreadFactory VIRTUAL_FACTORY = new VirtualThreadFactory("virtual-refresh-");

    /**
     * 私有构造器
     */
    private RefreshHelper() {
    }

    /**
     * 检查上次刷新任务队列是否已经全部执行完成
     *
     * @return {@code true} - 刷新队列已完成； {@code false} - 刷新队列未完成
     */
    public static boolean tasksFinished(Queue<TasksInfo> tasksList) {
        Iterator<TasksInfo> iterator = tasksList.iterator();
        while (iterator.hasNext()) {
            TasksInfo tasks = iterator.next();
            if (tasks != null) {
                Future<?>[] futures = tasks.getFutures();
                int last = Futures.checkAll(futures, tasks.getStart());
                if (last < futures.length) {
                    tasks.setStart(last);
                    return false;
                }
                iterator.remove();
            }
        }
        return true;
    }

    public static CompletableFuture<Void> close(String name, ScheduledFuture<?> scheduledFuture,
                                                Queue<TasksInfo> tasksQueue,
                                                long quietPeriod, long timeout, TimeUnit unit,
                                                ShutdownBehavior behavior) {
        log.info("CacheRefresh:{}, Commencing graceful shutdown. {} refresh tasks.", name, behavior.name());
        try {
            scheduledFuture.cancel(ShutdownBehavior.INTERRUPT == behavior);
        } catch (Throwable e) {
            log.error("CacheRefresh:{}, shutdown has error. [{}]", name, e.getMessage(), e);
        }

        return CompletableFuture.runAsync(() -> shutdown(name, tasksQueue, quietPeriod, timeout, unit, behavior))
                .whenComplete((vod, t) -> {
                    if (t != null) {
                        log.error("CacheRefresh:{}, shutdown has error. {}", name, t.getMessage(), t);
                    } else {
                        log.info("CacheRefresh:{}, Graceful shutdown complete.", name);
                    }
                });
    }

    private static void shutdown(String name, Queue<TasksInfo> tasksQueue, long quietPeriod,
                                 long timeout, TimeUnit unit, ShutdownBehavior behavior1) {
        if (quietPeriod > 0) {
            // 休眠指定时间，等待任务队列的已有任务执行完成（及等待调度器将任务加入刷新任务队列）
            try {
                Thread.sleep(unit.toMillis(quietPeriod));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("CacheRefresh:{}, shutdown has error. [{}]", name, "Interrupted", e);
            }
        }
        if (tasksFinished(tasksQueue)) {
            return;
        }
        if (ShutdownBehavior.CANCEL == behavior1 || ShutdownBehavior.INTERRUPT == behavior1) {
            boolean interrupt = ShutdownBehavior.INTERRUPT == behavior1;
            tasksQueue.forEach(tasks -> {
                if (tasks != null) {
                    Futures.cancelAll(tasks.getFutures(), interrupt, tasks.getStart());
                }
            });
            return;
        }
        if (ShutdownBehavior.AWAIT == behavior1) {
            long endTime = System.currentTimeMillis() + unit.toMillis(timeout);
            tasksQueue.forEach(tasks -> {
                if (tasks != null) {
                    try {
                        long remainTime = endTime - System.currentTimeMillis();
                        if (remainTime <= 0) {
                            log.error("CacheRefresh:{}, await tasks timeout.", name);
                            return;
                        }
                        Futures.awaitAll(tasks.getFutures(), remainTime, TimeUnit.MILLISECONDS, tasks.getStart());
                    } catch (Throwable e) {
                        log.error("CacheRefresh:{}, await tasks has error. {}", name, e.getMessage(), e);
                    }
                }
            });
        }
    }

    /**
     * 重设最大关闭超时时间
     *
     * @param maxShutdownTimeout 最大关闭超时时间
     * @param shutdownTimeout    关闭超时
     */
    public static void resetMaxShutdownTimeout(AtomicLong maxShutdownTimeout, long shutdownTimeout) {
        long maxTimeout = maxShutdownTimeout.get();
        while (shutdownTimeout > maxTimeout) {
            if (maxShutdownTimeout.compareAndSet(maxTimeout, shutdownTimeout)) {
                break;
            }
            maxTimeout = maxShutdownTimeout.get();
        }
    }

}
