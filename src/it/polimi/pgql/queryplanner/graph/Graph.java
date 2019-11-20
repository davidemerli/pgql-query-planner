package it.polimi.pgql.queryplanner.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Graph {

    private int[][] adjacencyMatrix;
    private final List<GraphVertex> vertices = new ArrayList<>();

    private int[] src;
    private int[] dst;

    private int edges;

    public Graph() {
        adjacencyMatrix = new int[0][0];
        src = new int[0];
        dst = new int[0];
    }

    public Graph(File config) throws IOException {
        List<String> lines = SettingsReader.getLinesFromFile(config);
        int vertexSize = Integer.parseInt(lines.get(0));

        for (int i = 1; i < vertexSize + 1; i++) {
            String vertexSettings = lines.get(i);

            GraphVertex v = new GraphVertex(vertexSettings.split(":")[0], vertexSettings.split(":")[1].split(","));

            addVertex(v);

            //TODO: finish
        }


    }

    public void addVertex(GraphVertex v) {
        if (vertices.contains(v)) {
            System.out.println("vertex already in the graph");
            return;
        }

        if (adjacencyMatrix == null) {
            adjacencyMatrix = new int[1][1];
        } else {
            int[][] newMatrix = new int[adjacencyMatrix.length + 1][adjacencyMatrix.length + 1];

            for (int i = 0; i < adjacencyMatrix.length; i++)
                System.arraycopy(adjacencyMatrix[i], 0, newMatrix[i], 0, adjacencyMatrix[i].length);

            adjacencyMatrix = newMatrix;
        }

        vertices.add(v);
    }

    public void addEdge(GraphVertex from, GraphVertex to) {
        if (!vertices.contains(from) || !vertices.contains(to)) {
            System.out.println("one of the given vertices is not in the graph");
            return;
        }

        int fromIndex = vertices.indexOf(from);
        int toIndex = vertices.indexOf(to);

        adjacencyMatrix[fromIndex][toIndex] = 1;

        edges++;
    }

    public void convertToCSR() {
        src = new int[vertices.size()];
        dst = new int[edges];

        int dstCount = 0;

        for (int i = 0; i < adjacencyMatrix.length; i++) {//cycling rows
            src[i] = dstCount;

            for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                if (adjacencyMatrix[i][j] == 1) {
                    dst[dstCount] = j;
                    dstCount++;
                }
            }
        }

        for (int i : src) {
            System.out.println(i);
        }

        System.out.println();

        for (int i : dst) {
            System.out.println(i);
        }
    }
}
