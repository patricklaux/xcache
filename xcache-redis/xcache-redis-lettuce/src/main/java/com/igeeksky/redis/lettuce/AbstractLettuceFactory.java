package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.redis.stream.RedisStreamOperator;
import com.igeeksky.xtool.core.io.IOUtils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/21
 */
public abstract class AbstractLettuceFactory implements RedisOperatorFactory {

    private final Lock lock = new ReentrantLock();

    private volatile RedisStreamOperator streamOperator;
    private volatile AbstractLettuceOperator lettuceOperator;

    @Override
    public RedisOperator getRedisOperator() {
        if (this.lettuceOperator == null) {
            this.lock.lock();
            try {
                if (this.lettuceOperator == null) {
                    this.lettuceOperator = this.createOperator();
                }
            } finally {
                this.lock.unlock();
            }
        }
        return this.lettuceOperator;
    }

    @Override
    public RedisStreamOperator getRedisStreamOperator() {
        if (this.streamOperator == null) {
            this.lock.lock();
            try {
                if (this.streamOperator == null) {
                    this.streamOperator = this.createStreamOperator();
                }
            } finally {
                this.lock.unlock();
            }
        }
        return this.streamOperator;
    }

    protected abstract AbstractLettuceOperator createOperator();

    protected abstract LettuceStreamOperator createStreamOperator();

    @Override
    public void close() {
        IOUtils.closeQuietly(lettuceOperator, streamOperator);
    }

}