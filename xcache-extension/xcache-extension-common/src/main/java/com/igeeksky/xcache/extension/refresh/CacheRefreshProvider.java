package com.igeeksky.xcache.extension.refresh;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/28
 */
public interface CacheRefreshProvider {

    CacheRefresh getCacheRefresh(RefreshConfig config);

}