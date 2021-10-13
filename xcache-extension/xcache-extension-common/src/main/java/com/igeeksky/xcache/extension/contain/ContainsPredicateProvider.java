package com.igeeksky.xcache.extension.contain;

import com.igeeksky.xcache.common.Provider;
import com.igeeksky.xcache.common.SPI;

/**
 * 判断数据源是否包含此key对应的value，工厂类
 *
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-26
 */
@SPI
public interface ContainsPredicateProvider extends Provider {

    <K> ContainsPredicate<K> get(Class<K> keyType);

}
