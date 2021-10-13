package com.igeeksky.xcache.extension;

import com.igeeksky.xcache.common.annotation.NotNull;
import com.igeeksky.xcache.common.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Patrick.Lau
 * @since 0.0.3 2021-07-25
 */
public class ExtensionLoader {

    private static final ConcurrentMap<Class<?>, ConcurrentMap<String, Class<?>>> cachedClass = new ConcurrentHashMap<>();

    private static final ClassLoader classLoader = ExtensionLoader.class.getClassLoader();

    @SuppressWarnings("unchecked")
    public static <T> Class<T> findClass(@NotNull String className, @NotNull Class<T> interfaceClass) {
        ConcurrentMap<String, Class<?>> map = cachedClass.computeIfAbsent(interfaceClass, k -> new ConcurrentHashMap<>(32));
        Class<?> result = map.computeIfAbsent(className, k -> getClass(k, interfaceClass));
        return (Class<T>) result;
    }

    @Nullable
    private static Class<?> getClass(@NotNull String className, @NotNull Class<?> interfaceClass) {
        try {
            Class<?> implClass = classLoader.loadClass(className);
            if (interfaceClass.isAssignableFrom(implClass)) {
                return implClass;
            }
            return null;
        } catch (ClassNotFoundException e) {
            throw new BeanInstantiationException(e);
        }
    }

    public static Class<?> getClass(@NotNull String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new BeanInstantiationException(e);
        }
    }

}
