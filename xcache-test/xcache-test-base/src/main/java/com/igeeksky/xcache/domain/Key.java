package com.igeeksky.xcache.domain;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.StringJoiner;

/**
 * @author patrick
 * @since 0.0.4 2024/5/12
 */
public class Key {

    private int age = 10;

    private String name;

    public Key() {
    }

    public Key(String name) {
        this.name = name;
    }

    public Key(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Key key)) return false;

        if (getAge() != key.getAge()) return false;
        return getName() != null ? getName().equals(key.getName()) : key.getName() == null;
    }

    @Override
    public int hashCode() {
        int result = getAge();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }
}
