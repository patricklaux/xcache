package com.igeeksky.xcache.beans;

import com.igeeksky.xcache.common.annotation.NotNull;
import com.igeeksky.xcache.common.annotation.Nullable;
import com.igeeksky.xcache.config.CacheConfigException;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * <b>缓存扩展初始化</b>
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-23
 */
public class BeanContext {

    private static final Logger log = LoggerFactory.getLogger(BeanContext.class);

    /**
     * <b>Step 1. 根据用户配置生成扩展的 BeanHolder；</b><br/>
     * <b>Step 2. 预生成常用扩展的 BeanHolder，避免用户重复配置和反射。</b><br/>
     * 如果 Bean id 被用户配置，用户配置覆盖预生成的 BeanHolder。<br/>
     * <b>预生成Key如下：</b><br/>
     * key = localCacheLockProvider <br/>
     * key = gzipCompressorProvider <br/>
     * key = jdkSerializerProvider <br/>
     * key = jacksonSerializerProvider（只有已引入xcache-serialization-jackson包才会生成） <br/>
     * key = stringSerializerProvider <br/>
     * key = alwaysTruePredicateBeanParser <br/>
     *
     * @param beans 用户配置
     */
    public BeanContext(@Nullable List<BeanDesc> beans, @Nullable Map<String, BeanParser> beanParsers) {
        if (Maps.isNotEmpty(beanParsers)) {
            this.beanParsers.putAll(beanParsers);
        }

        if (CollectionUtils.isNotEmpty(beans)) {
            parseBeans(beans);
        }
    }

    private final Map<String, BeanHolder> first = new ConcurrentHashMap<>(32);
    private final Map<String, BeanHolder> second = new ConcurrentHashMap<>(16);

    private final Map<String, BeanParser> beanParsers = new ConcurrentHashMap<>(16);

    public BeanHolder getBeanHolder(String id) {
        return first.get(id);
    }

    private void preset(@NotNull BeanParser beanParser) {
        String className = beanParser.getClassName();
        beanParsers.putIfAbsent(className, beanParser);

        BeanHolder beanHolder = beanParser.parse(new BeanDesc(className));
        first.putIfAbsent(beanHolder.getId(), beanHolder);
    }

    private void parseBeans(List<BeanDesc> beans) {
        if (CollectionUtils.isEmpty(beans)) {
            return;
        }
        for (BeanDesc beanDesc : beans) {
            if (null != beanDesc) {
                parseBean(beanDesc);
            }
        }
    }

    private void parseBean(@NotNull BeanDesc beanDesc) {
        String id = StringUtils.trimToNull(beanDesc.getId());
        if (null == id) {
            throw new CacheConfigException("id must not be null");
        }

        boolean singleton = beanDesc.isSingleton();
        String className = StringUtils.trimToNull(beanDesc.getClassName());

        //Step 1: 根据 Supplier 生成 BeanHolder
        Supplier<Object> supplier = beanDesc.getSupplier();
        if (null != supplier) {
            BeanHolder beanHolder = new BeanHolder(id, className, singleton, supplier);
            first.put(id, beanHolder);
            return;
        }

        //Step 2：使用预定义的Parser生成 BeanHolder
        if (null == className) {
            throw new CacheConfigException("Either className or instance must not be null");
        }
        BeanParser beanParser = beanParsers.get(className);
        if (null != beanParser) {
            first.put(id, beanParser.parse(beanDesc));
            return;
        }

        //Step 3: TODO 使用反射生成 BeanHolder

    }

    public BeanHolder putIfAbsent(String id, BeanHolder beanHolder) {
        return first.putIfAbsent(id, beanHolder);
    }
}
