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
 * @author Patrick.Lau
 * @since 1.0.0 2024/6/27
 */
public class VirtualThreadTest {

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


    @Test
    void test() throws InterruptedException {
        int size = 5;
        String[] urls = new String[size];
        Arrays.fill(urls, "https://milvus.io/");

        ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(() -> {
            System.out.println("start:" + System.currentTimeMillis());
            try {
                int i = 0;
                Future<?>[] futures = new Future[size];
                for (String url : urls) {
                    Future<String> future = executor.submit(() -> {
                        try (HttpClient client = HttpClient.newHttpClient()) {
                            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                            return response.toString();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    });
                    futures[i++] = future;
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
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://milvus.io/")).GET().build();
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
                HttpRequest request = HttpRequest.newBuilder(URI.create("https://milvus.io/")).GET().build();
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