package com.igeeksky.xcache.caffeine;

import com.github.benmanes.caffeine.cache.Weigher;
import com.igeeksky.xcache.common.CacheValue;

/**
 * 基于权重的驱逐策略提供者
 * <p>
 * 仅适用于 Caffeine，当通过配置已经无法满足个性化需求，可以通过继承此接口，返回自定义的时间策略。
 * <p>
 * <b>注意</b>：此接口实现类的 bean 实例需在 {@code com.igeeksky.xcache.autoconfigure.caffeine.CaffeineAutoConfiguration}
 * 之前注入 Spring 容器。
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
public interface CaffeineWeigherProvider {

    Weigher<String, CacheValue<Object>> get(String name);

}
