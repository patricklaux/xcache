package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.CRC16;
import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.sorted.ScoredValue;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshTask;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.function.tuple.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/29
 */
public class RedisClusterCacheRefresh extends AbstractRedisCacheRefresh {

    // TODO 可配置
    private final int LENGTH = 16384;

    private final byte[][] refreshKeys;

    public RedisClusterCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler,
                                    ExecutorService executor, RedisOperator operator) {
        super(config, scheduler, executor, operator);
        this.refreshKeys = initRefreshKeys(config.getRefreshKey());
    }

    @Override
    public void onPut(String key) {
        byte[] member = stringCodec.encode(key);
        this.operator.zadd(selectRefreshKey(member), nextRefreshTime(), member);
    }

    @Override
    public void onPutAll(Set<String> keys) {
        long nextRefreshTime = nextRefreshTime();
        Map<byte[], List<ScoredValue>> map = Maps.newHashMap(computeCapacity(keys.size()));
        for (String key : keys) {
            byte[] member = stringCodec.encode(key);
            List<ScoredValue> values = map.computeIfAbsent(selectRefreshKey(member), k -> new ArrayList<>());
            values.add(ScoredValue.just(member, nextRefreshTime));
        }
        for (Map.Entry<byte[], List<ScoredValue>> entry : map.entrySet()) {
            byte[] refreshKey = entry.getKey();
            List<ScoredValue> values = entry.getValue();
            this.operator.zadd(refreshKey, values.toArray(new ScoredValue[0]));
        }
    }

    @Override
    public void onRemove(String key) {
        byte[] member = stringCodec.encode(key);
        this.operator.zrem(selectRefreshKey(member), member);
    }

    @Override
    public void onRemoveAll(Set<String> keys) {
        Map<byte[], List<byte[]>> map = Maps.newHashMap(computeCapacity(keys.size()));
        for (String key : keys) {
            byte[] member = stringCodec.encode(key);
            List<byte[]> values = map.computeIfAbsent(selectRefreshKey(member), k -> new ArrayList<>());
            values.add(member);
        }
        for (Map.Entry<byte[], List<byte[]>> entry : map.entrySet()) {
            byte[] refreshKey = entry.getKey();
            List<byte[]> values = entry.getValue();
            this.operator.zrem(refreshKey, values.toArray(new byte[0][]));
        }
    }

    protected void refreshNow() {
        long now = now();
        int i = 0, j = 0, maximum = Math.min(MAXIMUM, maxRefreshTasks);

        Future<?>[] futures = new Future<?>[maximum];
        this.tasksList.add(Tuples.of(futures, 0));

        for (byte[] refreshKey : refreshKeys) {
            if (i >= maxRefreshTasks) {
                break;
            }
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

    private int computeCapacity(int size) {
        return Math.min(LENGTH, size / 2);
    }

    private byte[] selectRefreshKey(byte[] member) {
        return refreshKeys[CRC16.crc16(member)];
    }

    /**
     * 初始化顺序表名
     * <p/>
     * 仅集群模式时使用，用于将键和值分散到不同的节点。
     *
     * @return 顺序表名
     */
    private byte[][] initRefreshKeys(String refreshKey) {
        byte[][] keys = new byte[LENGTH][];
        for (int i = 0; i < LENGTH; i++) {
            keys[i] = stringCodec.encode(refreshKey + ":" + i);
        }
        return keys;
    }

}