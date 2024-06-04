package com.igeeksky.xcache.beans;

import com.igeeksky.xtool.core.collection.Maps;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-28
 */
public class BeanDesc {

    public BeanDesc() {
    }

    public BeanDesc(String className) {
        this.className = className;
    }

    public BeanDesc(String id, String className) {
        this.id = id;
        this.className = className;
    }

    public BeanDesc(String id, Supplier<Object> supplier) {
        this.id = id;
        this.supplier = supplier;
    }

    private String className;

    private String id;

    private boolean singleton = true;

    private Supplier<Object> supplier;

    private final Map<String, Object> constructor = new LinkedHashMap<>(1);

    private Map<String, Object> property = new LinkedHashMap<>(1);

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public Supplier<Object> getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier<Object> supplier) {
        this.supplier = supplier;
    }

    public Map<String, Object> getConstructor() {
        return constructor;
    }

    public void setConstructor(Map<String, Object> constructor) {
        if (Maps.isNotEmpty(constructor)) {
            this.constructor.putAll(constructor);
        }
    }

    public Map<String, Object> getProperty() {
        return property;
    }

    public void setProperty(Map<String, Object> property) {
        if (Maps.isNotEmpty(property)) {
            this.property.putAll(property);
        }
    }

    @Override
    public String toString() {
        return "{\"className\":\""
                + className
                + "\",\"id\":\""
                + id +
                "\",\"singleton\":\""
                + singleton +
                "\",\"supplier\":\""
                + supplier +
                "\",\"constructor\":\""
                + constructor +
                "\"}";
    }
}