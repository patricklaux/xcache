package com.igeeksky.xcache.extension.contains;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.config.props.CacheProps;

/**
 * 判断数据源是否包含此key对应的value，工厂类
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
public interface ContainsPredicateProvider extends Provider {

    <K> ContainsPredicate<K> get(Class<K> keyType, CacheProps cacheProps);

}
