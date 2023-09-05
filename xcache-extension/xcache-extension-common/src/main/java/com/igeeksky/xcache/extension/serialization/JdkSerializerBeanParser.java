package com.igeeksky.xcache.extension.serialization;

import com.igeeksky.xcache.beans.BeanDesc;
import com.igeeksky.xcache.beans.BeanHolder;
import com.igeeksky.xcache.beans.BeanParser;
import com.igeeksky.xcache.extension.serialization.JdkSerializerProvider;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-03
 */
public class JdkSerializerBeanParser implements BeanParser {

    private final String finalClassName = "com.igeeksky.xcache.extension.serialization.JdkSerializerProvider";

    @Override
    public String getClassName() {
        return finalClassName;
    }

    @Override
    public BeanHolder parse(BeanDesc beanDesc) {
        String id = beanDesc.getId();
        if (null == id) {
            id = "jdkSerializerProvider";
        }
        return new BeanHolder(id, finalClassName, true, JdkSerializerProvider::new);
    }
}
