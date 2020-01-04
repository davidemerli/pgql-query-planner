package it.polimi.pgql.queryplanner.planner;

import de.normalisiert.utils.graphs.ElementaryCyclesSearch;
import it.polimi.pgql.queryplanner.planner.operators.*;
import it.polimi.pgql.queryplanner.statistics.StatisticHandler;
import it.polimi.pgql.queryplanner.statistics.StructureType;
import oracle.pgql.lang.ir.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryPlanner {

    public List<QueryPlan> customPlan(GraphQuery query) {
        GraphPattern graphPattern = query.getGraphPattern();

        StatisticHandler stats = new StatisticHandler();

        List<QueryVertex> allVertices = new ArrayList<>(graphPattern.getVertices());

        ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(
                getAdjacencyMatrix(graphPattern),
                allVertices.toArray());

        //Gets all cycles
        List<List<QueryVertex>> cycles = ecs.getElementaryCycles();

        //Retrieves longest cycle
        int cycleMaxLength = cycles.size() == 0 ? 0 : cycles.stream().max(Comparator.comparingInt(List::size)).get().size();
        List<List<QueryVertex>> maxLengthCycles =
                cycles.size() == 0 ? Collections.emptyList() :
                        cycles.stream().filter(vertices -> vertices.size() == cycleMaxLength).collect(Collectors.toList());

        //Useful getter for statistics
        Function<List<QueryVertex>, Double> getSelectivity = (vertices) -> vertices.stream()
                .map(v -> Float.valueOf(stats.getVertexSelectivity(v, getVertexConstraints(v, graphPattern))).doubleValue())
                .reduce((product, el) -> product *= el).orElse(0D);

        Optional<List<QueryVertex>> bestCycleOptional = maxLengthCycles.stream()
                .max(Comparator.comparingDouble(getSelectivity::apply));

        List<QueryVertex> bestCycle = bestCycleOptional.orElse(Collections.emptyList());

        System.out.println("Best Cycle:");
        System.out.println(bestCycle + " value: " + getSelectivity.apply(bestCycle));
        //Now searching for the non in cycle nodes

        List<QueryVertex> notInCycleVertices = allVertices.stream().filter(v -> !bestCycle.contains(v)).collect(Collectors.toList());

        System.out.println("Not in cycle:");
        System.out.println(notInCycleVertices);

        List<Structure> structureList = new ArrayList<>();
        List<Structure> subLists = new ArrayList<>();

        //Retrieves other structures
        for (QueryVertex toStructure : notInCycleVertices) {
            Structure current = getStructureFromNode(graphPattern, toStructure, notInCycleVertices);

            if (structureList.stream().noneMatch(structure -> structure.equals(current))) {
                structureList.add(current);
            }
        }

        printDebug(structureList, bestCycle, stats);

        for (Structure structure : structureList) {
            subLists.addAll(structure.getSubLines(stats));
        }

        structureList.addAll(subLists);

        Queue<List<QueryVertex>> priorityQueue = new ArrayDeque<>();

        Stream.concat(structureList.stream().map(s -> s.vertices), Stream.of(bestCycle))
                .sorted(Comparator.comparingDouble(vertices -> stats.getStructureSelectivity(
                        vertices,
                        vertices.equals(bestCycle) ? StructureType.CYCLE : StructureType.LINE,
                        graphPattern)))
                .forEach(priorityQueue::add);

        List<QueryPlan> plan = new ArrayList<>();


        //Starts planning
        for (List<QueryVertex> vertices : priorityQueue) {
            if (vertices.equals(bestCycle) && bestCycle.size() > 0) {
                cyclePlanner(stats, graphPattern, vertices, plan);
            } else {
                linePlanner(stats, graphPattern, vertices, plan);
            }
        }

        return plan;
    }

    private void cyclePlanner(StatisticHandler stats, GraphPattern graphPattern, List<QueryVertex> cycle, List<QueryPlan> plan) {
        List<QueryVertex> orderedCycle = cycle.stream()
                .sorted(Comparator.comparingDouble(v -> stats.getVertexSelectivity((QueryVertex) v, getVertexConstraints((QueryVertex) v, graphPattern))).reversed())
                .collect(Collectors.toList());

        QueryVertex a = orderedCycle.get(0);
        QueryVertex b = orderedCycle.get(1);

        plan.add(new RootVertexMatchOperator(a));
        plan.add(new RootVertexMatchOperator(b));

        List<List<QueryVertex>> paths = getPaths(a, b, cycle, graphPattern);

        linePlanner(stats, graphPattern, paths.get(0), plan);
        linePlanner(stats, graphPattern, paths.get(1), plan);
    }

    private void linePlanner(StatisticHandler stats, GraphPattern graphPattern, List<QueryVertex> line, List<QueryPlan> plan) {
        if (line.size() == 0) return;

        if (line.size() == 1) {
            for (QueryVertex vertexConnection : getVertexConnections(line.get(0), graphPattern)) {
                plan.add(new EdgeMatchOperator(line.get(0), vertexConnection));
            }
        } else if (line.size() == 2) {
            plan.add(new EdgeMatchOperator(line.get(0), line.get(1)));
        } else if (line.size() == 3) {
//            plan.add(new RootVertexMatchOperator(line.get(1)));
            plan.add(new CartesianProductOperator(line.get(0), line.get(2)));
            plan.add(new CommonNeighborMatchOperator(line.get(0), line.get(2)));
        } else {
            List<QueryVertex> orderedCycle = line.stream()
                    .filter(v -> !v.equals(line.get(0)) && !v.equals(line.get(line.size() - 1)))
                    .sorted(Comparator.comparingDouble(v -> stats.getVertexSelectivity((QueryVertex) v, getVertexConstraints((QueryVertex) v, graphPattern))).reversed())
                    .collect(Collectors.toList());

            QueryVertex c = orderedCycle.get(0);

            plan.add(new RootVertexMatchOperator(c));

            linePlanner(stats, graphPattern, line.subList(0, line.indexOf(c) + 1), plan);
            linePlanner(stats, graphPattern, line.subList(line.indexOf(c), line.size()), plan);
        }
    }

    /**
     * @param a
     * @param b
     * @param cycle
     * @param graphPattern
     * @return two paths to get from one node to another in a cycle
     */
    private List<List<QueryVertex>> getPaths(QueryVertex a, QueryVertex b, List<QueryVertex> cycle, GraphPattern graphPattern) {
        List<QueryVertex> first = new ArrayList<>();

        List<QueryVertex> connections = getVertexConnections(a, graphPattern);

        List<QueryVertex> firstAndSecond = connections.stream().filter(cycle::contains).collect(Collectors.toList());

        QueryVertex current1 = firstAndSecond.get(0);
        QueryVertex current2 = firstAndSecond.get(1);

        first.add(a);
        while (!current1.equals(b)) {
            first.add(current1);

            connections = getVertexConnections(current1, graphPattern);
            current1 = connections.stream()
                    .filter(v -> !first.contains(v))
                    .filter(cycle::contains)
                    .findFirst().get();
        }
        first.add(b);

        List<QueryVertex> second = new ArrayList<>();

        second.add(a);
        while (!current2.equals(b)) {
            second.add(current2);

            connections = getVertexConnections(current2, graphPattern);
            current2 = connections.stream()
                    .filter(cycle::contains)
                    .filter(v -> !second.contains(v))
                    .findFirst().get();
        }
        second.add(b);


        return Arrays.asList(first, second);
    }

    private void printDebug(List<Structure> structureList, List<QueryVertex> bestCycle, StatisticHandler stats) {
        System.out.println("Structures:");

        for (Structure structure : structureList) {
            System.out.println("Structure found:");
            System.out.print(structure.vertices);
            System.out.print("\t Connected with cycle with: \t");
            System.out.println(structure.getVerticesConnectedWithCycle(bestCycle));
            System.out.println();

            System.out.print("Longest line: \t");
            System.out.println(structure.getLongestLine(stats));
            System.out.println();

            System.out.print("Sub lines: \t");

            for (Structure subLine : structure.getSubLines(stats)) {
                System.out.print(subLine.vertices + " \t\t");
            }

            System.out.println();
            System.out.println();
            System.out.println();
        }
    }

    private Structure getStructureFromNode(GraphPattern gp, QueryVertex vertex, List<QueryVertex> availableNodes) {
        List<QueryVertex> structure = new ArrayList<>();
        structure.add(vertex);

        QueryVertex current = vertex;

        List<QueryVertex> visited = new ArrayList<>();
        visited.add(current);

        while (true) {
            List<QueryVertex> connectedVertices = getVertexConnections(current, gp);

            connectedVertices.stream()
                    .filter(v -> !structure.contains(v))
                    .filter(availableNodes::contains)
                    .forEach(structure::add);

            Optional<QueryVertex> next = structure.stream().filter(v -> !visited.contains(v)).findAny();

            if (next.isPresent()) {
                visited.add(next.get());
                current = next.get();
            } else {
                break;
            }
        }

        return new Structure(structure, gp);
    }

    private List<QueryVertex> getVertexConnections(QueryVertex vertex, GraphPattern gp) {
        return gp.getConnections().stream()
                .filter(connection -> connection.getSrc().equals(vertex) || connection.getDst().equals(vertex))
                .map(connection -> connection.getSrc().equals(vertex) ? connection.getDst() : connection.getSrc())
                .collect(Collectors.toList());
    }

    private boolean[][] getAdjacencyMatrix(GraphPattern pattern) {
        boolean[][] matrix = new boolean[pattern.getVertices().size()][pattern.getVertices().size()];

        List<QueryVertex> vertices = new ArrayList<>(pattern.getVertices());

        for (VertexPairConnection connection : pattern.getConnections()) {
            int src = vertices.indexOf(connection.getSrc());
            int dst = vertices.indexOf(connection.getDst());

            if (connection.getDirection() == Direction.OUTGOING) {
                matrix[src][dst] = true;
            } else if (connection.getDirection() == Direction.INCOMING) {
                matrix[dst][src] = true;
            } else if (connection.getDirection() == Direction.ANY) {
                matrix[src][dst] = true;
                matrix[dst][src] = true;
            }
        }

        return matrix;
    }

    public static List<QueryExpression.BinaryExpression> getVertexConstraints(QueryVertex qv, GraphPattern gp) {
        List<QueryExpression.BinaryExpression> list = new ArrayList<>();

        for (QueryExpression constraint : gp.getConstraints()) {
            if (constraint instanceof QueryExpression.BinaryExpression) {
                QueryExpression.BinaryExpression be = (QueryExpression.BinaryExpression) constraint;

                boolean first = be.getExp1() instanceof QueryExpression.PropertyAccess
                        && ((QueryExpression.PropertyAccess) be.getExp1()).getVariable().equals(qv);
                boolean second = be.getExp2() instanceof QueryExpression.PropertyAccess
                        && ((QueryExpression.PropertyAccess) be.getExp2()).getVariable().equals(qv);

                if (first || second) {
                    list.add(be);
                }
            }
        }

        return list;
    }

    /**
     * Class used to store lines and have useful functions
     */
    private class Structure {
        List<QueryVertex> vertices;
        GraphPattern graphPattern;

        public Structure(List<QueryVertex> vertices, GraphPattern graphPattern) {
            this.vertices = vertices;
            this.graphPattern = graphPattern;
        }

        public List<QueryVertex> getVerticesConnectedWithCycle(List<QueryVertex> cycle) {
            return this.vertices.stream()
                    .filter(v -> getVertexConnections(v, graphPattern).stream().anyMatch(cycle::contains))
                    .collect(Collectors.toList());
        }

        private List<QueryVertex> getLongestLineRecursive(List<QueryVertex> line, List<QueryVertex> notMatched, StatisticHandler stats) {
            if (notMatched.size() == 0 || line.size() == 0) return line;

            QueryVertex last = line.get(line.size() - 1);

            List<QueryVertex> connected = getVertexConnections(last, graphPattern);

            if (notMatched.stream().noneMatch(connected::contains)) return line;

            List<QueryVertex> max = new ArrayList<>();

            for (QueryVertex queryVertex : connected) {
                if (!notMatched.contains(queryVertex)) continue;

                List<QueryVertex> newLine = new ArrayList<>(line);
                List<QueryVertex> newNotMatched = new ArrayList<>(notMatched);
                newLine.add(queryVertex);
                newNotMatched.remove(queryVertex);

                List<QueryVertex> result = getLongestLineRecursive(newLine, newNotMatched, stats);

                if (result.size() > max.size()) {
                    max = result;
                }
            }

            return max;
        }

        public List<Structure> getSubLines(StatisticHandler stats) {
            List<Structure> structureList = new ArrayList<>();
            List<QueryVertex> toMatch = new ArrayList<>(vertices);
            toMatch.removeAll(getLongestLine(stats));

            if (toMatch.size() == 0) return Collections.emptyList();

            while (toMatch.size() != 0) {
                Structure s = getStructureFromNode(graphPattern, toMatch.get(0), toMatch);

                List<QueryVertex> longestLine = s.getLongestLine(stats);
                structureList.add(new Structure(longestLine, graphPattern));
                toMatch.removeAll(longestLine);
            }

            return structureList;
        }

        public List<QueryVertex> getLongestLine(StatisticHandler stats) {
            List<QueryVertex> max = new ArrayList<>();

            for (QueryVertex vertex : this.vertices) {
                List<QueryVertex> line = new ArrayList<>();
                line.add(vertex);
                List<QueryVertex> notMatched = new ArrayList<>(vertices);
                notMatched.remove(vertex);

                List<QueryVertex> result = getLongestLineRecursive(line, notMatched, stats);
                if (result.size() > max.size() ||
                        stats.getStructureSelectivity(result, StructureType.LINE, graphPattern) > stats.getStructureSelectivity(max, StructureType.LINE, graphPattern)) {
                    max = result;
                }
            }

            return max;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Structure)) return false;

            return ((Structure) obj).vertices.containsAll(this.vertices);
        }
    }
}