package com.igeeksky.xcache.other;


import com.igeeksky.xtool.core.concurrent.Futures;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * –Èƒ‚œﬂ≥Ã≤‚ ‘
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/27
 */
public class VirtualThreadTest {

    private static final String URL = "https://www.baidu.com/";
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    @Test
    void test() throws InterruptedException {
        int size = 2;
        String[] urls = new String[size];
        Arrays.fill(urls, URL);

        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("start:" + System.currentTimeMillis());
            try {
                Future<?>[] futures = new Future[size];
                for (int i = 0; i < size; i++) {
                    String url = urls[i];
                    futures[i] = executor.submit(() -> {
                        try (HttpClient client = HttpClient.newHttpClient()) {
                            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            return response.toString();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
                }

                Futures.awaitAll(1000, TimeUnit.MILLISECONDS, 0, futures);

                for (Future<?> future : futures) {
                    System.out.println(future.get());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("end:" + System.currentTimeMillis());
        }, 0, 5, TimeUnit.SECONDS);

        Thread.sleep(15000);

        scheduledFuture.cancel(true);
    }

    @Test
    void test2() {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.toString());
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    void test3() throws ExecutionException, InterruptedException {
        Future<String> future = executor.submit(() -> {
            try (HttpClient client = HttpClient.newHttpClient()) {
                HttpRequest request = HttpRequest.newBuilder(URI.create(URL)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                return response.toString();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        });
        System.out.println(future.get());
    }

}