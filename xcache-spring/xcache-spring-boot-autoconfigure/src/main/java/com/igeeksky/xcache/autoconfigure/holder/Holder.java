package com.igeeksky.xcache.autoconfigure.holder;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public interface Holder<T> {

    void put(String beanId, T provider);

    T get(String beanId);

    Map<String, T> getAll();

}
