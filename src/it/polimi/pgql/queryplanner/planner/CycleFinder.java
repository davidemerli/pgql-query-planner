package it.polimi.pgql.queryplanner.planner;

import oracle.pgql.lang.ir.GraphPattern;
import oracle.pgql.lang.ir.GraphQuery;
import oracle.pgql.lang.ir.QueryVertex;
import oracle.pgql.lang.ir.VertexPairConnection;

import java.util.*;
import java.util.stream.Collectors;

public class CycleFinder {

    private enum Colors {
        WHITE, GRAY, BLACK;
    }

    private final GraphPattern graph;
    private final List<QueryVertex> vertices;
    private final List<Colors> colors;
    private final List<List<Integer>> cycles;

    private int[] mark, par;

    private int cycleNumber;

    public CycleFinder(GraphQuery graph) {
        this.graph = graph.getGraphPattern();
        this.vertices = new ArrayList<>(this.graph.getVertices());
        this.colors = new ArrayList<>();

        this.mark = new int[10000];
        this.par = new int[10000];
        this.cycles = new ArrayList<>();
        vertices.forEach(v -> cycles.add(new ArrayList<>()));

        this.vertices.forEach(queryVertex -> colors.add(Colors.WHITE));

        dfs(0, 0);
    }

    private void dfs(int u, int p) {
        if (colors.get(u) == Colors.BLACK) return;

        if (colors.get(u) == Colors.GRAY) {
            cycleNumber += 1;
            int current = p;

            mark[current] = cycleNumber;

            while (current != u) {
                current = par[current];
                mark[current] = cycleNumber;
            }

            return;
        }

        par[u] = p;

        colors.set(u, Colors.GRAY);

        for (int v : graph.getConnections().stream()
                .filter(vertexPairConnection -> vertexPairConnection.getSrc() == vertices.get(u))
                .map(VertexPairConnection::getDst)
                .map(vertices::indexOf)
                .collect(Collectors.toList())) {

            if (v == par[u]) continue;

            dfs(v, u);
        }

        colors.set(u, Colors.BLACK);
    }

    public void printCycles() {
        int edges = graph.getConnections().size();

        for (int i = 0; i < edges; i++) {
            if(mark[i] != 0) {
                cycles.get(mark[i]).add(i);
            }
        }

        for (int i = 1; i < cycleNumber + 1; i++) {
            System.out.println("Cycle #" + i);
            for (int x : cycles.get(i)) {
                System.out.print(vertices.get(x).getName());
            }
            System.out.println();
        }
    }
}
