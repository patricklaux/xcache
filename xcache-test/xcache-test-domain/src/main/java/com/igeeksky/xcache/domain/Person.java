package com.igeeksky.xcache.domain;

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
}