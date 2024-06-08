package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.compress.CompressorProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-02
 */
public class CompressorProviderHolder implements Holder<CompressorProvider> {

    private final Map<String, CompressorProvider> map = new HashMap<>();

    @Override
    public void put(String beanId, CompressorProvider provider) {
        map.put(beanId, provider);
    }

    @Override
    public CompressorProvider get(String beanId) {
        CompressorProvider provider = map.get(beanId);
        Assert.notNull(provider, "beanId:[" + beanId + "] CompressorProvider doesn't exit");
        return provider;
    }

    @Override
    public Map<String, CompressorProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
