package com.igeeksky.xcache.extension;

import java.util.concurrent.Future;

/**
 * 任务完成信息
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class TasksInfo {

    private volatile int start = 0;

    private final Future<?>[] futures;

    /**
     * 构造任务完成信息
     *
     * @param futures 任务列表
     */
    public TasksInfo(Future<?>[] futures) {
        this.futures = futures;
    }

    /**
     * 获取任务列表
     *
     * @return 任务列表
     */
    public Future<?>[] getFutures() {
        return futures;
    }

    /**
     * 获取任务尚未完成的起始位置
     *
     * @return 任务尚未完成的起始位置
     */
    public int getStart() {
        return start;
    }

    /**
     * 设置任务尚未完成的起始位置
     *
     * @param start 任务尚未完成的起始位置
     */
    public void setStart(int start) {
        this.start = start;
    }

}
