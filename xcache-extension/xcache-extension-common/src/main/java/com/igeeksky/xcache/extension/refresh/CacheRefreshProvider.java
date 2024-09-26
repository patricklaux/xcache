package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xcache.common.CacheRefresh;

/**
 * 缓存刷新器工厂接口
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/28
 */
public interface CacheRefreshProvider {

    /**
     * 获取缓存刷新器
     * <p>
     * 根据缓存名称及其它配置信息，创建并返回缓存刷新器
     *
     * @param config 缓存刷新配置
     * @return {@code CacheRefresh} - 缓存刷新器
     */
    CacheRefresh getCacheRefresh(RefreshConfig config);

}