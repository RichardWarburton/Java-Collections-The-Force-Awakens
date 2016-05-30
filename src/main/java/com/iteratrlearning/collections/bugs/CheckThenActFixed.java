package com.iteratrlearning.collections.bugs;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class CheckThenActFixed {

    public static final String MOVIE = "Phantom Menace";
    public static final int VIEWS = 100_000;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException {

        Map<String, BigDecimal> movieViews = new ConcurrentHashMap<>();
        movieViews.put(MOVIE, BigDecimal.ZERO);

        concurrentAdd(movieViews, () -> addOneViewPreJava8(movieViews));
//        concurrentAdd(movieViews, () -> addOneViewJava8(movieViews));

        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
            System.out.println(movieViews);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void concurrentAdd(Map<String, BigDecimal> movieViews, Runnable task) {
        for(int i = 1; i <= VIEWS; i++) {
            executorService.submit(task);
        }
    }

    private static void addOneViewPreJava8(Map<String, BigDecimal> movieViews) {
        for(;;) {
            BigDecimal views = movieViews.get(MOVIE);
            if(views != null) {
                if(movieViews.replace(MOVIE, views, views.add(BigDecimal.ONE))) {
                    break;
                }
            }
        }
    }

    private static void addOneViewJava8(Map<String, BigDecimal> movieViews) {
        movieViews.computeIfPresent(MOVIE, (k, v) -> v.add(BigDecimal.ONE));
    }
}

