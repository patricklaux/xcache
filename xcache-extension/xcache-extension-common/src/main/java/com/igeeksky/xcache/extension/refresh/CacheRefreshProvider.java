package com.igeeksky.xcache.extension.refresh;

import com.igeeksky.xcache.common.CacheRefresh;

/**
 * 缓存刷新器工厂接口
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/28
 */
public interface CacheRefreshProvider {

    CacheRefresh getCacheRefresh(RefreshConfig config);

}