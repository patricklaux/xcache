package com.igeeksky.xcache.extension.lock;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class EmbedLock extends KeyLock {

    /**
     * 申请锁的次数（当计数为 0 时从 map 中删除锁）
     */
    private final AtomicInteger count = new AtomicInteger(1);

    /**
     * 内部锁
     */
    private final ReentrantLock innerLock = new ReentrantLock();

    public EmbedLock(String key) {
        super(key);
    }

    protected int decrementAndGet() {
        return count.decrementAndGet();
    }

    protected void increment() {
        count.incrementAndGet();
    }

    @Override
    public void lock() {
        innerLock.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        innerLock.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return innerLock.tryLock();
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return innerLock.tryLock(time, unit);
    }

    @Override
    public void unlock() {
        innerLock.unlock();
    }

    @Override
    public Condition newCondition() {
        return innerLock.newCondition();
    }

}