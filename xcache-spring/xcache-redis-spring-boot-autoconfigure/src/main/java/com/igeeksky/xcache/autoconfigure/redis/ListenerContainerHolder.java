package com.igeeksky.xcache.autoconfigure.redis;

import com.igeeksky.redis.stream.StreamListenerContainer;
import com.igeeksky.xcache.autoconfigure.holder.Holder;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/25
 */
public class ListenerContainerHolder implements Holder<StreamListenerContainer> {

    private final Map<String, StreamListenerContainer> map = new HashMap<>();

    @Override
    public void put(String beanId, StreamListenerContainer container) {
        StreamListenerContainer old = map.put(beanId, container);
        Assert.isTrue(old == null, "StreamListenerContainer:[" + beanId + "]: duplicate id.");
    }

    @Override
    public StreamListenerContainer get(String id) {
        return map.get(id);
    }

    @Override
    public Map<String, StreamListenerContainer> getAll() {
        return Collections.unmodifiableMap(map);
    }

}
