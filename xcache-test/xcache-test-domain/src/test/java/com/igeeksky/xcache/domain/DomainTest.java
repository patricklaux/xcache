package com.igeeksky.xcache.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-24
 */
class DomainTest {

    @Test
    void testUserToString() {
        User user = new User();
        user.setId("001");
        user.setName("John");
        user.setAge(18);

        System.out.println(user);
        Assertions.assertEquals("{\"id\":\"001\",\"name\":\"John\",\"age\":18}", user.toString());
    }

    @Test
    void testPersonToString() {
        Person<String, Integer> person = new Person<>();
        person.setName("Lucy");
        person.setAge(18);

        System.out.println(person);
        Assertions.assertEquals("{\"name\":\"Lucy\",\"age\":18}", person.toString());
    }

    @Test
    void testKeyToString() {
        Key key = new Key();
        key.setName("Sara");
        key.setAge(18);

        System.out.println(key);
        Assertions.assertEquals("{\"name\":\"Sara\",\"age\":18}", key.toString());
    }

}