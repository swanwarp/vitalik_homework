package com.company;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) {
        List<INode> graph = buildCube(CubeNode.SIZE);

        Instant start;
        int[] dClassique, dQuick;

        start = Instant.now();
        dClassique = bfsLeClassique(graph, 0);
        System.out.println(Duration.between(start, Instant.now()).toMillis());

        System.out.println();

        start = Instant.now();
        dQuick = bfsLeQuick(graph, 0, 10000, 7500, true);
        System.out.println(Duration.between(start, Instant.now()).toMillis());

        for (int i = 0; i < dClassique.length; i++) {
            if (dClassique[i] != dQuick[i]) {
                System.out.println("You were ducked! Duck 1 = " + dClassique[i] + ", Duck 2 = " + dQuick[i]);
            }
        }
    }

    public static int[] bfsLeClassique(List<INode> graph, int start) {
        boolean[] visited = new boolean[graph.size()];
        int[] d = new int[graph.size()];

        HashSet<INode> layer = new HashSet<>();
        d[start] = 0;

        layer.add(graph.get(start));

        while (!layer.isEmpty()) {
            HashSet<INode> newLayer = new HashSet<>();

            for (INode v : layer) {
                visited[v.getIndex()] = true;

                for (Integer ui : v.getNeighbours()) {
                    INode u = graph.get(ui);

                    if (!visited[u.getIndex()]) {
                        newLayer.add(u);
                        d[u.getIndex()] = d[v.getIndex()] + 1;
                    }
                }
            }

            layer = newLayer;
        }

        return d;
    }

    public static int[] bfsLeQuick(List<INode> graph, int start, int block, int scanBlock, boolean seqFilter) {
        AtomicBoolean[] visited = new AtomicBoolean[graph.size()];
        int[] d = new int[graph.size()];

        for (int i = 0; i < graph.size(); i++) {
            visited[i] = new AtomicBoolean();
        }

        INode[] layer = new INode[1];

        layer[0] = graph.get(start);
        d[start] = 0;
        int count = 0;

        visited[start].set(true);

        ExecutorService service = Executors.newScheduledThreadPool(10 * graph.size()/block);

        try {
            while (layer.length != 0) {
                int[] sizes = scan(layer, scanBlock, service);

                INode[] newLayer = new INode[sizes[sizes.length - 1]];

                pfor(graph, layer, visited, newLayer, sizes, 0, layer.length, block, service, d, count + 1);

                layer = filter(newLayer, scanBlock, service, seqFilter).toArray(new INode[0]);

                count++;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            service.shutdown();
        }

        return d;
    }

    private static List<INode> filter(INode[] newLayer, int block, ExecutorService service, boolean seq) throws ExecutionException, InterruptedException  {
        ArrayList<INode> a = new ArrayList<>();

        if (newLayer.length < block || seq) {
            for (int i = 0; i < newLayer.length; i++) {
                if (newLayer[i] != null)
                    a.add(newLayer[i]);
            }
        }
        else {
            int bSize = newLayer.length / block;

            if (bSize * block != newLayer.length) {
                bSize++;
            }

            Future<ArrayList<INode>>[] f = new Future[bSize];

            for (int i = 0; i < f.length; i++) {
                int ii = i;

                f[i] = service.submit(() -> {
                    int min = Math.min(block * ii + block, newLayer.length);
                    ArrayList<INode> aa = new ArrayList<>();

                    for (int j = block * ii + 1; j < min; j++) {
                        if (newLayer[j] != null)
                            aa.add(newLayer[j]);
                    }

                    return aa;
                });
            }

            for (Future<ArrayList<INode>> future : f) {
                a.addAll(future.get());
            }
        }

        return a;
    }

    private static int[] scan(INode[] layer, int block, ExecutorService service) throws ExecutionException, InterruptedException {
        int[] sizes = new int[layer.length];

        if (layer.length < block) {
            sizes[0] = layer[0].getNeighbours().size();

            for (int i = 1; i < layer.length; i++) {
                sizes[i] += layer[i].getNeighbours().size() + sizes[i - 1];
            }
        }
        else {
            int bSize = layer.length / block;

            if (bSize * block != layer.length) {
                bSize++;
            }

            Future<?>[] f = new Future[bSize];
            int[] sums = new int[bSize];
            int[] pSums = new int[bSize];

            for (int i = 0; i < f.length; i++) {
                int ii = i;

                f[i] = service.submit(() -> {
                    sizes[block * ii] = layer[block * ii].getNeighbours().size();

                    int min = Math.min(block * ii + block, layer.length);

                    for (int j = block * ii + 1; j < min; j++) {
                        sizes[j] += layer[j].getNeighbours().size() + sizes[j - 1];
                    }

                    sums[ii] = sizes[min - 1];
                });
            }

            for (Future<?> future : f) {
                future.get();
            }

            pSums[0] = sums[0];

            for (int i = 1; i < pSums.length; i++) {
                pSums[i] = pSums[i - 1] + sums[i];
            }

            for (int i = 1; i < f.length; i++) {
                int ii = i;

                f[i] = service.submit(() -> {
                    int min = Math.min(block * ii + block, layer.length);

                    for (int j = block * ii; j < min; j++) {
                        sizes[j] += pSums[ii - 1];
                    }
                });
            }

            for (int i = 1; i < f.length; i++) {
                f[i].get();
            }
        }

        return sizes;
    }

    private static void pfor(
            List<INode> graph, INode[] layer, AtomicBoolean[] visited, INode[] newLayer, int[] sizes, int l, int r,
            int block, ExecutorService service, int[] d, int count)
    {
        if (r - l <= block) {
            for (int i = l; i < r; i++) {
                addNeighboursFromNode(graph, layer, visited, newLayer, sizes, i, d, count);
            }
        }
        else {
            int m = (r + l) / 2;

            Future<?> left = service.submit(() -> pfor(graph, layer, visited, newLayer, sizes, l, m, block, service, d, count));
            Future<?> right = service.submit(() -> pfor(graph, layer, visited, newLayer, sizes, m + 1, r, block, service, d, count));

            try {
                left.get();
                right.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addNeighboursFromNode(List<INode> graph, INode[] layer, AtomicBoolean[] visited, INode[] newLayer, int[] sizes, int i, int[] d, int count) {
        int index = 0;

        if (i > 0) {
            index += sizes[i - 1];
        }

        for (Integer ui : layer[i].getNeighbours()) {
            INode u = graph.get(ui);

            if (visited[u.getIndex()].compareAndSet(false, true)) {
                newLayer[index++] = u;
                d[u.getIndex()] = count;
            }
        }
    }

    public static List<INode> buildCube(int side) {
        List<INode> cube = new ArrayList<>();

        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                for (int k = 0; k < side; k++) {
                    cube.add(new CubeNode(i, j, k));
                }
            }
        }

        return cube;
    }
}
