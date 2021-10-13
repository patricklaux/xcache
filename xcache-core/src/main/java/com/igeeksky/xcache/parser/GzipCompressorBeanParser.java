package com.igeeksky.xcache.parser;

import com.igeeksky.xcache.beans.BeanParser;
import com.igeeksky.xcache.beans.BeanDesc;
import com.igeeksky.xcache.beans.BeanHolder;
import com.igeeksky.xcache.extension.compress.GzipCompressorProvider;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-03
 */
public class GzipCompressorBeanParser implements BeanParser {

    private final String finalClassName = "com.igeeksky.xcache.extension.compress.GzipCompressorProvider";

    @Override
    public String getClassName() {
        return finalClassName;
    }

    @Override
    public BeanHolder parse(BeanDesc beanDesc) {
        String id = beanDesc.getId();
        if (null == id) {
            id = "gzipCompressorProvider";
        }
        return new BeanHolder(id, finalClassName, true, GzipCompressorProvider::new);
    }
}
