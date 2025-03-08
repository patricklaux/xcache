package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshTask;
import com.igeeksky.xcache.extension.TasksInfo;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.collection.CollectionUtils;

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
    private final byte[][] refreshKeys;

    private final ExecutorService executor;

    public RedisCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler, ExecutorService executor,
                             RedisOperatorProxy operator) {
        super(config, scheduler, operator);
        this.executor = executor;
        this.refreshKey = this.stringCodec.encode(config.getRefreshKey());
        this.refreshKeys = new byte[][]{refreshKey};
    }

    @Override
    public void onPut(String key) {
        this.put(refreshKeys, new byte[][]{refreshAfterWrite, stringCodec.encode(key)});
    }

    @Override
    public void onPutAll(Set<String> keys) {
        List<byte[]> refreshArgs = new ArrayList<>(keys.size());
        refreshArgs.add(refreshAfterWrite);
        for (String key : keys) {
            refreshArgs.add(stringCodec.encode(key));
        }
        int size = refreshArgs.size();
        this.put(refreshKeys, refreshArgs.toArray(new byte[size][]));
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
        if (isShutdown()) {
            return;
        }
        int refreshTasksSize = config.getRefreshTasksSize();
        int total = 0, index = 0, count = Math.min(FUTURES_LENGTH, refreshTasksSize);

        Future<?>[] futures = new Future<?>[count];
        tasksQueue.add(new TasksInfo(futures));

        while (!isShutdown() && total < refreshTasksSize) {
            // 1. 获取当前需刷新的键集
            List<byte[]> members = this.getRefreshMembersAndUpdateTime(refreshKeys, count);
            if (CollectionUtils.isEmpty(members)) {
                break;
            }
            // 2. 异步循环刷新键集
            for (byte[] member : members) {
                if (member != null) {
                    if (index >= count) {
                        index = 0;
                        futures = new Future<?>[count];
                        tasksQueue.add(new TasksInfo(futures));
                    }
                    RefreshTask task = new RefreshTask(this, stringCodec.decode(member), consumer, predicate);
                    futures[index++] = executor.submit(task);
                    total++;
                }
            }
        }
    }

}