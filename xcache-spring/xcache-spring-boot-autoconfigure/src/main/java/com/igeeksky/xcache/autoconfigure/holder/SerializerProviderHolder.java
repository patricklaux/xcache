package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.serializer.SerializerProvider;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-08
 */
public class SerializerProviderHolder implements Holder<SerializerProvider> {

    private final Map<String, SerializerProvider> map = new HashMap<>();

    @Override
    public void put(String beanId, SerializerProvider bean) {
        Assert.hasText(beanId, () -> "SerializerProvider: beanId must not be null or empty.");
        Assert.notNull(bean, () -> "SerializerProvider: bean must not be null.");
        map.put(StringUtils.trim(beanId), bean);
    }

    @Override
    public SerializerProvider get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, SerializerProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }
}
