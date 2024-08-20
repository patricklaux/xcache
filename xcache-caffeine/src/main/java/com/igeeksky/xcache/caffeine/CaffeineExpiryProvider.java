package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Expiry;
import com.igeeksky.xcache.core.CacheValue;

/**
 * <b>基于时间的驱逐策略提供者</b>
 * <p>
 * 仅适用于 Caffeine，当通过配置已经无法满足个性化需求，可以通过继承此接口，返回自定义的时间策略。
 * <p>
 * <b>注意</b>：实现类的 bean 实例需在 CaffeineAutoConfiguration 之前注入 Spring 容器。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public interface CaffeineExpiryProvider {

    /**
     * 根据缓存名称获取存活时间策略
     *
     * @param name 缓存名称
     * @return 自定义的基于时间的驱逐策略
     */
    Expiry<String, CacheValue<Object>> get(String name);

}
