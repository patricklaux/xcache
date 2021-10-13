package com.igeeksky.xcache.beans;

import java.util.*;

/**
 *
 *
 * @author Patrick.Lau
 * @since 0.0.4 2021-09-26
 */
public class Parameter {

    private String name;

    private String value;

    private String className;

    private String ref;

    private Object instance;

    private String clazz;

    private String type;

    private final List<Parameter> list = new LinkedList<>();

    private final Set<Parameter> set = new LinkedHashSet<>(0);

    private final Map<String, Parameter> map = new LinkedHashMap<>(0);

    private final Map<String, Parameter> property = new LinkedHashMap<>(0);

    private final Map<String, Parameter> constructor = new LinkedHashMap<>(0);

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Parameter> getList() {
        return list;
    }

    public void setList(List<Parameter> list) {
        if (null != list) {
            this.list.addAll(list);
        }
    }

    public void addToList(Parameter parameter) {
        if (null != parameter) {
            this.list.add(parameter);
        }
    }

    public Set<Parameter> getSet() {
        return set;
    }

    public void setSet(Set<Parameter> set) {
        if (null != set) {
            this.set.addAll(set);
        }
    }

    public void addToSet(Parameter parameter) {
        if (null != parameter) {
            this.set.add(parameter);
        }
    }

    public Map<String, Parameter> getMap() {
        return map;
    }

    public void setMap(Map<String, Parameter> map) {
        if (null != map) {
            this.map.putAll(map);
        }
    }

    public void addToMap(String name, Parameter parameter) {
        if (null != name) {
            this.map.put(name, parameter);
        }
    }

    public Map<String, Parameter> getProperty() {
        return property;
    }

    public void setProperty(Map<String, Parameter> property) {
        this.property.putAll(property);
    }

    public void addToProperty(String name, Parameter parameter) {
        this.property.put(name, parameter);
    }

    public Map<String, Parameter> getConstructor() {
        return constructor;
    }

    public void setConstructor(Map<String, Parameter> constructor) {
        this.constructor.putAll(constructor);
    }

    public void addToConstructor(String name, Parameter parameter) {
        this.constructor.put(name, parameter);
    }

}