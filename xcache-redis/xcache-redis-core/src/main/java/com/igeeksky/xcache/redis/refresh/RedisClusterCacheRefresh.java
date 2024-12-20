package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshTask;
import com.igeeksky.xcache.redis.RedisClusterHelper;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.function.tuple.Tuples;
import com.igeeksky.xtool.core.lang.IntegerValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Redis 缓存刷新（集群模式）
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisClusterCacheRefresh extends AbstractRedisCacheRefresh {

    private final RedisClusterHelper clusterHelper;

    public RedisClusterCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler,
                                    ExecutorService executor, RedisOperator operator) {
        super(config, scheduler, executor, operator);
        int sequenceSize = config.getRefreshSequenceSize();
        this.clusterHelper = new RedisClusterHelper(sequenceSize, config.getRefreshKey(), this.stringCodec);
    }

    @Override
    public void doPut(String key) {
        byte[] member = stringCodec.encode(key);
        this.put(new byte[][]{clusterHelper.selectKey(member)}, new byte[][]{refreshAfterWrite, member});
    }

    @Override
    public void doPutAll(Set<String> keys) {
        Map<byte[], List<byte[]>> map = Maps.newHashMap(clusterHelper.calculateCapacity(keys.size()));
        for (String key : keys) {
            byte[] member = stringCodec.encode(key);
            List<byte[]> values = map.computeIfAbsent(clusterHelper.selectKey(member), k -> {
                List<byte[]> list = new ArrayList<>();
                list.add(refreshAfterWrite);
                return list;
            });
            values.add(member);
        }
        for (Map.Entry<byte[], List<byte[]>> entry : map.entrySet()) {
            byte[] refreshKey = entry.getKey();
            List<byte[]> values = entry.getValue();
            this.put(new byte[][]{refreshKey}, values.toArray(new byte[0][]));
        }
    }

    @Override
    public void doRemove(String key) {
        byte[] member = stringCodec.encode(key);
        this.remove(new byte[][]{clusterHelper.selectKey(member)}, new byte[][]{member});
    }

    @Override
    public void doRemoveAll(Set<String> keys) {
        Map<byte[], List<byte[]>> map = Maps.newHashMap(clusterHelper.calculateCapacity(keys.size()));
        for (String key : keys) {
            byte[] member = stringCodec.encode(key);
            List<byte[]> values = map.computeIfAbsent(clusterHelper.selectKey(member), k -> new ArrayList<>());
            values.add(member);
        }
        for (Map.Entry<byte[], List<byte[]>> entry : map.entrySet()) {
            byte[] refreshKey = entry.getKey();
            List<byte[]> values = entry.getValue();
            this.remove(new byte[][]{refreshKey}, values.toArray(new byte[0][]));
        }
    }

    protected void refresh() {
        final long now = this.getServerTime();
        // Future<?>[] 数组的当前可写入位置
        final IntegerValue index = new IntegerValue();
        // 已提交的总任务数
        final IntegerValue total = new IntegerValue();
        // 键的数量
        final int keysSize = clusterHelper.getSize();
        // 最大任务数，大于等于键的数量，避免 refreshTasksSize 太小导致键序列末尾的数据始终无法刷新
        final int maximum = Math.max(keysSize, refreshTasksSize);
        // maximum / keysSize，确保每个 SortedSet 都能刷新至少 count 个元素
        final int count = maximum / keysSize;
        // Future<?>[] 数组长度
        final int length = Math.min(FUTURES_LENGTH, maximum);

        this.tasksList.add(Tuples.of(new Future<?>[length], 0));
        byte[][] keys = clusterHelper.getKeys();
        while (total.get() < maximum) {
            List<byte[]> unfinishedSets = this.refresh(keys, now, count, length, maximum, total, index);
            if (unfinishedSets.isEmpty()) {
                break;
            }
            keys = unfinishedSets.toArray(new byte[0][]);
        }
    }

    /**
     * 刷新任务提交
     *
     * @param keys    刷新键
     * @param now     当前服务器时间戳
     * @param count   单次循环，每个 SortedSet 最多刷新 count 个 key
     * @param length  {@code Future<?>[]} 数组长度
     * @param maximum 最大任务数
     * @param total   已提交的总任务数
     * @param index   {@code Future<?>[]} 数组的当前写入下标
     */
    private List<byte[]> refresh(byte[][] keys, final long now, final int count, final int length, final int maximum,
                                 final IntegerValue total, final IntegerValue index) {
        Future<?>[] futures = tasksList.getLast().getT1();
        List<byte[]> unfinishedSets = new ArrayList<>();
        for (byte[] refreshKey : keys) {
            // 1. 获取当前需要刷新的键
            List<byte[]> members = this.getRefreshMembers(refreshKey, now, count);
            if (CollectionUtils.isEmpty(members)) {
                continue;
            }
            // 2. 判断是否需要继续刷新
            int size = members.size();
            if (size >= count) {
                unfinishedSets.add(refreshKey);
            }
            // 3. 循环刷新每一个键
            for (byte[] member : members) {
                if (member != null) {
                    if (index.get() >= length) {
                        index.set(0);
                        futures = new Future<?>[length];
                        tasksList.add(Tuples.of(futures, 0));
                    }
                    RefreshTask task = new RefreshTask(this, stringCodec.decode(member), consumer, predicate);
                    futures[index.getAndIncrement()] = executor.submit(task);
                    total.increment();
                }
            }
            // 4. 总任务数达到上限，退出循环
            if (total.get() >= maximum) {
                break;
            }
        }
        return unfinishedSets;
    }

}