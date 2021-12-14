package com.company;

import java.util.ArrayList;
import java.util.List;

public class Node implements INode {
    private final int index;
    private final List<Integer> neighbours;

    public Node(int index) {
        this.index = index;

        neighbours = new ArrayList<>();
    }

    public void addNeighbour(int v) {
        neighbours.add(v);
    }

    @Override
    public List<Integer> getNeighbours() {
        return neighbours;
    }

    @Override
    public int getIndex() {
        return index;
    }
}
