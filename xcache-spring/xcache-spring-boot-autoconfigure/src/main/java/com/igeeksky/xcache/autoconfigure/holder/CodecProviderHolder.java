package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-08
 */
public class CodecProviderHolder implements Holder<CodecProvider> {

    private final Map<String, CodecProvider> map = new HashMap<>();

    @Override
    public void put(String beanId, CodecProvider provider) {
        CodecProvider old = map.put(beanId, provider);
        Assert.isTrue(old == null, () -> "CodecProvider: [" + beanId + "] duplicate id.");
    }

    @Override
    public CodecProvider get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, CodecProvider> getAll() {
        return Collections.unmodifiableMap(map);
    }
}
