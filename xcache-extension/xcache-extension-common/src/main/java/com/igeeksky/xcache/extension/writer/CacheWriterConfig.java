package com.igeeksky.xcache.extension.writer;

import com.igeeksky.xcache.common.CacheWriter;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/2
 */
public class CacheWriterConfig<K, V> {

    private int queueSize;

    private int maxDelay;

    private int concurrency;

    /**
     * 最大重试次数
     */
    private int attempts;

    /**
     * 批量写入数量
     */
    private int batchSize;

    private CacheWriteStrategy strategy;

    private CacheWriter<K, V> writer;

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    public CacheWriteStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(CacheWriteStrategy strategy) {
        this.strategy = strategy;
    }

    public CacheWriter<K, V> getWriter() {
        return writer;
    }

    public void setWriter(CacheWriter<K, V> writer) {
        this.writer = writer;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

}