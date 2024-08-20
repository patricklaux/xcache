package com.igeeksky.xcache.other;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;

/**
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-17
 */
public class FluxTest {

    @BeforeEach
    void setUp() {
        Hooks.onOperatorDebug();
    }

    @AfterEach
    void tearDown() {
        Hooks.resetOnOperatorDebug();
    }

    @Test
    void doFinally() {
        Flux.just(1, 2, 3)
                .doFinally(s -> System.out.println("doFinally:" + s))
                .doOnNext(n -> System.out.println("doOnNext:" + n))
                .subscribe(n -> System.out.println("subscribe:" + n));
    }

    @Test
    void doFinally2() {
        Flux.just(1, 2, 3)
                .doOnNext(n -> System.out.println("doOnNext:" + n))
                .then()
                .doFinally(signal -> System.out.println("doFinally:" + signal))
                .then()
                .doOnSuccess(n -> System.out.println("doOnNext2:" + n))
                .subscribe();
    }

}