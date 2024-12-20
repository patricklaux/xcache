package com.igeeksky.xcache.redis;

import com.igeeksky.redis.CRC16;
import com.igeeksky.xtool.core.lang.codec.StringCodec;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/12/20
 */
public class RedisClusterHelper {

    private static final int MINIMUM_CAPACITY = 32;
    private static final int MAXIMUM_CAPACITY = 16384;

    private final int size;
    private final int mask;
    private final byte[][] table;

    public RedisClusterHelper(int capacity, String prefix, StringCodec codec) {
        this.size = tableSizeFor(capacity);
        this.mask = size - 1;
        this.table = initKeys(this.size, prefix, codec);
    }

    /**
     * 获取键序列大小
     *
     * @return 键序列大小
     */
    public int getSize() {
        return size;
    }

    /**
     * 获取键序列
     *
     * @return 键序列
     */
    public byte[][] getKeys() {
        return table;
    }

    /**
     * 选择键
     *
     * @param member 成员
     * @return 键
     */
    public byte[] selectKey(byte[] member) {
        return table[CRC16.crc16(member) & mask];
    }

    /**
     * 根据成员数量预估合适的 HashMap 容量。
     *
     * @param num 成员数量，用以预估容量
     * @return 预估容量值
     */
    public int calculateCapacity(int num) {
        // 如果给定大小小于或等于最小键序列大小，则直接返回该大小
        if (num <= MINIMUM_CAPACITY) {
            return num;
        }
        // 多个成员可能会分布于同一个键对应的集合容器，因此将成员数量除以 2，避免容量过大浪费内存
        return Math.max(MINIMUM_CAPACITY, Math.min(this.size, num >>> 1));
    }

    /**
     * 获取最接近的 2 的幂次方
     *
     * @param capacity 容量
     * @return 最接近的2的幂次方
     */
    private static int tableSizeFor(int capacity) {
        if (capacity <= MINIMUM_CAPACITY) {
            return MINIMUM_CAPACITY;
        }
        if (capacity >= MAXIMUM_CAPACITY) {
            return MAXIMUM_CAPACITY;
        }
        int n = -1 >>> Integer.numberOfLeadingZeros(capacity - 1);
        return (n <= MINIMUM_CAPACITY) ? MINIMUM_CAPACITY : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * 初始化键序列
     * <p/>
     * 仅集群模式时使用，用于将键和值分散到不同的节点。
     *
     * @return 顺序表名
     */
    private static byte[][] initKeys(int cap, String prefix, StringCodec codec) {
        byte[][] keys = new byte[cap][];
        for (int i = 0; i < cap; i++) {
            keys[i] = codec.encode(prefix + ":" + i);
        }
        return keys;
    }

}
