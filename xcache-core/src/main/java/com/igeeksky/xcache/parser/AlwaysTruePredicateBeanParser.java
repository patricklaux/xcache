package com.igeeksky.xcache.parser;

import com.igeeksky.xcache.beans.BeanDesc;
import com.igeeksky.xcache.beans.BeanHolder;
import com.igeeksky.xcache.beans.BeanParser;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-10-03
 */
public class AlwaysTruePredicateBeanParser implements BeanParser {

    private final String finalClassName = "com.igeeksky.xcache.extension.contain.AlwaysTruePredicateProvider";

    @Override
    public String getClassName() {
        return finalClassName;
    }

    @Override
    public BeanHolder parse(BeanDesc beanDesc) {
        String id = beanDesc.getId();
        if (null == id) {
            id = "alwaysTruePredicateProvider";
        }
        return new BeanHolder(id, finalClassName, true, com.igeeksky.xcache.extension.contain.AlwaysTruePredicateProvider::new);
    }
}
