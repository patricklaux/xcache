package com.igeeksky.xcache.domain;

import com.igeeksky.xtool.core.json.SimpleJSON;

public class User {

    private String id;
    private String name;
    private int age;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public User(String id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof User user)) return false;

        if (getAge() != user.getAge()) return false;
        if (getId() != null ? !getId().equals(user.getId()) : user.getId() != null) return false;
        return getName() != null ? getName().equals(user.getName()) : user.getName() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + getAge();
        return result;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}