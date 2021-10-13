package com.igeeksky.xcache.beans;

import java.util.function.Supplier;

/**
 * <b>Bean持有器</b><br/>
 * 实现Bean的lazy init
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-22
 */
public class BeanHolder {

    private final Object lock = new Object();

    private volatile Object bean;
    private final boolean singleton;
    private final String id;
    private final String className;
    private final Supplier<Object> supplier;

    public BeanHolder(String id, String className, Supplier<Object> supplier) {
        this(id, className, true, supplier);
    }

    public BeanHolder(String id, String className, boolean singleton, Supplier<Object> supplier) {
        this.id = id;
        this.className = className;
        this.singleton = singleton;
        this.supplier = supplier;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> requiredType) {
        return (T) getBean();
    }

    public Object getBean() {
        if (!singleton) {
            return supplier.get();
        }
        if (null == bean) {
            synchronized (lock) {
                if (null == bean) {
                    return (bean = supplier.get());
                }
            }
        }
        return bean;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public String getId() {
        return id;
    }

    public String getClassName() {
        return className;
    }
}
