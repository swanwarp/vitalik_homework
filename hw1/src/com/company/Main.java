package com.company;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    static Random r = new Random();
    static int tests = 5;
    static int size = 100_000_000;

    public static void main(String[] args) {
        print("Seq", runSeq());
        print("Par", runPar());
    }

    private static Duration[] runSeq() {
        Duration[] time = new Duration[tests];

        for (int i = 0; i < tests; i++) {
            int[] testArray = generateData(size);

            Instant start = Instant.now();

            Sorter.quickSort(testArray, 0, size - 1);

            time[i] = Duration.between(start, Instant.now());
        }

        return time;
    }

    private static Duration[] runPar() {
        ExecutorService service = Executors.newScheduledThreadPool(1 + (int) (Math.log(size) / Math.log(2)));

        try {

            Duration[] time = new Duration[tests];

            for (int i = 0; i < tests; i++) {
                int[] testArray = generateData(size);

                Instant start = Instant.now();

                Sorter.parallelQuickSort(testArray, 0, size - 1, 10_000_000, service);

                time[i] = Duration.between(start, Instant.now());
            }

            return time;

        }
        finally {
            service.shutdown();
        }
    }

    private static void print(String name, Duration[] durations) {
        System.out.println("Results for " + name + ":");

        for (Duration d: durations) {
            System.out.println(d.toMillis());
        }

        System.out.println("Average: " + (Arrays.stream(durations).map(Duration::toMillis).reduce(Long::sum).get() / durations.length)+ " ms");
        System.out.println();
    }

    private static int[] generateData(int size) {
        int[] test = new int[size];

        for (int i = 0; i < test.length; i++) {
            test[i] = r.nextInt();
        }

        return test;
    }
}
