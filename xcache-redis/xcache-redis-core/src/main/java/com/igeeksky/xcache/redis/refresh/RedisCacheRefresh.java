package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshTask;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.function.tuple.Tuples;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Redis 缓存刷新（非集群模式）
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/29
 */
public class RedisCacheRefresh extends AbstractRedisCacheRefresh {

    private final byte[][] refreshKey = new byte[1][];

    public RedisCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler, ExecutorService executor,
                             RedisOperator operator) {
        super(config, scheduler, executor, operator);
        this.refreshKey[0] = this.stringCodec.encode(config.getRefreshKey());
    }

    @Override
    public void doPut(String key) {
        this.put(refreshKey, new byte[][]{refreshAfterWrite, stringCodec.encode(key)});
    }

    @Override
    public void doPutAll(Set<String> keys) {
        int i = 0;
        byte[][] args = new byte[keys.size() + 1][];
        args[0] = refreshAfterWrite;
        for (String key : keys) {
            args[++i] = stringCodec.encode(key);
        }
        this.put(refreshKey, args);
    }

    @Override
    public void doRemove(String key) {
        this.remove(refreshKey, new byte[][]{stringCodec.encode(key)});
    }

    @Override
    public void doRemoveAll(Set<String> keys) {
        int i = 0;
        byte[][] args = new byte[keys.size()][];
        for (String key : keys) {
            args[i++] = stringCodec.encode(key);
        }
        this.remove(refreshKey, args);
    }

    protected void refresh() {
        long now = this.getServerTime();
        int total = 0, index = 0, count = Math.min(FUTURES_LENGTH, refreshTasksSize);

        Future<?>[] futures = new Future<?>[count];
        tasksList.add(Tuples.of(futures, 0));

        while (total < refreshTasksSize) {
            // 1. 获取当前需要刷新的缓存键
            List<byte[]> members = getRefreshMembers(refreshKey[0], now, count);
            if (CollectionUtils.isEmpty(members)) {
                break;
            }
            // 2. 循环刷新每一个键
            for (byte[] member : members) {
                if (member != null) {
                    if (index >= count) {
                        index = 0;
                        futures = new Future<?>[count];
                        tasksList.add(Tuples.of(futures, 0));
                    }
                    RefreshTask task = new RefreshTask(this, stringCodec.decode(member), consumer, predicate);
                    futures[index++] = executor.submit(task);
                    total++;
                }
            }
        }
    }

}