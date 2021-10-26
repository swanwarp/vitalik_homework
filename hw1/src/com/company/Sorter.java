package com.company;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Sorter {
    public static void parallelQuickSort(int[] a, int l, int r, int block, ExecutorService service) {
        if (r - l <= block) {
            quickSort(a, l, r);
        }
        else {
            int q = partition(a, l, r);


            Future<?> left = service.submit(() -> parallelQuickSort(a, l, q, block, service));
            Future<?> right = service.submit(() -> parallelQuickSort(a, q + 1, r, block, service));

            try {
                left.get();
                right.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public static void quickSort(int[] a, int l, int r) {
        if  (l < r) {
            int q = partition(a, l, r);
            quickSort(a, l, q);
            quickSort(a, q + 1, r);
        }
    }

    private static int partition(int[] a, int l, int r) {
        int v = a[(l + r) / 2];
        int i = l;
        int j = r;
        while (i <= j) {
            while (a[i] < v) {
                i++;
            }

            while (a[j] > v) {
                j--;
            }

            if (i >= j)
                break;

            swap(a, i++, j--);
        }

        return j;
    }

    private static void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
}
