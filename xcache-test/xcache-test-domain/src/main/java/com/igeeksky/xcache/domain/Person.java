package com.igeeksky.xcache.domain;

import com.igeeksky.xtool.core.json.SimpleJSON;

import java.util.Objects;

public class Person<K, V> {

    private K name;
    private V age;

    public Person() {
    }

    public Person(K name, V age) {
        this.name = name;
        this.age = age;
    }

    public K getName() {
        return name;
    }

    public void setName(K name) {
        this.name = name;
    }

    public V getAge() {
        return age;
    }

    public void setAge(V age) {
        this.age = age;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person<?, ?> person)) return false;

        return Objects.equals(getName(), person.getName()) && Objects.equals(getAge(), person.getAge());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(getName());
        result = 31 * result + Objects.hashCode(getAge());
        return result;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}