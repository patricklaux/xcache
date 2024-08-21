package com.igeeksky.xcache.domain;

import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * @author patrick
 * @since 0.0.4 2024/5/12
 */
public class Key {

    private String name;

    private int age = 10;

    public Key() {
    }

    public Key(String name) {
        this.name = name;
    }

    public Key(int age, String name) {
        this.age = age;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
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