package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshTask;
import com.igeeksky.xredis.common.RedisFutureHelper;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.tuple.Tuples;

import java.util.ArrayList;
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

    private final byte[] refreshKey;

    public RedisCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler, ExecutorService executor,
                             RedisOperatorProxy operator, long batchTimeout) {
        super(config, scheduler, executor, operator, batchTimeout);
        this.refreshKey = this.stringCodec.encode(config.getRefreshKey());
    }

    @Override
    public void onPut(String key) {
        this.put(refreshKey, stringCodec.encode(key));
    }

    @Override
    public void onPutAll(Set<String> keys) {
        List<byte[]> members = new ArrayList<>(keys.size());
        for (String key : keys) {
            members.add(stringCodec.encode(key));
        }
        this.put(refreshKey, members);
    }

    @Override
    public void onRemove(String key) {
        this.remove(refreshKey, stringCodec.encode(key));
    }

    @Override
    public void onRemoveAll(Set<String> keys) {
        int i = 0;
        byte[][] members = new byte[keys.size()][];
        for (String key : keys) {
            members[i++] = stringCodec.encode(key);
        }
        this.remove(refreshKey, members);
    }

    protected void doRefresh() {
        long now = this.getServerTime();
        int total = 0, index = 0, count = Math.min(FUTURES_LENGTH, refreshTasksSize);

        Future<?>[] futures = new Future<?>[count];
        tasksList.add(Tuples.of(futures, 0));

        while (total < refreshTasksSize) {
            // 1. 获取当前需刷新的键集
            List<byte[]> members = this.getRefreshMembers(refreshKey, now, count);
            if (CollectionUtils.isEmpty(members)) {
                break;
            }
            // 2. 异步循环刷新键集
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
            // 3. 同步更新刷新时间，避免下次循环重复刷新（移动到队尾）
            RedisFutureHelper.get(this.put(refreshKey, members), batchTimeout);
        }
    }

}