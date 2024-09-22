package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.extension.codec.CodecProvider;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于向 CacheManager 注册 CodecProvider
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-08
 */
public class CodecProviderHolder implements Holder<CodecProvider> {

    private final Map<String, CodecProvider> map = new HashMap<>();

    /**
     * 默认构造函数
     * <p>
     * 对象初始化时内部会自动创建一个 map ，用于存放 CodecProvider 对象
     */
    public CodecProviderHolder() {
    }

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
