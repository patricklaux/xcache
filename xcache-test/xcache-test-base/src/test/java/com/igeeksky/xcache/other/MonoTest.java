package com.igeeksky.xcache.other;

import com.igeeksky.xcache.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-09
 */
public class MonoTest {

    @BeforeEach
    void setUp() {
        Hooks.onOperatorDebug();
    }

    @AfterEach
    void tearDown() {
        Hooks.resetOnOperatorDebug();
    }

    @Test
    void testDuration() {
        Duration duration = Duration.ofMillis(-1L);
        Assertions.assertEquals(-1L, duration.toMillis());
    }

    @Test
    void testThrowException() {
        Set<String> set = new HashSet<>();
        set.add(null);
        Mono.just(set)
                .doOnNext(ks -> {
                    System.out.println(ks.size());
                    ks.forEach(key -> {
                        if (null == key) {
                            throw new RuntimeException("error");
                        }
                    });
                })
                .doOnError(throwable -> System.out.printf("1[%s]\n", throwable))
                .doOnSuccess(ks -> System.out.println("success"))
                .doOnNext(ks -> System.out.println("doOnNext[" + ks + "]"))
                .subscribe(ks -> System.out.println("subscribe[" + ks + "]"));
    }

    @Test
    void testDoOnNext1() {
        hasValueReturn(Mono.just(new User("ssss"))).subscribe();
    }

    static Mono<Void> hasValueReturn(Mono<User> mono) {
        return mono.doOnNext(user -> user.setName("xxxx")).doOnNext(System.out::println).then();
    }

    @Test
    void testDoOnNext3() {
        emptyReturn1()
                .doOnSuccess(user -> System.out.println("执行1" + user))
                .map(user -> {
                    System.out.println("执行2" + user);
                    return user;
                })
                .switchIfEmpty(emptyReturn2())
                .doOnSuccess(user -> System.out.println("执行3" + user))
                .subscribe();
    }

    static Mono<User> emptyReturn1() {
        System.out.println("emptyReturn1");
        return Mono.fromSupplier(() -> null);
    }

    @Test
    void testDoOnNext4() {
        Mono<User> mono = emptyReturn2();
        mono.switchIfEmpty(
                        Mono.just(new User("ss"))
                )
                .doOnSuccess(user -> System.out.println("执行2" + user))
                .subscribe();
    }

    @Test
    void testEmptyReturn2() {
        Mono<User> mono = emptyReturn2();
        mono.map(u -> new User("d"))
                .doOnSuccess(user -> System.out.println("执行2" + user))
                .subscribe();
    }

    static Mono<User> emptyReturn2() {
        System.out.println("emptyReturn2");
        return Mono.empty();
    }

}
