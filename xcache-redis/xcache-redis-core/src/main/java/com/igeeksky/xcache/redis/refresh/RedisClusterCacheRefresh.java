package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.redis.CRC16;
import com.igeeksky.redis.RedisOperator;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshTask;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.function.tuple.Tuples;
import com.igeeksky.xtool.core.lang.IntegerValue;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

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

    private static final int MINIMUM_KEY_SEQ_SIZE = 32;
    private static final int MAXIMUM_KEY_SEQ_SIZE = 16384;

    private final int mask;
    private final int refreshSequenceSize;

    // 集群模式下，用以分散刷新键到不同节点，避免数据倾斜。
    private final byte[][] refreshKeys;

    public RedisClusterCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler,
                                    ExecutorService executor, RedisOperator operator) {
        super(config, scheduler, executor, operator);
        this.refreshSequenceSize = keySequenceSizeFor(config.getRefreshSequenceSize());
        this.mask = this.refreshSequenceSize - 1;
        this.refreshKeys = initRefreshKeys(this.stringCodec, config.getRefreshKey(), this.refreshSequenceSize);
    }

    @Override
    public void doPut(String key) {
        byte[] member = stringCodec.encode(key);
        this.put(new byte[][]{selectRefreshKey(member)}, new byte[][]{refreshAfterWrite, member});
    }

    @Override
    public void doPutAll(Set<String> keys) {
        Map<byte[], List<byte[]>> map = Maps.newHashMap(calculateCapacity(keys.size()));
        for (String key : keys) {
            byte[] member = stringCodec.encode(key);
            List<byte[]> values = map.computeIfAbsent(selectRefreshKey(member), k -> {
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
        this.remove(new byte[][]{selectRefreshKey(member)}, new byte[][]{member});
    }

    @Override
    public void doRemoveAll(Set<String> keys) {
        Map<byte[], List<byte[]>> map = Maps.newHashMap(calculateCapacity(keys.size()));
        for (String key : keys) {
            byte[] member = stringCodec.encode(key);
            List<byte[]> values = map.computeIfAbsent(selectRefreshKey(member), k -> new ArrayList<>());
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
        // Future<?>[] 数组的当前写入下标
        final IntegerValue index = new IntegerValue();
        // 已提交的总任务数
        final IntegerValue total = new IntegerValue();
        // 最大任务数，当 maximumTasksSize 太小或当前待刷新的键过多，避免序列尾部的键集始终无法刷新
        final int maximum = Math.max(refreshKeys.length, refreshTasksSize);
        // maximum / refreshKeys.length，确保每个 SortedSet 都能刷新至少 count 个元素
        final int count = maximum / refreshKeys.length;
        // Future<?>[] 数组长度
        final int length = Math.min(FUTURES_ARRAY_LENGTH, maximum);

        this.tasksList.add(Tuples.of(new Future<?>[length], 0));
        byte[][] keys = refreshKeys;
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

    /**
     * 预估容量
     * <p>
     * 当传入键集时，根据键数量预估合适容量。
     *
     * @param size 键数量，用以预估容量
     * @return 预估容量值
     */
    private int calculateCapacity(int size) {
        // 如果给定大小小于或等于最小键序列大小，则直接返回该大小
        if (size <= MINIMUM_KEY_SEQ_SIZE) {
            return size;
        }
        // 多个键可能会分布于同一个 SortedSet，因此将键数量除以 2，避免容量过大浪费内存
        return Math.max(MINIMUM_KEY_SEQ_SIZE, Math.min(refreshSequenceSize, size >>> 1));
    }

    private byte[] selectRefreshKey(byte[] member) {
        return refreshKeys[CRC16.crc16(member) & mask];
    }

    /**
     * 初始化顺序表名
     * <p/>
     * 仅集群模式时使用，用于将键和值分散到不同的节点。
     *
     * @return 顺序表名
     */
    private static byte[][] initRefreshKeys(StringCodec codec, String refreshKey, int size) {
        byte[][] keys = new byte[size][];
        for (int i = 0; i < size; i++) {
            keys[i] = codec.encode(refreshKey + ":" + i);
        }
        return keys;
    }

    private static int keySequenceSizeFor(int cap) {
        if (cap <= MINIMUM_KEY_SEQ_SIZE) {
            return MINIMUM_KEY_SEQ_SIZE;
        }
        if (cap >= MAXIMUM_KEY_SEQ_SIZE) {
            return MAXIMUM_KEY_SEQ_SIZE;
        }
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n <= MINIMUM_KEY_SEQ_SIZE) ? MINIMUM_KEY_SEQ_SIZE : (n >= MAXIMUM_KEY_SEQ_SIZE) ? MAXIMUM_KEY_SEQ_SIZE : n + 1;
    }

}