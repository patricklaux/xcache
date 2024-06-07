package com.igeeksky.xcache.autoconfigure.holder;

import com.igeeksky.xcache.common.Provider;

import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-29
 */
public interface Holder<T extends Provider> {

    void put(String beanId, T provider);

    T get(String beanId);

    Map<String, T> getAll();

}
