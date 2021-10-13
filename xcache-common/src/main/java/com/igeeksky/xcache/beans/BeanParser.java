package com.igeeksky.xcache.beans;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-02
 */
public interface BeanParser {

    default void setBeanMap(Map<String, BeanHolder> beanMap) {
    }

    String getClassName();

    BeanHolder parse(BeanDesc beanDesc);

}
