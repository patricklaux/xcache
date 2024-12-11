package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.sorted.ScoredValue;
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
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/29
 */
public class RedisCacheRefresh extends AbstractRedisCacheRefresh {

    private final byte[] refreshKey;

    public RedisCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler, ExecutorService executor,
                             RedisOperator operator) {
        super(config, scheduler, executor, operator);
        this.refreshKey = this.stringCodec.encode(config.getRefreshKey());
    }

    @Override
    public void onPut(String key) {
        this.operator.zadd(refreshKey, nextRefreshTime(), stringCodec.encode(key));
    }

    @Override
    public void onPutAll(Set<String> keys) {
        int i = 0;
        long nextRefreshTime = nextRefreshTime();
        ScoredValue[] values = new ScoredValue[keys.size()];
        for (String key : keys) {
            values[i++] = ScoredValue.just(stringCodec.encode(key), nextRefreshTime);
        }
        this.operator.zadd(refreshKey, values);
    }

    @Override
    public void onRemove(String key) {
        this.operator.zrem(refreshKey, stringCodec.encode(key));
    }

    @Override
    public void onRemoveAll(Set<String> keys) {
        int i = 0, size = keys.size();
        byte[][] values = new byte[size][];
        for (String key : keys) {
            values[i++] = stringCodec.encode(key);
        }
        this.operator.zrem(refreshKey, values);
    }

    protected void refreshNow() {
        long now = now();
        int i = 0, j = 0, maximum = Math.min(MAXIMUM, maxRefreshTasks);

        Future<?>[] futures = new Future<?>[maximum];
        tasksList.add(Tuples.of(futures, 0));

        while (true) {
            // 1. 获取当前需要刷新的缓存键
            List<byte[]> members = this.operator.zrangebyscore(refreshKey, 0, now, 0, maximum);
            if (CollectionUtils.isEmpty(members)) {
                break;
            }

            // 2. 先移动到队尾，避免其它实例重复刷新
            moveToTail(refreshKey, members);

            for (byte[] member : members) {
                if (member != null) {
                    if (j >= maximum) {
                        j = 0;
                        futures = new Future<?>[maximum];
                        tasksList.add(Tuples.of(futures, 0));
                    }

                    String key = stringCodec.decode(member);
                    futures[j++] = executor.submit(new RefreshTask(this, key, consumer, predicate));
                    i++;
                }
            }

            if (i + maximum >= maxRefreshTasks) {
                break;
            }
        }
    }

}