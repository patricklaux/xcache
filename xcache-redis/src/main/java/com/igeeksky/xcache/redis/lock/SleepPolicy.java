package com.igeeksky.xcache.redis.lock;

/**
 * 休眠策略
 */
public class SleepPolicy {

    /**
     * 执行次数
     */
    private int times = 0;

    /**
     * 单位时间
     */
    private final long unitTime;

    /**
     * 加锁命令单次处理耗时
     */
    private final long usedTime;

    public SleepPolicy(long usedTime) {
        this.usedTime = usedTime;
        this.unitTime = Math.min(20, Math.max(5, usedTime / 2));
    }

    public SleepPolicy(long waitTime, long usedTime) {
        this.usedTime = usedTime;
        this.unitTime = Math.min(waitTime, Math.min(20, Math.max(5, Math.max(waitTime / 50, usedTime / 2))));
    }

    /**
     * @param ttl 锁剩余存活时间
     * @return 休眠时间
     */
    public long getNext(long ttl) {
        if (++times > 5) {
            times = 5;
        }
        return Math.min(unitTime * times, Math.max(ttl - (usedTime / 2), 1));
    }

}