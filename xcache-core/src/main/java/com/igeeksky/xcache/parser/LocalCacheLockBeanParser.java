package com.igeeksky.xcache.parser;

import com.igeeksky.xcache.beans.BeanParser;
import com.igeeksky.xcache.beans.BeanDesc;
import com.igeeksky.xcache.beans.BeanHolder;
import com.igeeksky.xcache.config.PropertiesKey;
import com.igeeksky.xcache.extension.lock.LocalCacheLockProvider;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-03
 */
public class LocalCacheLockBeanParser implements BeanParser {

    private final String finalClassName = "com.igeeksky.xcache.extension.lock.LocalCacheLockProvider";

    @Override
    public String getClassName() {
        return finalClassName;
    }

    @Override
    public BeanHolder parse(BeanDesc beanDesc) {
        String id = beanDesc.getId();
        if (null == id) {
            id = "localCacheLockProvider";
        }

        Map<String, Object> constructor = beanDesc.getConstructor();
        Integer lockSize = PropertiesKey.getInteger(constructor, PropertiesKey.METADATA_LOCK_SIZE, null);
        return new BeanHolder(id, finalClassName, true, () -> new LocalCacheLockProvider(lockSize));
    }
}
