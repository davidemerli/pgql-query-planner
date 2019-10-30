package it.polimi.pgql.queryplanner;

import oracle.pgx.api.GraphBuilder;
import oracle.pgx.api.PgxGraph;
import oracle.pgx.api.PgxSession;
import oracle.pgx.api.VertexBuilder;
import oracle.pgx.common.Pair;
import oracle.pgx.common.types.IdType;
import oracle.pgx.config.IdGenerationStrategy;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GraphMaker {

    public static PgxGraph getRandomEnemyGraph(PgxSession session, String graphName, String[] vertices, int edgeSize) throws ExecutionException, InterruptedException {
        GraphBuilder<String> builder = session.createGraphBuilder(IdType.STRING, IdGenerationStrategy.USER_IDS, IdGenerationStrategy.USER_IDS);

        List<VertexBuilder<String>> vertexBuilders = new ArrayList<>();
        List<Pair<VertexBuilder<String>, VertexBuilder<String>>> edges = new ArrayList<>();
        Random rand = new Random();

        Arrays.asList(vertices).forEach(s -> {
            VertexBuilder<String> vb = builder.addVertex(s)
                    .addLabel("Person")
                    .setProperty("name", s)
                    .setProperty("percentuale di figaggine", rand.nextDouble());

            vertexBuilders.add(vb);
        });

        while (edges.size() < edgeSize) {
            VertexBuilder<String> first = vertexBuilders.get(rand.nextInt(vertexBuilders.size()));

            List<VertexBuilder<String>> suitableVertices = vertexBuilders.stream().filter(v -> v != first).collect(Collectors.toList());

            VertexBuilder<String> second = suitableVertices.get(rand.nextInt(suitableVertices.size()));

            Pair<VertexBuilder<String>, VertexBuilder<String>> pair = new Pair<>(first, second);

            if (edges.contains(pair)) continue;

            edges.add(pair);
            builder.addEdge(edges.size(), first, second).setProperty("edge-prop", "enemy").setLabel("IS ENEMY OF");
        }

        return builder.build(graphName);
    }
}
