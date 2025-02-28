package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.xcache.autoconfigure.register.Register;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public class LettuceRegister implements Register<LettuceHolder>, AutoCloseable {

    private final Map<String, LettuceHolder> map = new HashMap<>();

    @Override
    public void put(String beanId, LettuceHolder holder) {
        LettuceHolder old = map.put(beanId, holder);
        Assert.isTrue(old == null, () -> "RedisOperatorHolder: [" + beanId + "] duplicate id.");
    }

    @Override
    public LettuceHolder get(String beanId) {
        return map.get(beanId);
    }

    @Override
    public Map<String, LettuceHolder> getAll() {
        return Collections.unmodifiableMap(map);
    }

    @Override
    public void close() {
        map.forEach((k, holder) -> {
            try {
                holder.shutdown();
            } catch (Exception ignored) {
            }
        });
    }

}
