package com.igeeksky.redis.lettuce;

import com.igeeksky.redis.RedisOperator;
import com.igeeksky.redis.RedisOperatorFactory;
import com.igeeksky.redis.stream.RedisStreamOperator;
import com.igeeksky.xtool.core.io.IOUtils;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lettuce 抽象工厂
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/21
 */
public sealed abstract class AbstractLettuceFactory implements RedisOperatorFactory
        permits LettuceStandaloneFactory, LettuceSentinelFactory, LettuceClusterFactory {

    private final Lock lock = new ReentrantLock();

    private volatile RedisStreamOperator streamOperator;
    private volatile AbstractLettuceOperator lettuceOperator;

    /**
     * 默认构造函数
     */
    protected AbstractLettuceFactory() {
    }

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

    /**
     * 创建 LettuceOperator
     *
     * @return {@link AbstractLettuceOperator} Lettuce客户端
     */
    protected abstract AbstractLettuceOperator createOperator();

    /**
     * 创建 LettuceStreamOperator
     *
     * @return {@link LettuceStreamOperator} Lettuce客户端
     */
    protected abstract LettuceStreamOperator createStreamOperator();

    @Override
    public void close() {
        IOUtils.closeQuietly(lettuceOperator, streamOperator);
    }

}