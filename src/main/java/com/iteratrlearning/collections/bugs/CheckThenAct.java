package com.iteratrlearning.collections.bugs;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class CheckThenAct {

    public static final String MOVIE = "Phantom Menace";
    public static final int VIEWS = 100_000;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void main(String[] args) throws InterruptedException {

        Map<String, BigDecimal> movieViews = new HashMap<>();
        movieViews.put(MOVIE, BigDecimal.ZERO);

        sequentialAdd(movieViews);
//        concurrentAdd(movieViews);

        executorService.shutdown();

        try {
            while(!executorService.awaitTermination(1, TimeUnit.SECONDS));
            System.out.println(movieViews);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static void sequentialAdd(Map<String, BigDecimal> movieViews) {
        for(int i = 1; i <= VIEWS; i++) {
            addOneView(movieViews);
        }
    }

    private static void concurrentAdd(Map<String, BigDecimal> movieViews) {
        for(int i = 1; i <= VIEWS; i++) {
            executorService.submit(() -> addOneView(movieViews));
        }
    }

    private static void addOneView(Map<String, BigDecimal> movieViews) {
        BigDecimal views = movieViews.get(MOVIE);
        if(views != null) {
            movieViews.put(MOVIE, views.add(BigDecimal.ONE));
        }

//        movieViews.computeIfPresent(MOVIE, (k, v) -> v.add(BigDecimal.ONE));
    }
}

