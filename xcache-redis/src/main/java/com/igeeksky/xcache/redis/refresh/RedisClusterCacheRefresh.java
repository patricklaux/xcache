package com.igeeksky.xcache.redis.refresh;

import com.igeeksky.xcache.extension.TasksInfo;
import com.igeeksky.xcache.extension.refresh.RefreshConfig;
import com.igeeksky.xcache.extension.refresh.RefreshTask;
import com.igeeksky.xcache.redis.RedisClusterHelper;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.collection.CollectionUtils;

import java.util.*;
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

    private final ExecutorService executor;
    private final RedisClusterHelper clusterHelper;

    public RedisClusterCacheRefresh(RefreshConfig config, ScheduledExecutorService scheduler,
                                    ExecutorService executor, RedisOperatorProxy operator) {
        super(config, scheduler, operator);
        this.executor = executor;
        int slotSize = config.getRefreshSlotSize();
        String refreshKey = config.getRefreshKey();
        this.clusterHelper = new RedisClusterHelper(slotSize, refreshKey, this.stringCodec);
    }

    @Override
    public void onPut(String key) {
        byte[] member = stringCodec.encode(key);
        byte[] refreshKey = clusterHelper.selectSlot(member);
        this.put(new byte[][]{refreshKey}, new byte[][]{refreshAfterWrite, member});
    }

    @Override
    public void onPutAll(Set<String> keys) {
        Map<byte[], List<byte[]>> map = HashMap.newHashMap(clusterHelper.calculateCapacity(keys.size()));
        for (String key : keys) {
            byte[] member = stringCodec.encode(key);
            byte[] refreshKey = clusterHelper.selectSlot(member);
            map.computeIfAbsent(refreshKey, k -> {
                List<byte[]> list = new ArrayList<>();
                list.add(refreshAfterWrite);
                return list;
            }).add(member);
        }
        for (Map.Entry<byte[], List<byte[]>> entry : map.entrySet()) {
            List<byte[]> refreshArgs = entry.getValue();
            this.put(new byte[][]{entry.getKey()}, refreshArgs.toArray(new byte[refreshArgs.size()][]));
        }
    }

    @Override
    public void onRemove(String key) {
        byte[] member = stringCodec.encode(key);
        byte[] refreshKey = clusterHelper.selectSlot(member);
        this.remove(refreshKey, member);
    }

    @Override
    public void onRemoveAll(Set<String> keys) {
        Map<byte[], List<byte[]>> map = HashMap.newHashMap(clusterHelper.calculateCapacity(keys.size()));
        for (String key : keys) {
            byte[] member = stringCodec.encode(key);
            byte[] refreshKey = clusterHelper.selectSlot(member);
            map.computeIfAbsent(refreshKey, k -> new ArrayList<>()).add(member);
        }
        for (Map.Entry<byte[], List<byte[]>> entry : map.entrySet()) {
            List<byte[]> values = entry.getValue();
            this.remove(entry.getKey(), values.toArray(new byte[values.size()][]));
        }
    }

    protected void doRefresh() {
        if (isShutdown()) {
            return;
        }
        // SortedSet 键序列
        final List<byte[]> refreshKeys = new LinkedList<>(clusterHelper.getSlotsList());
        // 最大任务数，大于等于键的数量，确保每个 SortedSet 至少能刷新 1 个元素（避免 refreshTasksSize 设定过小）
        final int maximum = Math.max(refreshKeys.size(), config.getRefreshTaskSize());
        // maximum / keysSize，确保每个 SortedSet 都能刷新至少 count 个元素
        final int count = Math.min(FUTURES_LENGTH, maximum / refreshKeys.size());
        // futures 数组长度
        final int length = Math.min(FUTURES_LENGTH, maximum);

        // index：futures 当前可写位置；total：已提交的总任务数
        int index = 0, total = 0;
        Future<?>[] futures = new Future<?>[length];
        this.tasksQueue.add(new TasksInfo(futures));

        // 两层嵌套循环，目的是为了确保每一个 SortedSet 都能至少刷新一次
        // 外层循环：每一轮刷新一遍 refreshKeys，当一轮刷新过后，仍有 refreshKey 包含待刷新的成员集合，则再刷新一轮
        // 内层循环：循环刷新每一个 refreshKey，每一个 SortedSet 每次最多刷新 count 个元素
        while (!isShutdown() && total < maximum && !refreshKeys.isEmpty()) {
            Iterator<byte[]> iterator = refreshKeys.iterator();
            while (!isShutdown() && total < maximum && iterator.hasNext()) {
                byte[] refreshKey = iterator.next();
                // 1. 获取当前需刷新的成员集合
                List<byte[]> members = this.getRefreshMembersAndUpdateTime(new byte[][]{refreshKey}, count);
                // 2. 如果成员集合为空 或 数量小于 count，说明该 SortedSet 此刻已无需刷新的键，移除该 SortedSet
                if (CollectionUtils.isEmpty(members)) {
                    iterator.remove();
                    continue;
                }
                if (members.size() < count) {
                    iterator.remove();
                }
                // 3. 循环刷新每一个键
                for (byte[] member : members) {
                    if (member != null) {
                        if (index >= length) {
                            index = 0;
                            futures = new Future<?>[length];
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

}
