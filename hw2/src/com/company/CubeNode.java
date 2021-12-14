package com.company;

import java.util.ArrayList;
import java.util.List;

public class CubeNode implements INode {
    public static final int SIZE = 200;

    private final int i, j, k;

    public CubeNode(int i, int j, int k) {
        this.i = i;
        this.j = j;
        this.k = k;
    }

    @Override
    public List<Integer> getNeighbours() {
        ArrayList<Integer> a = new ArrayList<>();

        if (i > 0) {
            a.add((i - 1) * SIZE * SIZE + j * SIZE + k);
        }

        if (j > 0) {
            a.add(i * SIZE * SIZE + (j - 1) * SIZE + k);
        }

        if (k > 0) {
            a.add(i * SIZE * SIZE + j * SIZE + k - 1);
        }

        if (i + 1 != SIZE) {
            a.add((i + 1) * SIZE * SIZE + j * SIZE + k);
        }

        if (j + 1 != SIZE) {
            a.add(i * SIZE * SIZE + (j + 1) * SIZE + k);
        }

        if (k + 1 < SIZE) {
            a.add(i * SIZE * SIZE + j * SIZE + k + 1);
        }

        return a;
    }

    @Override
    public int getIndex() {
        return i * SIZE * SIZE + j * SIZE + k;
    }
}
