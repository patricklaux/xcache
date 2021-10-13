package com.igeeksky.xcache.extension;

import com.igeeksky.xcache.common.SPI;
import com.igeeksky.xcache.common.Singleton;
import com.igeeksky.xcache.util.BeanUtils;
import com.igeeksky.xcache.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 扩展类的静态工厂
 *
 * @author Patrick.Lau
 * @date 2021-07-26
 */
public class ExtensionFactory {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionFactory.class);

    private static final ConcurrentHashMap<String, Object> BEAN_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> interfaceClass, String clazz, String id, Object... params) {
        if (StringUtils.isNotEmpty(id)) {
            return (T) BEAN_MAP.computeIfAbsent(id, key -> instantiateClass(interfaceClass, clazz, params));
        }
        return instantiateClass(interfaceClass, clazz, params);
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiateClass(Class<T> interfaceClass, String clazz, Object... params) {
        if (StringUtils.isEmpty(clazz)) {
            SPI spi = interfaceClass.getAnnotation(SPI.class);
            clazz = spi.value();
            if (StringUtils.isEmpty(clazz)) {
                throw new BeanInstantiationException("className must not be null");
            }
        }

        try {
            Class<T> extensionClass = ExtensionLoader.findClass(clazz, interfaceClass);
            Singleton singleton = extensionClass.getAnnotation(Singleton.class);
            if (null != singleton) {
                return (T) BEAN_MAP.computeIfAbsent(clazz, key -> {
                    try {
                        return BeanUtils.instantiateClass(extensionClass, params);
                    } catch (ReflectiveOperationException e) {
                        logger.error(e.getMessage(), e);
                        throw new BeanInstantiationException(e);
                    }
                });
            }

            return BeanUtils.instantiateClass(extensionClass, params);
        } catch (BeanInstantiationException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (ReflectiveOperationException e) {
            logger.error(e.getMessage(), e);
            throw new BeanInstantiationException(e);
        }
    }

    public static Object instantiate(Class<?> clazz, Object... params) {
        try {
            return BeanUtils.instantiateClass(clazz, params);
        } catch (ReflectiveOperationException e) {
            logger.error(e.getMessage(), e);
            throw new BeanInstantiationException(e);
        }
    }

    public static Object instantiate(String className, Object... params) {
        try {
            Class<?> extensionClass = ExtensionLoader.getClass(className);
            return BeanUtils.instantiateClass(extensionClass, params);
        } catch (BeanInstantiationException e) {
            logger.error(e.getMessage(), e);
            throw e;
        } catch (ReflectiveOperationException e) {
            logger.error(e.getMessage(), e);
            throw new BeanInstantiationException(e);
        }
    }

}
