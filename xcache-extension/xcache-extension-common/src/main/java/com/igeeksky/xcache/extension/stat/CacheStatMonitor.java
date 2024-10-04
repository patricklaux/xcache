package com.igeeksky.xcache.extension.stat;

import com.igeeksky.xcache.props.StoreLevel;

/**
 * 缓存指标监控
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/9/9
 */
public interface CacheStatMonitor {

    /**
     * 增加命中次数
     *
     * @param level 缓存级别
     * @param times 命中次数
     */
    void incHits(StoreLevel level, long times);

    /**
     * 增加未命中次数
     *
     * @param level 缓存级别
     * @param times 未命中次数
     */
    void incMisses(StoreLevel level, long times);

    /**
     * 增加缓存写入次数
     *
     * @param level 缓存级别
     * @param times 写入次数
     */
    void incPuts(StoreLevel level, long times);

    /**
     * 增加缓存回源加载成功次数（值不为空）
     *
     * @param times 回源加载取值成功次数
     */
    void incHitLoads(long times);

    /**
     * 增加缓存回源加载失败次数（值为空）
     *
     * @param times 回源加载取值失败次数
     */
    void incMissLoads(long times);

    /**
     * 增加缓存删除次数
     *
     * @param level 缓存级别
     * @param times 删除次数
     */
    void incRemovals(StoreLevel level, long times);

    /**
     * 增加缓存清空次数
     *
     * @param level 缓存级别
     */
    void incClears(StoreLevel level);

    /**
     * 设置需统计的缓存级别
     *
     * @param level 缓存级别
     */
    void setCounter(StoreLevel level);

    /**
     * 采集指标信息
     *
     * @return 指标信息
     */
    CacheStatMessage collect();

}
