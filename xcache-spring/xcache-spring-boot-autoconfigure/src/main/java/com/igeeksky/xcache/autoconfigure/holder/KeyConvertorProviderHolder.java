package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.convertor.KeyConvertorProvider;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-08
 */
public class KeyConvertorProviderHolder implements Holder<KeyConvertorProvider> {

    private final Map<String, KeyConvertorProvider> map = new HashMap<>();

    @Override
    public void put(String beanId, KeyConvertorProvider provider) {
        Assert.hasText(beanId, () -> "KeyConvertorProvider: beanId must not be null or empty.");
        Assert.notNull(provider, () -> "KeyConvertorProvider: bean must not be null.");
        map.put(StringUtils.trim(beanId), provider);
    }

    @Override
    public KeyConvertorProvider get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, KeyConvertorProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }
}
